package com.learnopengles.android;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by 000 on 2018/4/13.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
// ��ȡ��ǰ����
        String packageName = context.getPackageName();
// ��ȡ��ǰ������
        String processName = getProcessName(android.os.Process.myPid());
// �����Ƿ�Ϊ�ϱ�����
//        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
//        strategy.setUploadProcess(processName == null || processName.equals(packageName));
//        CrashReport.initCrashReport(getApplicationContext(), "633c8ddb77", false);
        //����
//        CrashReport.testJavaCrash();
    }
    /**
     * ��ȡ���̺Ŷ�Ӧ�Ľ�����
     *
     * @param pid ���̺�
     * @return ������
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
