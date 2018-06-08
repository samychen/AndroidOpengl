package com.cam001.photoeditor.beauty.detect;


import com.cam001.util.Util;

public class NV21Sampler {

	private int mHandle = 0;
	
	public NV21Sampler() {
		mHandle = native_create();
		Util.Assert(mHandle != 0);
	}
	
	public void destroy() {
		if(mHandle!=0) {
			native_destroy(mHandle);
			mHandle = 0;
		}
	}
	
	public void downSample(byte[] in, int inWidth, int inHeight, byte[] out, int sampleSize, int rotation) {
		native_downSample(mHandle, in, inWidth, inHeight, out, sampleSize, rotation);
	}
	
	@Override
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}	
	
	private static native int native_create();
	private static native void native_destroy(int handle);
	private static native void native_downSample(int handle, byte[] in, int inWidth, int inHeight, byte[] out, int sampleSize, int rotation);
	 
}
