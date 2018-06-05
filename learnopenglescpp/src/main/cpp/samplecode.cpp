
//opengl 环境下调用函数

#include <include/beautitune.h>
#include <GLES2/gl2.h>
typedef struct _tag_thread_para {
	THandle TuneEngine;
	FPOINT center;
	TFloat radius;
	TInt32 Width;
	TInt32 Height;
	TInt32 tExit;
	Mat   * DstImg;
	texture_2d * srcTex,*dstTex;
	TInt32 IsDraw;
}thread_para;

void main()
{
	thread_para EnginePara = {0};
	//1.打开并发送图像到texture，
	thread_para * pPara = &EnginePara;
    pPara->srcTex = new texture_2d(pPara->DstImg->data,pPara->Width,pPara->Height, GL_BGRA, GL_RGBA, GL_TEXTURE0, GL_UNSIGNED_BYTE);
	pPara->dstTex = new texture_2d(NULL,               pPara->Width,pPara->Height, GL_BGRA, GL_RGBA, GL_TEXTURE1, GL_UNSIGNED_BYTE);
	//2.初始化Eninge，并预处理,进入每个功能需要单独重新初始化
	if (!pPara->TuneEngine)
	{
		BTType ProType = Detail;// Smooth; TeethWhite;
		nRes = BeautiTune_Init(&pPara->TuneEngine,pPara->Width,pPara->Height, ProType);
		if (nRes)
		{
			printf("BeautiTune_Init failed\n");
			return -2;
		}
		nRes = BeautiTune_PreProcess(pPara->TuneEngine, pPara->srcTex->getTextureId());
		if (nRes)
		{
			printf("BeautiTune_PreProcess failed\n");
			return -3;
		}
	}
	//3.响应每次涂抹动作，单次涂抹产生的参数有（center，radius），擦除动作也会产生同样的参数
	//具体的效果参数有不同，见code
	{
		TypePara Para = { 0 };
		if (ProType == TeethWhite)
		{
			Para.BsWork = Paint;//涂抹还是擦除
			nRes = BeautiTune_Process(pPara->TuneEngine, pPara->srcTex->getTextureId(), pPara->dstTex->getTextureId(), &(pPara->center), 15.0f,&Para, ImgBuf);
		}
		else if (ProType == Smooth)
		{
			Para.IsMoreSmooth = 0;//是否需要更强的模糊
			Para.BsWork = Paint;
			nRes = BeautiTune_Process(pPara->TuneEngine, pPara->srcTex->getTextureId(), pPara->dstTex->getTextureId(), &(pPara->center), 15.0f,&Para, ImgBuf);
		}
		else if (ProType == Detail)
		{
			Para.BsWork = Paint;
			nRes = BeautiTune_Process(pPara->TuneEngine, pPara->srcTex->getTextureId(), pPara->dstTex->getTextureId(), &(pPara->center), 15.0f, &Para, ImgBuf);
		}
	}
	//4.效果切换或者退出时需要释放效果资源
	if (pPara->TuneEngine)
	{
		BeautiTune_UnInit(pPara->TuneEngine);
		if (pPara->srcTex) delete pPara->srcTex;
		if (pPara->dstTex) delete pPara->dstTex;
		pPara->TuneEngine = NULL;

	}
}
