package com.cam001.photoeditor.beauty.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.net.Uri;
import android.view.MotionEvent;

import com.cam001.photoeditor.AppConfig;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.photoeditor.beauty.makeup.widget.MagnifierView;
import com.cam001.service.LogUtil;
import com.cam001.util.BitmapUtil;
import com.cam001.util.CameraUtil;
import com.cam001.util.CompatibilityUtil;
import com.cam001.util.StorageUtil;

public class EditEngine implements Widget{

	public static final int EDIT_MODE_ONEKEY = -1;
	public static final int EDIT_MODE_MAIN = 0;
	public static final int EDIT_MODE_FRAME = 1;
	public static final int EDIT_MODE_STAMP = 2;
	public static final int EDIT_MODE_COVER = 3;
	public static final int EDIT_MODE_FILTER = 4;
	public static final int EDIT_MODE_CROP   = 5;
	public static final int EDIT_MODE_FACEWHITEN = 6;
	public static final int EDIT_MODE_FACETRIM = 7;
	public static final int EDIT_MODE_ENLAGEEYES = 8;
	public static final int EDIT_MODE_BRIGHTEYES = 9;
	public static final int EDIT_MODE_EYECIRCLE = 10;
	public static final int EDIT_MODE_EYEBAG = 11;
	public static final int EDIT_MODE_DEBLEMISH = 12;
	public static final int EDIT_MODE_MOSAIC = 13;
	public static final int EDIT_MODE_FACESOFTEN = 14;
	public static final int EDIT_MODE_FACECOLOR = 15;
	
	private static final String TAG = "EditEngine";
	
	public static final int MAX_IMAGE_WIDTH = 1024;
	public static final int MAX_IMAGE_HEIGHT = 1024;
//	protected static final int RES_STAMP_CTRL = R.drawable.icon_rotate;
//	protected static final int RES_STAMP_DEL = R.drawable.icon_delete;
//	protected static final int RES_STAMP_CPY = R.drawable.icon_copy;
	
	private static EditEngine sInstance = null;
	public static EditEngine getInstance() {
		if(sInstance==null) {
			Context c = AppConfig.getInstance().appContext;
			sInstance = new EditEngine(c);
		}
		return sInstance;
	}
	
	public static void destroy() {
		if(sInstance!=null) {
			sInstance.clearStamp();
			if(sInstance.mEditBmp!=null) {
				sInstance.mEditBmp.recycle();
				sInstance.mEditBmp = null;
			}
			sInstance = null;
		}
	}
	
	protected Context mContext = null;
	
	private RoundFrameWidget mWgtFrame = null;
	private CoverWidget mWgtCover = null;
	private CtrlTransListWidget mWgtStamp = null;
//	private GpuToolLib mWgtFilter = null;
	private boolean mbNeedRefreshFilter = true;
	
	private MagnifierView mMagniferview;
	
	private Matrix mMat = null;
	protected EditBitmap mEditBmp = null;
	private RectF mRectView = null;
	private RectF mRectCanv = null;
	private int mMode = EDIT_MODE_MAIN;
	private boolean mbOriginal = false;
	private boolean mbUseDrawingCache = false;
	
	/**
	 * MUST call loadImage after construction
	 * @param context
	 */
	protected EditEngine(Context context) {
		mContext = context;
		mMat = new Matrix();		
		mWgtFrame = new RoundFrameWidget();
//		Bitmap stampCtrl = BitmapFactory.decodeResource(mContext.getResources(), RES_STAMP_CTRL);
//		Bitmap stampDel = BitmapFactory.decodeResource(mContext.getResources(), RES_STAMP_DEL);
//		Bitmap stampCpy = BitmapFactory.decodeResource(mContext.getResources(), RES_STAMP_CPY);
		mWgtStamp = new CtrlTransListWidget(null, null, null);
		mWgtCover = new CoverWidget();
	}
	
	public boolean loadImage(Uri uri) {
		Bitmap bmp = null;
		if(CompatibilityUtil.low512MMemory())
			bmp = BitmapUtil.getBitmap(uri, mContext, MAX_IMAGE_WIDTH, MAX_IMAGE_WIDTH);
		else
			bmp = BitmapUtil.getBitmap(uri, mContext, 720, 960);
		return loadImage(bmp);
	}
	
	public boolean loadImage(String path) {
		Bitmap bmp = null;
		if(CompatibilityUtil.low512MMemory())
			bmp = BitmapUtil.getBitmap(path, MAX_IMAGE_WIDTH, MAX_IMAGE_WIDTH);
		else
			bmp = BitmapUtil.getBitmap(path, 720, 960);
		return loadImage(bmp);
	}
	
	public boolean loadImage(byte[] jpeg) {
		Bitmap bmp = null;
		if(CompatibilityUtil.low512MMemory())
			bmp = BitmapUtil.getBitmap(jpeg, MAX_IMAGE_WIDTH, MAX_IMAGE_WIDTH);
		else
			bmp = BitmapUtil.getBitmap(jpeg, 720, 960);
		return loadImage(bmp);
	}
	
	public synchronized boolean loadImage(Bitmap bmp) {
		LogUtil.logV(TAG, "loadImage <----- bmp=" + bmp);
		if(bmp==null) {
			LogUtil.logE(TAG, "loadImage failed: bmp=null");
			return false;
		}
		FaceInfo[] faces = null;
		if(mEditBmp!=null) {
			reset(false);
			faces = mEditBmp.getFaces();
			mEditBmp.recycle();
			mEditBmp = null;
			mbUseDrawingCache = false;
		}

		mEditBmp = new EditBitmap(bmp);
		mEditBmp.setFaces(faces);
		mWgtFrame.setDisplayBitmap(mEditBmp.getBitmap());
		refreshWidgetSize();
		mbNeedRefreshFilter = true;

		LogUtil.logV(TAG, "loadImage ----->");
		return true;
	}
	
	public void setDispViewSize(int width, int height) {
		LogUtil.logV(TAG, "setDispViewSize %d,%d", width, height);
		mRectView = new RectF(0,0,width,height);
		refreshWidgetSize();
	}
	
	private void refreshWidgetSize() {
		if(mRectView==null || mEditBmp==null) {
			return;
		}
		mRectCanv = new RectF(0,0, mEditBmp.getWidth(),mEditBmp.getHeight());
		mMat.reset();
		mMat.setRectToRect(mRectCanv,mRectView, ScaleToFit.CENTER);
		mMat.mapRect(mRectCanv);
		mWgtStamp.setImageDispRect(mRectCanv);
		mWgtFrame.setDisplayRect(mRectCanv);
		mWgtStamp.setCtrlDispRect(mRectView);
		mWgtCover.setCoverRect(mRectCanv);
	}
	
	public void setEditMode(int mode) {
		if(mode==mMode) {
			return;
		}
		LogUtil.logV(TAG, "setEditMode -----< mode=%d", mode);
		//Go to STAMP mode.
		//To improve stamp performance, use cache.
		if(mode==EDIT_MODE_STAMP) {
			useDrawingCache();
		} else {
			clearDrawingCache();
		}
		//Leave from STAMP mode. Hide stamp controller first.
		if(mMode==EDIT_MODE_STAMP) {
			showStampCtrl(false);
		}
		mMode = mode;
		LogUtil.logV(TAG, "setEditMode ----->");
	}
	
	/**
	 * Create drawing cache of frame, to improve stamp performance.
	 * @return
	 */
	private void useDrawingCache() {
		if(mbUseDrawingCache) {
			return;
		}
		LogUtil.startLogTime("useDrawingCache");
		//To save VM size, should save cache to EditBimap which is in JNI.
		mEditBmp.save();
		int width = mEditBmp.getWidth();
		int height = mEditBmp.getHeight();
		//MUST create a new bitmap, or the bitmap may fill with frame.
		Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Matrix invert = new Matrix();
		mMat.invert(invert);
		canvas.setMatrix(invert);
		mWgtFrame.draw(canvas);
		mWgtCover.draw(canvas);
		apply(bmp);
		bmp.recycle();
		mbUseDrawingCache = true;
		LogUtil.stopLogTime("useDrawingCache");
	}
	
	private void clearDrawingCache() {
		if(!mbUseDrawingCache) {
			return;
		}
		LogUtil.startLogTime("clearDrawingCache");
		mEditBmp.restore();
		mbUseDrawingCache = false;
		LogUtil.stopLogTime("clearDrawingCache");
	}
	
	/**
	 * Specify the round frame bitmap.
	 * NOTE: The frame bitmap will be tiled repeatedly.
	 * @param resid
	 */
	public void setFrame(int resid) {
		Bitmap frame = BitmapFactory.decodeResource(mContext.getResources(), resid);
		setFrame(frame);
	}
	
	public void setFrame(Bitmap bmp) {
		mWgtFrame.setFrameBitmap(bmp);
	}
	
	public Bitmap getFrame() {
		return mWgtFrame.getFrameBitmap();
	}
	
	/**
	 * Set weight & radius for the round frame.
	 * @param weight border width.
	 * @param radius round radius.
	 */
	public void setFrameBorder(int weight, int radius) {
		mWgtFrame.setFrameBorder(weight, radius);
	}
	
	public void setFrameBorderWight(int weight) {
		mWgtFrame.setFrameBorderWeight(weight);
	}
	
	public void setFrameBorderRadius(int radius) {
		mWgtFrame.setFrameBorderRadius(radius);
	}
	
	public int getFrameBorderWight() {
		return mWgtFrame.getFrameBorderWeight();
	}
	
	public int getFrameBorderRadius() {
		return mWgtFrame.getFrameBorderRadius();
	}
	
	/**
	 * Put a bitmap covering the edited bitmap. (XY-Fit)
	 * @param resid
	 */
	public void setCover(int resid) {
		Bitmap cover = BitmapFactory.decodeResource(mContext.getResources(), resid);
		setCover(cover);
	}
	
	public void setCover(Bitmap cover) {
		mWgtCover.setCover(cover);
	}
	
	public void setCover(Bitmap cover, ScaleToFit stf) {
		mWgtCover.setCover(cover);
		if(cover==null) return;
		RectF bmpRect = new RectF(0, 0, cover.getWidth(), cover.getHeight());
		Matrix mat = new Matrix();
		mat.setRectToRect(bmpRect, mRectCanv, stf);
		mat.mapRect(bmpRect);
		mWgtCover.setCoverRect(bmpRect);
	}
	
	public Bitmap getCover() {
		return mWgtCover.getCover();
	}
	
//	public void setFilter(int filterid, Bitmap[] res, boolean needResScale) {
//		if(mWgtFilter==null) {
//			mWgtFilter = new GpuToolLib();
//			mWgtFilter.initGLFilter();
//		}
//		if(mbNeedRefreshFilter) {
//			mWgtFilter.setImage(mEditBmp.getBitmap());
//			mbNeedRefreshFilter = false;
//		}
////		if(filterid==14) {
//			int bmpWidth = mEditBmp.getBitmap().getWidth();
//			int bmpHeight = mEditBmp.getBitmap().getHeight();
//			for(int i=0; i<res.length; i++) {
//				Bitmap old = res[i];
//				int oldWidth = old.getWidth();
//				int oldHeight = old.getHeight();
//				
//				if(needResScale) {
//					int newWidth, newHeight;
//					if(bmpWidth*oldHeight>oldWidth*bmpHeight) {
//						newWidth = oldWidth;
//						newHeight = newWidth*bmpHeight/bmpWidth;
//					} else { 
//						newHeight = oldHeight;
//						newWidth = newHeight*bmpWidth/bmpHeight;
//					}
//					int x = (oldWidth-newWidth)/2;
//					int y = (oldHeight-newHeight)/2;
////					res[i] = Bitmap.createBitmap(old, x, y, newWidth, newHeight);
//					res[i] = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);
//					Canvas c = new Canvas(res[i]);
//					c.drawBitmap(old, -x, -y, null);
//					old.recycle();
//				}
//			}
////		} 
//		mWgtFilter.getImage(filterid,0, mEditBmp.getBitmap(), res);
//	}
	
	public void addStamp(int resid) {
		Bitmap stamp = BitmapFactory.decodeResource(mContext.getResources(), resid);
		addStamp(stamp);
	}
	
	public void addStamp(Bitmap stamp) {
		float density = mContext.getResources().getDisplayMetrics().density;
		float scale = density/3.0f;
		mWgtStamp.addWidget(stamp,scale); 
	}
	
	/**
	 * Show the stamp controller which you can use it to move/scale/rotate/delete.
	 * @param bShow
	 */
	public void showStampCtrl(boolean bShow) {
		mWgtStamp.showCtrl(bShow);
	}
	
	/**
	 * Remove the top stamp in the stamp list.
	 * If there are no stamps, do nothing. 
	 */
	public void removeStamp() {
		if(mWgtStamp==null) {
			return;
		}
		mWgtStamp.removeTopWidget();
	}
	
	public int getStampCount() {
		if(mWgtStamp==null) {
			return 0;
		}
		return mWgtStamp.getCount();
	}
	
	/**
	 * Remove all stamps.
	 */
	public void clearStamp() {
		mWgtStamp.reset();
	}
	
	public void setMagnifier(MagnifierView view)
	{
		mMagniferview = view;
	}
	
	/**
	 * Temporarily show the original bitmap.
	 * @param bOriginal
	 */
	public void showOriginal(boolean bOriginal) {
		mEditBmp.setToOriginal(bOriginal);
		mbOriginal = bOriginal;
	}
	
	public EditBitmap getEditBitmap() {
		return mEditBmp;
	}
	
	public Matrix getImageMatrix() {
		return mMat;
	}
	
	/**
	 * Apply the current edited bitmap.
	 */
	public void apply() {
		//TODO: FIX ME
		throw new RuntimeException("Unimplements.");
	}
	
	/**
	 * Apply the current edited bitmap to specified bitmap.
	 * @param bmp
	 */
	public synchronized void apply(Bitmap bmp) {
		mEditBmp.apply(bmp);
		mbNeedRefreshFilter = true;
	}
	
	/**
	 * Reset the edited bitmap to original.
	 */
	public void reset() {
		reset(true);
	}
	
	private void reset(boolean resetBmp) {
		LogUtil.logV(TAG, "reset <----- resetBmp="+resetBmp);
		if(resetBmp) {
			mEditBmp.reset();
		}
		mWgtStamp.reset();
		mWgtFrame.reset();
		mWgtCover.reset();
		LogUtil.logV(TAG, "reset ----->");
	}
	
	/**
	 * Save the edited bitmap to storage.
	 * @return
	 */
	public String savePath;
	public Uri saveToUri() {
		LogUtil.logV(TAG, "save -----<");
		LogUtil.startLogTime("save");
		Bitmap bmp = saveToBitmap();
		long dateTaken = System.currentTimeMillis();
		savePath = CameraUtil.createJpegPath(dateTaken);
        StorageUtil.ensureOSXCompatible();
        Uri uri = StorageUtil.addImage(mContext.getContentResolver(), savePath, dateTaken,
                null, 0, bmp);
        if (uri != null) {
            CameraUtil.broadcastNewPicture(mContext, uri);
        }
        LogUtil.stopLogTime("save");
        LogUtil.logV(TAG, "save ----->Path=%s Uri=%s", savePath, uri.toString());
        return uri;
	}
	
	public Bitmap saveToBitmap() {
		int width = mEditBmp.getWidth();
		int height = mEditBmp.getHeight();
		Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Matrix invert = new Matrix();
		mMat.invert(invert);
		canvas.setMatrix(invert);
		mWgtFrame.draw(canvas);
		mWgtCover.draw(canvas);
		mWgtStamp.showCtrl(false);
		mWgtStamp.draw(canvas);
		return bmp;
	}
	
	@Override
	public synchronized void draw(Canvas canvas) {
		long t1 = System.currentTimeMillis();
		if(mbOriginal) {
			canvas.drawBitmap(mEditBmp.getBitmap(), mMat, null);
			return;
		}
		if(mbUseDrawingCache) {
			Bitmap cache = mEditBmp.getBitmap();
			if(cache!=null) canvas.drawBitmap(cache, mMat, null);
		} else {
			mWgtFrame.draw(canvas);
			mWgtCover.draw(canvas);
		}
		long t2 = System.currentTimeMillis(); 
		mWgtStamp.draw(canvas);
		long t3 = System.currentTimeMillis();
		System.out.println("draw frame cost:"+(t2-t1)+", stamp cost:"+(t3-t2));
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch(mMode) {
		case EDIT_MODE_STAMP:
			if(!mWgtStamp.dispatchTouchEvent(event)) {
				showStampCtrl(false);
			}
			return true;
		case EDIT_MODE_DEBLEMISH:
			return mMagniferview.DispachTouchEvent(event);
		default:
			break;
		}
		return false;
	}

	
	public void convertScreenPointToPicture(float[] pts)
	{
		Matrix invert = new Matrix();
		mMat.invert(invert);
		
		invert.mapPoints(pts);
	}
	
	public boolean isModified() {
		return (mWgtFrame!=null&&mWgtFrame.getFrameBitmap()!=null) || 
				(mWgtCover!=null&&mWgtCover.getCover()!= null) ||
				(mWgtStamp!=null&&mWgtStamp.getCount()>0);
//				||(mWgtFilter!=null&&mWgtFilter.getCurrentMethod()>0&&!mbNeedRefreshFilter);
	}
}