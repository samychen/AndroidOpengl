package com.cam001.photoeditor;

import android.app.Application;

import com.cam001.service.AdAgent;
import com.cam001.stat.StatApi;
import com.cam001.util.CompatibilityUtil;

/**
 * Created by hzhao on 2015/6/2.
 */
public class MainApplication extends Application {

    public static final String TAG = "MainApplication";


    static {
        System.loadLibrary("bspatch");
        System.loadLibrary("Jnijpegdecode");
        System.loadLibrary("tsutils");
        System.loadLibrary("makeupengine");
        System.loadLibrary("tsmakeuprt_jni");

//            try {
////                System.loadLibrary("stlport_shared");
////                System.loadLibrary("FacialOutline");
////                System.loadLibrary("self_portrait_jni");
//                System.loadLibrary("makeupengine");
//            } catch (UnsatisfiedLinkError e) {
//                String path = "/data/thundersoft/beauty/";
////                System.load(path+"libstlport_shared.so");
////                System.load(path+"libFacialOutline.so");
////                System.load(path+"libself_portrait_jni.so");
//                System.load(path+"libmakeupengine.so");
//            }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppConfig.getInstance().appContext = getApplicationContext();
        // Thread.setDefaultUncaughtExceptionHandler(mExpHandler);
        StatApi.init(this);
        StatApi.setDebugMode(true);

        String chl = CompatibilityUtil.getUMengChannel(getApplicationContext());
        AdAgent.instance().init(getApplicationContext(), chl);
        AdAgent.instance().register();
    }

}