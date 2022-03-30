package com.media.core.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;


import com.media.rtc.MediaSDK;

import java.util.List;
import java.util.Objects;

public class MyApplication extends Application {
    private static MyApplication myApplication;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        super.onCreate();
        if (Objects.equals(this.getProcessName(this), this.getPackageName())) {
            //获取Context
            myApplication = this;
            //初始化sdk
            MediaSDK.init(this);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    //返回 全局的Context
    public static Context context() {
        return myApplication;
    }

    private String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }
}
