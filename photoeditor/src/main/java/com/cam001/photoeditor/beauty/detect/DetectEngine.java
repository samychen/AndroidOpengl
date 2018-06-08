package com.cam001.photoeditor.beauty.detect;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Build;

import com.cam001.util.LogUtil;
import com.cam001.util.Util;

import java.util.ArrayList;

public class DetectEngine 
	implements Camera.PreviewCallback{
	
	private static String TAG = "DetectEngine";
	
	public static final int DETECT_TYPE_FACE 	= 0x00000001;
	public static final int DETECT_TYPE_SMILE 	= 0x00000002;
	public static final int DETECT_TYPE_GESTURE	= 0x00000004;
	public static final int DETECT_TYPE_SOUND 	= 0x00000008;
	
	private Object mLock = new Object();
	
	private Camera mCamera = null; 
	private int mDetectType = 0;
	private PreviewDetect mFaceDetector = null;
	private GestureDetect mGestureDetector = null;
	private SmileDetect mSmileDetector = null;
	private NV21Sampler mNv21Sampler = null;
	private ArrayList<OnDetectListener> mlstDetectListener = null;
	private int mPreviewWidth,mPreviewHeight;
	private int mDetectWidth, mDetectHeight;
	private int mDetectSample = 0;
	private int mDisplayWidth,mDisplayHeight;
	private int mDisplayOrientation;
	private int mDeviceRotation;
	private int mDetectRotation;
	private boolean mbMirror;
	private Matrix mMatDisplay = null;
	private Matrix mMatHWDetect = null;
	private byte[][] mPreviewBuffer = null; 
	private int mBufIndex = 0;
	private Rect[] mFaces = null;
	
	private boolean mIsStarted = false;
	private boolean mIsAppendToStart = false;
	private boolean mIsHWDetected = false;
	private boolean mIsFrameExternal = false;
	
	private PreviewDetectThread mPreviewDetectThread = null;
	private SoundDetectThread mSoundDetectThread = null;
	
	public DetectEngine() {
		mMatDisplay = new Matrix();
		mMatHWDetect = new Matrix();
		mlstDetectListener = new ArrayList<OnDetectListener>();
		mNv21Sampler = new NV21Sampler();
	}
	
	public void setFrameCallbackExternal(boolean bExternal) {
		mIsFrameExternal = bExternal;
	}
	
	public void destroy() {
		stopDetect();
		if(mFaceDetector!=null) {
			mFaceDetector.uninitialize();
			mFaceDetector = null;
		}
		if(mGestureDetector!=null) {
			mGestureDetector.uninitialize();
			mGestureDetector = null;
		}
		if(mSmileDetector!=null) {
			mSmileDetector.uninitialize();
			mSmileDetector = null;
		}
		mNv21Sampler.destroy();
		mlstDetectListener.clear();
	}
	
	public void setDisplaySize(int width, int height) {
		mDisplayWidth = width;
		mDisplayHeight = height;
		refreshTransform();
	}
	
	public void setDisplayOritation(int degrees) {
		mDisplayOrientation = degrees;
		refreshTransform();
	}
	
	public void setDeviceRotation(int degrees) {
		mDeviceRotation = degrees;
		refreshTransform();
	}
	
	public void setMirror(boolean bMirror) {
		mbMirror = bMirror;
		refreshTransform();
	}
	
	private void refreshTransform() {
		if(mDisplayWidth==0 || mPreviewHeight==0) {
			return;
		}
		mDetectSample = mPreviewWidth/320;
		mDetectWidth = mPreviewWidth/mDetectSample;
		mDetectHeight = mPreviewHeight/mDetectSample;
		
		if(mbMirror) {
			mDetectRotation = (720-mDeviceRotation-mDisplayOrientation)%360;
		} else {
			mDetectRotation = (mDeviceRotation+mDisplayOrientation)%360;
		}
		if(mDetectRotation%180!=0) {
			int temp = mDetectWidth;
			mDetectWidth = mDetectHeight;
			mDetectHeight = temp;
		}
		
		mMatDisplay.reset();
		int maxDetect = Math.max(mDetectWidth,mDetectHeight);
		int minDetect = Math.min(mDetectWidth,mDetectHeight);
		float sx,sy;
		mMatDisplay.postTranslate(-mDetectWidth/2.0f, -mDetectHeight/2.0f);
		mMatDisplay.postRotate(360-mDetectRotation);
		mMatDisplay.postScale(mbMirror ? -1 : 1, 1);
		mMatDisplay.postRotate(mDisplayOrientation);
		sx = (float)mDisplayWidth/minDetect;
		sy = (float)mDisplayHeight/maxDetect;
		mMatDisplay.postScale(sx, sy);
		mMatDisplay.postTranslate(mDisplayWidth / 2f, mDisplayHeight / 2f);
		LogUtil.logV(TAG, "Disp(%dx%d)Rotate=%d Prev(%dx%d)Rotate=%d", mDisplayWidth, mDisplayHeight, mDisplayOrientation, mPreviewWidth, mPreviewHeight, mDetectRotation);
		
		mMatHWDetect.reset();
		mMatHWDetect.setRectToRect(new RectF(-1000,-1000,1000, 1000), 
				new RectF(0, 0, maxDetect, minDetect), 
				ScaleToFit.FILL);
		mMatHWDetect.postTranslate( -maxDetect/2.0f, -minDetect/2.0f);
		mMatHWDetect.postRotate(mDetectRotation);
		mMatHWDetect.postTranslate(mDetectWidth/2.0f,mDetectHeight/2.0f);
	}
	
	
	public Rect[] detectFace(byte[] nv21, int width, int height) {
		if(mFaceDetector==null) {
			mFaceDetector = new FaceTrack();
			mFaceDetector.initialize();
		}
		return mFaceDetector.detect(nv21, width, height);
	}
	
	public Rect[] detectGesture(byte[] nv21, int width, int height, Rect[] faces) {
		if(mGestureDetector==null) {
			mGestureDetector = new GestureDetect();
			mGestureDetector.initialize();
		}
		if(faces==null || faces.length<1 || faces[0]==null) {
			return null;
		}
		//This may be in none-UI thread. (Detect Thread)
		//Avoid that any of mFaces��s elements is null while gesture detecting.
//		synchronized(mLock) {
//			mGestureDetector.setFaces(faces);
//		}
		Rect[] res =  mGestureDetector.detect(nv21, width, height,faces);
		String gest = "null";
		String face = "null";
		if(faces!=null && faces.length>0) face = faces[0].toString();
		if(res!=null && res.length>0) gest = res[0].toString();
		LogUtil.logV(TAG, "detectGesture face="+face+" gest="+gest);
		return res;
	}
	
	public Rect[] detectSmile(byte[] nv21, int width, int height, Rect[] faces) {
		if(mSmileDetector==null) {
			mSmileDetector = new SmileDetect();
			mSmileDetector.initialize();
		}
		if(faces==null || faces.length<1 || faces[0]==null) {
			return null;
		}
		//This may be in none-UI thread. (Detect Thread)
		//Avoid that any of mFaces��s elements is null while gesture detecting.
		synchronized(mLock) {
			mSmileDetector.setFaces(faces);
		}
		Rect[] res =  mSmileDetector.detect(nv21, width, height);
		String smile = "null";
		String face = "null";
		if(faces!=null && faces.length>0) face = faces[0].toString();
		if(res!=null && res.length>0) smile = res[0].toString();
		LogUtil.logV(TAG, "detectSmile face="+face+" smile="+smile);
		return res;
	}
			
	
	/**
	 * The detect type can be DETECT_TYPE_FACE/DETECT_TYPE_SMILE/DETECT_TYPE_GESTURE,
	 * Or combine of them.
	 * @param type
	 */
	public void setDetectType(int type) {
		if(mDetectType==type) {
			return;
		}
		boolean needRestart = false;
		if(mDetectType==DETECT_TYPE_FACE && mIsHWDetected) {
			needRestart = true;
		}
		if(type==DETECT_TYPE_FACE && mIsHWDetected) {
			needRestart = true;
		}
		if((mDetectType&DETECT_TYPE_SOUND)!=0 || (type&DETECT_TYPE_SOUND)!=0) {
			needRestart = true;
		}
		mDetectType = type;
		if(mIsStarted && needRestart) {
			stopDetect();
			startDetect(mCamera);
		} else if(!mIsStarted && mIsAppendToStart) {
			startDetect(mCamera);
		}
		
		
	}
	
	public int getDetectType()
	{
		return mDetectType;
	}
	
	public void addOnDetectListener(OnDetectListener l) {
		if(mlstDetectListener.contains(l)) {
			return;
		}
		mlstDetectListener.add(l);
	}
	
	public void startDetect(Camera camera) {
		mCamera = camera;
		Size previewSize = camera.getParameters().getPreviewSize();
		mPreviewWidth = previewSize.width;
		mPreviewHeight = previewSize.height;
		mIsHWDetected = isSupportHWFaceDetect(camera);
		refreshTransform();
		if(mIsStarted) {
			return;
		}
		if(mDetectType==0) {
			mIsAppendToStart = true;
			return;
		}
		if(mIsHWDetected) {
			startHWFaceDetect(camera);
		}
		mIsStarted = true;
		if(((mDetectType&DETECT_TYPE_FACE)!=0 && !mIsHWDetected) ||
				(mDetectType&DETECT_TYPE_SMILE)!=0 ||
				(mDetectType&DETECT_TYPE_GESTURE)!=0) {
			Util.Assert(mPreviewDetectThread == null);
			mPreviewDetectThread = new PreviewDetectThread();
			mPreviewDetectThread.start();
			if(!mIsFrameExternal) {
				mPreviewBuffer = new byte[][]{
					new byte[mPreviewWidth*mPreviewHeight*3/2],
					new byte[mPreviewWidth*mPreviewHeight*3/2],
					new byte[mPreviewWidth*mPreviewHeight*3/2]
				};
				camera.addCallbackBuffer(mPreviewBuffer[mBufIndex]);
				camera.setPreviewCallbackWithBuffer(this);
			}
		}
		
		if((mDetectType&DETECT_TYPE_SOUND)!=0) {
			mSoundDetectThread = new SoundDetectThread();
			mSoundDetectThread.start();
		}
	}
	
	public void stopDetect() {
		if(mIsStarted && mIsHWDetected) {
			stopHWFaceDetect(mCamera);
		}
		mIsStarted = false;
		mIsAppendToStart = false;
		if(mPreviewDetectThread!=null) {
			mPreviewDetectThread.terminate();
			Util.joinThreadSilently(mPreviewDetectThread);
			mPreviewDetectThread = null;
		}
		if(mSoundDetectThread!=null) {
			mSoundDetectThread.terminate();
			Util.joinThreadSilently(mSoundDetectThread);
			mSoundDetectThread = null;
		}
		for(OnDetectListener l: mlstDetectListener) {
			l.onDetectFace(null);
			l.onDetectGesture(null);
			l.onDetectSmile(null);
			l.onDetectSound(0.0f, 0);
		}
		if(mCamera!=null && !mIsFrameExternal) {
			try {
				mCamera.setPreviewCallbackWithBuffer(null);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		mPreviewBuffer = null;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private boolean isSupportHWFaceDetect(Camera camera) {
//		if(Build.VERSION.SDK_INT<Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//			return false;
//		}
//		Parameters param = camera.getParameters();
//		int fdNum = param.getMaxNumDetectedFaces();
//		if(fdNum>0) {
//			return true;
//		}
		return false;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void startHWFaceDetect(Camera camera) {
		
		camera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
			@Override
			public void onFaceDetection(Face[] faces, Camera camera) {
				//This is in UI thread.
				//Avoid that any of mFaces��s elements is null while gesture detecting.
				synchronized(mLock) {
					mFaces = new Rect[faces.length];
					for(int i=0; i<mFaces.length; i++) {
						mFaces[i] = faces[i].rect;
						RectF temp = new RectF(faces[i].rect);
						mMatHWDetect.mapRect(temp);
						mFaces[i] = new Rect((int)temp.left, (int)temp.top, (int)temp.right, (int)temp.bottom);
					}
				}
				if(mIsStarted) {
					for(OnDetectListener l: mlstDetectListener) {
						l.onDetectFace(mapRects(mFaces));
					}
				}
			}
		});
		camera.startFaceDetection();
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void stopHWFaceDetect(Camera camera) {
		camera.stopFaceDetection();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		if(mPreviewDetectThread!=null) mPreviewDetectThread.onPreviewFrame(data);
	}
	
	private class PreviewDetectThread extends Thread {
		
		private boolean mRunning = false;
		private byte[] mFrame = null;
		private byte[] mDetectBuffer = null;
		
		public void onPreviewFrame(byte[] data) {
			if(data==null) {
				return;
			}
			mFrame = data;
			interrupt();
		}
		
		@Override
		public void start() {
			mRunning = true;
			setName("PreviewDetectThread");
			super.start();
		}
		
		public void terminate() {
			mRunning = false;
			interrupt();
		}
		
		private void ensureDetectBufferSize() {
			int requiredSize = mDetectWidth*mDetectHeight*3/2;
			if(mDetectBuffer==null || mDetectBuffer.length<requiredSize) {
				mDetectBuffer = new byte[requiredSize];
			}
		}
		
		@Override
		public void run() {
			while(mRunning) {
				while(mRunning && mFrame==null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						break;
					}
				}
				if(!mRunning) break;
				byte[] nv21 = mFrame;
				if(nv21==null || nv21.length==0) {
					String len = "null";
					if(nv21!=null) {
						len = String.valueOf(nv21.length);
					}
					LogUtil.logE(TAG, "ERROR: Frame width=%d height=%d nv21 length=%s", mPreviewWidth, mPreviewHeight, len);
					continue;
				}

//				Util.dumpNv21ToJpeg(nv21, mPreviewWidth, mPreviewHeight, "/sdcard/zhl.jpg");
				ensureDetectBufferSize();
//				LogUtil.startLogTime("nv21DownSample");
				mNv21Sampler.downSample(nv21, mPreviewWidth, mPreviewHeight, mDetectBuffer, mDetectSample, mDetectRotation);
//				LogUtil.stopLogTime("nv21DownSample");
				nv21 = mDetectBuffer;
//				Util.dumpToFile(nv21, "/sdcard/zhl_"+mDetectWidth+"x"+mDetectHeight+".nv21");
				Rect[] gestures = null;
				Rect[] smiles = null;
//				Util.dumpNv21ToJpeg(nv21, mDetectWidth, mDetectHeight, "/sdcard/zhl.jpg");
				if(!mIsHWDetected) {
					mFaces = detectFace(nv21,mDetectWidth, mDetectHeight);
					if(mRunning) {
						for(OnDetectListener l: mlstDetectListener) {
							l.onDetectFace(mapRects(mFaces));
						}
					}
				}
				if((mDetectType&DETECT_TYPE_GESTURE)!=0) {
					gestures = detectGesture(nv21, mDetectWidth, mDetectHeight, mFaces);
					if(mRunning) {
						for(OnDetectListener l: mlstDetectListener) {
							l.onDetectGesture(mapRects(gestures));
						}
					}
				}
				if((mDetectType&DETECT_TYPE_SMILE)!=0) {
					if(mIsHWDetected) {
						for(Rect face: mFaces) {
							face.bottom += face.height()/8;
						}
					}
					smiles = detectSmile(nv21, mDetectWidth, mDetectHeight, mFaces);
					if(mRunning) {
						for(OnDetectListener l: mlstDetectListener) {
							l.onDetectSmile(mapRects(smiles));
						}
					}
				}
				

				if(mDumpPath!=null) {
					Util.dumpToFile(nv21, mDumpPath);
					Util.dumpNv21ToJpeg(nv21, mPreviewWidth, mPreviewHeight, mDumpPath+".jpg");
					mDumpPath = null;
					String smile = "null";
					String face = "null";
					String gesture = null;
					if(mFaces!=null && mFaces.length>0) face = mFaces[0].toString();
					if(gestures!=null && gestures.length>0) gesture = gestures[0].toString();
					if(smiles!=null && smiles.length>0) smile = smiles[0].toString();
					LogUtil.logE("dumpPreviewToFile", "face=%s gesture=%s smile=%s", face, gesture, smile);
				}
				
				mFrame = null;
				if(mRunning && !mIsFrameExternal) {
					mBufIndex = (mBufIndex+1)%mPreviewBuffer.length;
					mCamera.addCallbackBuffer(mPreviewBuffer[mBufIndex]);
				}
			}
		}
	}
	
	private class SoundDetectThread extends Thread {
    	
    	private boolean mIsRunning = false;
    	
    	@Override
    	public void start() {
    		mIsRunning = true;
    		setName("AudioDetectThread");
    		super.start();
    	}
    	
    	public void terminate() {
    		mIsRunning = false;
    	}
     	
    	@Override
    	public void run() {
    		int audioSource = AudioSource.MIC;
    		int sampleSize = 44100;
    		int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    		int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    		int bufferSize = AudioRecord.getMinBufferSize(sampleSize, channelConfig, audioFormat);
    		AudioRecord audioRecord = new AudioRecord(audioSource, sampleSize,channelConfig, audioFormat, bufferSize);
    		audioRecord.startRecording();
    		short[] buffer = new short[(bufferSize+1)/2];
    		while(mIsRunning) {
    			int num = audioRecord.read(buffer, 0, buffer.length);
    			float volumn = calcVolumeRMS(buffer, num);
    			if(mIsRunning) {
    				for(OnDetectListener l: mlstDetectListener) {
    					l.onDetectSound(volumn, buffer.length);
    				}
    			}
    		}
    		audioRecord.stop();
    		audioRecord.release();
    	}
    	
    	private float calcVolumeCrest(short[] buffer, int num) {
    		int value0 = 0;
    		int value1 = 0;
    		int value2 = 0;
    		int crestCnt = 0;
    		int crestSum = 0;
    		int crestAvr = 0;
			for(int i=0; i<num; i++) {
				value0 = value1;
				value1 = value2;
				value2 = Math.abs(buffer[i]);
				if(value1>value0 && value1>value2) {
					crestSum += value1;
					crestCnt ++;
				}
			}
			crestAvr = crestSum / crestCnt;
			float volume = (float)crestAvr/Short.MAX_VALUE;
			return volume;
    	}
    	
    	private float calcVolumeRMS(short[] buffer, int num) {
    		if(num==0) {
    			return 0.0f;
    		}
    		long rmsSum = 0;
    		int rmsOne = 0;
    		for(int i=0; i<num; i++) {
    			rmsOne = buffer[i];
    			rmsOne *= rmsOne;
    			rmsSum += rmsOne;
    		}
    		int rmsArg = (int)(rmsSum/num);
    		double rms = Math.sqrt(rmsArg);
    		double volume = rms/Short.MAX_VALUE;
    		return (float)volume;
    	}
    	
    }
	
	private Rect[] mapRects(Rect[] src) {
		if(src==null || src.length<1) {
			return null;
		}
		Rect[] res = new Rect[src.length];
		RectF temp = new RectF();
		for(int i=0; i<src.length; i++) {
			Rect rect = src[i];
			temp.set(rect);
			mMatDisplay.mapRect(temp);
			res[i] = new Rect((int)temp.left, (int)temp.top, (int)temp.right, (int)temp.bottom);
		}
		return res;
	}
	
	private String mDumpPath = null;
	public void dumpPreviewToFile(String path) {
		mDumpPath = path;
	}
}
