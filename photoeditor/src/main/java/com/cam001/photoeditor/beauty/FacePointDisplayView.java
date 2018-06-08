package com.cam001.photoeditor.beauty;

import com.cam001.photoeditor.R;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.photoeditor.beauty.engine.EditBitmap;
import com.cam001.photoeditor.beauty.makeup.FacePointUtil;
import com.cam001.photoeditor.beauty.makeup.widget.MagnifierView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class FacePointDisplayView extends ScaledDisplayView{

	public static interface OnFacePointChangeListenter {
		void onFacePointChange(FaceInfo face);
	}
	
	private Bitmap mBmpEye = null;
	private Bitmap mBmpMouth = null;
	
	private FaceInfo mFace = null;
	protected boolean mbEnableEyes = false;
	protected boolean mbEnableMouth = false;
	protected boolean mbShow  = false;
	private Matrix mMatPoint = null;
	
	private float[] mFacePoint = new float[6];
	private boolean mIsLeftEyeDown = false;
	private boolean mIsRightEyeDown = false;
	private boolean mIsMouthDown = false;
	
	protected MagnifierView mZoomer = null;
	private OnFacePointChangeListenter mlFaceChange = null;
	
	public FacePointDisplayView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setOnFacePointChangeListener(OnFacePointChangeListenter l) {
		mlFaceChange = l;
	}
	
	public void enableFacePoint(boolean bEnable) {
		enableFacePoint(bEnable, bEnable);
	}
	
	public void enableFacePoint(boolean bEnableEyes, boolean bEnableMouth) {
		mbEnableEyes = bEnableEyes;
		mbEnableMouth = bEnableMouth;
		showFacePoint(bEnableEyes||bEnableMouth);
	}
	
	public void showFacePoint(boolean bShow) {
		if(bShow) {
			loadResource();
			loadFace();
		}
		mbShow = bShow;
		postInvalidate();
	}
	
	private void loadResource() {
		if(mbEnableEyes && mBmpEye==null) {
			mBmpEye = BitmapFactory.decodeResource(getResources(), R.drawable.posting_eye);
		}
		if(mbEnableMouth && mBmpMouth==null) {
			mBmpMouth = BitmapFactory.decodeResource(getResources(), R.drawable.posting_mouth);
		}
		if(mZoomer==null) {
			mZoomer = new MagnifierView(getContext());
			mZoomer.setDisplayView(this);
			mZoomer.onSizeChanged(getWidth(), getHeight(), 0, 0);
		}
	}
	
	private void loadFace() {
		if(mFace==null) {
			EditBitmap bmp = mEngine.getEditBitmap();
			FaceInfo[] faces = bmp.getFaces();
			if(faces==null || faces.length<1) {
				faces = new FaceInfo[1];
				faces[0] = FacePointUtil.createDefaultFace(bmp.getWidth(), bmp.getHeight());
				bmp.setFaces(faces);
			}
			mFace = faces[0];
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		canvas.save();
		super.onDraw(canvas);
		canvas.restore();
		if(!mbShow) return;
		drawFacePoint(canvas);
		mZoomer.DoDraw(canvas);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if(mZoomer!=null) {
			mZoomer.onSizeChanged(w, h, oldw, oldh);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	private void drawFacePoint(Canvas c) {
		if(mIsLeftEyeDown||mIsRightEyeDown||mIsMouthDown) {
			//Do not modify the points.
		} else {
			mapFaceToPoints();
		}
		float dx,dy;
		if(mbEnableEyes) {
			dx = mBmpEye.getWidth()/2.0f;
			dy = mBmpEye.getHeight()/2.0f;
			c.drawBitmap(mBmpEye, mFacePoint[0]-dx, mFacePoint[1]-dy, null);
			c.drawBitmap(mBmpEye, mFacePoint[2]-dx, mFacePoint[3]-dy, null);
		}
		if(mbEnableMouth) {
			dx = mBmpMouth.getWidth()/2.0f;
			dy = mBmpMouth.getHeight()/2.0f;
			c.drawBitmap(mBmpMouth, mFacePoint[4]-dx, mFacePoint[5]-dy, null);
		}
	}
	
	private void mapFaceToPoints() {
		mMatPoint = new Matrix(mEngine.getImageMatrix());
		mMatPoint.postConcat(mMatCanvas);
		mFacePoint[0] = mFace.eye1.exactCenterX();
		mFacePoint[1] = mFace.eye1.exactCenterY();
		mFacePoint[2] = mFace.eye2.exactCenterX();
		mFacePoint[3] = mFace.eye2.exactCenterY();
		mFacePoint[4] = mFace.mouth.exactCenterX();
		mFacePoint[5] = mFace.mouth.exactCenterY();
		mMatPoint.mapPoints(mFacePoint);
	}
	
	private void mapPointsToFace() {
		Matrix matInverse = new Matrix();
		mMatPoint.invert(matInverse);
		matInverse.mapPoints(mFacePoint);
		int dx, dy;
		dx = (int)(mFacePoint[0] - mFace.eye1.exactCenterX());
		dy = (int)(mFacePoint[1] - mFace.eye1.exactCenterY());
		mFace.eye1.offset(dx, dy);
		dx = (int)(mFacePoint[2] - mFace.eye2.exactCenterX());
		dy = (int)(mFacePoint[3] - mFace.eye2.exactCenterY());
		mFace.eye2.offset(dx, dy);
		dx = (int)(mFacePoint[4] - mFace.mouth.exactCenterX());
		dy = (int)(mFacePoint[5] - mFace.mouth.exactCenterY());
		mFace.mouth.offset(dx, dy);
		mFace.face = FacePointUtil.createFace((int)mFace.eye1.centerX(), 
				(int)mFace.eye1.centerY(), 
				(int)mFace.eye2.centerX(), (int)mFace.eye2.centerY(), 
				(int)mFace.mouth.centerX(), (int)mFace.mouth.centerY())
				.face;
		mFace.needRefreshOutline = true;
	}
	
	private void ensureFaceInBitmap() {
		ensurePointInBitmap(mFace.eye1);
		ensurePointInBitmap(mFace.eye2);
		ensurePointInBitmap(mFace.mouth);
		ensureEyesPosition(mFace);
	}
	
	/**
	 * Ensure the eye point in the range of the bitmap.
	 * @param r
	 */
	private void ensurePointInBitmap(Rect r) {
		int dx, dy;
		int x, y;
		int bmpWidth = mEngine.getEditBitmap().getWidth();
		int bmpHeight = mEngine.getEditBitmap().getHeight();
		x = r.centerX();
		y = r.centerY();
		if(x<0) dx = -x;
		else if(x>bmpWidth) dx = bmpWidth-x;
		else dx = 0;
		if(y<0) dy = -y;
		else if(y>bmpHeight) dy = bmpHeight-y;
		else dy = 0;
		r.offset(dx, dy);
	}
	
	/**
	 * Ensure eye1 in the left and eye 2 in the right of the bitmap.
	 */
	private void ensureEyesPosition(FaceInfo face) {
		if(face.eye1.centerX()>face.eye2.centerX()) {
			Rect tmp = face.eye1;
			face.eye1 = face.eye2;
			face.eye2 = tmp;
		}
	}

	protected boolean handleSingleTouhEvent(MotionEvent event) {
		if (mbShow) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (isPointInLeftEye(event.getX(0), event.getY(0))) {
					mZoomer.setCircleResource(mBmpEye);
					mIsLeftEyeDown = true;
				} else if (isPointInRightEye(event.getX(0), event.getY(0))) {
					mZoomer.setCircleResource(mBmpEye);
					mIsRightEyeDown = true;
				} else if (isPointInMouth(event.getX(0), event.getY(0))) {
					mZoomer.setCircleResource(mBmpMouth);
					mIsMouthDown = true;
				} else {
					break;
				}
				mLastX0 = event.getX(0);
				mLastY0 = event.getY(0);
				mbShow = false;
				mZoomer.DispachTouchEvent(event);
				mbShow = true;
				return true;
			case MotionEvent.ACTION_MOVE:
				if(mIsLeftEyeDown) {
					mFacePoint[0] = event.getX(0);
					mFacePoint[1] = event.getY(0);
				} else if (mIsRightEyeDown) {
					mFacePoint[2] = event.getX(0);
					mFacePoint[3] = event.getY(0);
				} else if (mIsMouthDown) {
					mFacePoint[4] = event.getX(0);
					mFacePoint[5] = event.getY(0);
				} else {
					break;
				}
				mLastX0 = event.getX(0);
				mLastY0 = event.getY(0);
				mZoomer.DispachTouchEvent(event);
				return true;
			case MotionEvent.ACTION_UP:
				if(mIsLeftEyeDown) {
					mIsLeftEyeDown = false;
				} else if(mIsRightEyeDown) {
					mIsRightEyeDown = false;
				} else if(mIsMouthDown) {
					mIsMouthDown = false;
				} else {
					break;
				}
				mapPointsToFace();
				ensureFaceInBitmap();
				mZoomer.DispachTouchEvent(event);
				if(mlFaceChange!=null) {
					mlFaceChange.onFacePointChange(mFace);
				}
				return true;
			}
		}
		return super.handleSingleTouhEvent(event);
	}
	
	@Override
	protected boolean handleSingleClick() {
		if(mbEnableEyes||mbEnableMouth) {
			showFacePoint(!mbShow);
		}
		return true;
	}
	
	private boolean isPointInLeftEye(float x, float y) {
		if(!mbEnableEyes) return false;
		return new RectF(mFacePoint[0]-mBmpEye.getWidth()/2.0f, 
				mFacePoint[1]-mBmpEye.getHeight()/2.0f,
				mFacePoint[0]+mBmpEye.getWidth()/2.0f,
				mFacePoint[1]+mBmpEye.getHeight()/2.0f)
		.contains(x, y);
	}
	
	private boolean isPointInRightEye(float x, float y) {
		if(!mbEnableEyes) return false;
		return new RectF(mFacePoint[2]-mBmpEye.getWidth()/2.0f, 
				mFacePoint[3]-mBmpEye.getHeight()/2.0f,
				mFacePoint[2]+mBmpEye.getWidth()/2.0f,
				mFacePoint[3]+mBmpEye.getHeight()/2.0f)
		.contains(x, y);
	}
	
	private boolean isPointInMouth(float x, float y) {
		if(!mbEnableMouth) return false;
		return new RectF(mFacePoint[4]-mBmpMouth.getWidth()/2.0f, 
				mFacePoint[5]-mBmpMouth.getHeight()/2.0f,
				mFacePoint[4]+mBmpMouth.getWidth()/2.0f,
				mFacePoint[5]+mBmpMouth.getHeight()/2.0f)
		.contains(x, y);
	}
	
}
