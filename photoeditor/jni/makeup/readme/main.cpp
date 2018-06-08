#include "opencvlib.h"
#include "windows.h"
#include "ts-detect-object.h"
#include "ts-facial-outline.h"
#include "cosmetic.h"
using namespace std;

#define FRAME_H 768
#define FRAME_W 1024

int main(int argc, char **argv)
{
	cout<<"esc-----exit\n";
	VideoCapture cap(0); 
	cap.set( CV_CAP_PROP_FRAME_WIDTH,FRAME_W);
	cap.set( CV_CAP_PROP_FRAME_HEIGHT,FRAME_H);
	if (!cap.isOpened())
	{
		cout<<"open camera failed\n";
		return -1;
	}

	Mat frame;
	namedWindow( "makeup", 1 );

	//face outline initiation
	TSFacialOutline  ft = tsFacialOutline_create();
	TSObjectDetector od = tsDetectObject_create();
	int nLandmarks ;
	TDouble *landmarks = TNull;
	tsFacialOutline_setProperty(ft, "landmark-type", "fast16");//fast16 default
	tsFacialOutline_getProperty(ft, "landmarks-number", &nLandmarks);
	landmarks = (TDouble *)malloc(2*nLandmarks*sizeof(TDouble));
	TPOINT* lm = (TPOINT*) malloc(nLandmarks*sizeof(*lm));

	THandle pMakeUp = TNull;

	
	//Make Up initiation
	TMU_Init(&pMakeUp);

	//load resource
	TMU_MakeUpStyleLoad(pMakeUp,0);

	TSOFFSCREEN src = {0},dst = {0};
	src.i32Height = FRAME_H;
	src.i32Width  = FRAME_W;
	src.pi32Pitch[0] = FRAME_W;
	src.pi32Pitch[1] = FRAME_W;
	src.ppu8Plane[0] = (unsigned char*)malloc(FRAME_W*FRAME_H*3/2);
	src.ppu8Plane[1] = src.ppu8Plane[0] + FRAME_W*FRAME_H;
	src.u32PixelArrayFormat = TS_PAF_NV21;

	//逐帧处理
	for (;;)
	{
		int c = cvWaitKey(10);
		c = (char)c;
		if(c == 27)
		{
			break;
		}

		 cap>>frame;

		 //resize(frame,frame,Size(frame.rows,frame.rows));
		 //face detect
		{
			Mat imgray;
			TSOFFSCREEN im={0};				
			TRESULT err;
			TRECT faceRect = {0};

			//人脸特征点检测
			cvtColor(frame, imgray, CV_BGR2GRAY);
			im.i32Height    = imgray.rows;
			im.i32Width     = imgray.cols;
			im.pi32Pitch[0] = imgray.step;
			im.ppu8Plane[0] = imgray.data;
			im.u32PixelArrayFormat = TS_PAF_GRAY;

		    err = tsDetectObject_setImage(od, &im);
			if (err != TOK) 
			 {
				 printf("face image error\n");
			 }
			 int face_count = tsDetectObject_detect(od, "face");
			 if (face_count > 0)
				 tsDetectObject_object(od, 0, &faceRect);
			 else
			 {
				imshow("makeup",frame);
				continue;
			 }
			
			 err = tsFacialOutline_figure(ft, &im, faceRect, NULL);
			 if (err == TOK)//特征点检测成功，进行彩妆
			 {							
				 Mat imSrc,imDst;
				 //cvtColor(frame,imSrc,CV_RGB2RGBA);
				 frame.copyTo(imSrc);
				 imSrc.copyTo(imDst);

				 //载入特征点
				 tsFacialOutline_getProperty(ft, "landmarks", (void*)landmarks);
				 for (int i = 0; i < nLandmarks; i++) 
				 {
					 lm[i].x = ((TDouble*)landmarks)[i*2];
					 lm[i].y = ((TDouble*)landmarks)[i*2+1];
					 circle(imSrc,Point(lm[i].x,lm[i].y),2,Scalar(100,200,200));
				 }
				 
#if 1
				 /*src.i32Height = imSrc.rows;
				 src.i32Width  = imSrc.cols;
				 src.pi32Pitch[0] = imSrc.step;
				 src.ppu8Plane[0] = imSrc.data;
				 src.u32PixelArrayFormat = TS_PAF_RGB32_B8G8R8A8;*///实时状态下，这里是Nv21

				 BGR2YUV420VU(imSrc.data,imSrc.step,src.ppu8Plane,src.pi32Pitch,FRAME_W,FRAME_H);
 			 
				 LARGE_INTEGER m_nFreq;
				 LARGE_INTEGER m_nBeginTime;  
				 LARGE_INTEGER nEndTime; 
				 QueryPerformanceFrequency(&m_nFreq); // 获取时钟周期  
				 QueryPerformanceCounter(&m_nBeginTime); // 获取时钟计数

				 TRESULT nres = TMU_MakeUpStyle(pMakeUp,&src,&src,lm,nLandmarks);//彩妆处理
				 if (nres)
				 {
					 cout<<"TMU_MakeUpStyle error\n";
				 }

				 QueryPerformanceCounter(&nEndTime);  
				 cout <<"make up "<< (double)(nEndTime.QuadPart-m_nBeginTime.QuadPart)*1000/m_nFreq.QuadPart << "ms"<<endl;
				 YUV420VU2BGR(src.ppu8Plane,src.pi32Pitch,imSrc.data,imSrc.step,FRAME_W,FRAME_H);
#endif
				 imshow("makeup",imSrc);

			 }
		 }
	}

	TMU_UnInit(pMakeUp);
	tsFacialOutline_destroy(ft);
	tsDetectObject_destroy(od);
	if (landmarks)
	{
		free(landmarks);
	}

	if (lm)
	{
		free(lm);
	}

	if (src.ppu8Plane[0])
	{
		free(src.ppu8Plane[0]);
	}
	return 0;
}