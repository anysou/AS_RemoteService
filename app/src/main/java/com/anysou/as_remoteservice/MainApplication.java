package com.anysou.as_remoteservice;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Process;
import android.widget.Toast;

import java.util.List;

/***
 * 1、将用于将服务变为前台服务发通知所需要的通道，先注册。
 * 2、启动 MyServer服务。
 */
public class MainApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // 兼容  API 26，Android 8.0; 建立通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannels.createAllNotificationChannels(this);
        }
        RunMyServer();
    }

    private void RunMyServer(){
        //构建启动服务的Intent对象
        Intent startIntent = new Intent(this, MyService.class);
        //调用startService()方法-传入Intent对象,以此启动服务
        startService(startIntent);
    }

}
