package com.anysou.as_remoteservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

public class MyService extends Service {

    private static int notificationID = 100;  //通知ID

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setFrontService();
        Toast.makeText(getApplicationContext(), "=创建成功！", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //onStartCommand 中 手动返回START_STICKY，亲测当service因内存不足被kill，当内存又有的时候，service又被重新创建
        return START_STICKY;
    }

    //设置为前台服务，注意： <!--android 9.0上使用前台服务，需要添加权限-->
    private void setFrontService(){

        // 获取通知管理器
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //新建Builer对象
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("AIDL前台服务");          //设置通知的标题
        builder.setContentText("AIDL前台服务已启动！");    //设置通知的内容
        builder.setSmallIcon(R.drawable.service);        //设置通知的图标 //Android5.0及以上版本通知栏和状态栏不显示彩色图标而都是白色，简单粗暴的方法，降低sdk的目标版本小于21，将android:targetSdkVersion="19"，
        builder.setWhen(System.currentTimeMillis());     //设置时间,long类型自动转换
        builder.setPriority(Notification.PRIORITY_HIGH); //优先级
        builder.setDefaults(Notification.DEFAULT_ALL);

        //兼容  API 16    android 4.1 Jelly Bean    果冻豆
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            builder.setShowWhen(true);      //设置显示通知时间
        }

        // 建立通道，在Android O版本中，发送通知的时候必须要为通知设置通知渠道，否则通知不会被发送。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NotificationChannels.CRITICAL_ID);
        }

        //添加下列三行 构建 "点击通知后打开MainActivity" 的Intent 意图
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);       //设置点击通知后的操作

        Notification notification = builder.getNotification(); //将Builder对象转变成普通的notification

        ////标记不可删除通知
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(notificationID, notification);          //让Service变成前台Service,并在系统的状态栏显示出来
    }


    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(),"=销毁成功！",Toast.LENGTH_LONG).show();

        //删除前台通知
        stopForeground(true); // 停止前台服务--参数：表示是否移除之前的通知
        NotificationManager mNotifyMgr =  (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(notificationID);
        // 重启自己
        Intent intent = new Intent(getApplicationContext(), MyService.class);
        startService(intent);

        super.onDestroy();
    }

    // 获取AIDL接口类，
    private MyAIDL.Stub myAIDL = new MyAIDL.Stub() {
        @Override  //获取App的名称
        public String getgAppName() throws RemoteException {
            return PackageUtils.getAppName(getApplicationContext());
        }

        @Override  //获取版本名称
        public String getVersionName() throws RemoteException {
            return PackageUtils.getVersionName(getApplicationContext());
        }

        @Override  //获取版本号
        public int getVersionCode() throws RemoteException {
            return PackageUtils.getVersionCode(getApplicationContext());
        }
    };


    @Override //绑定服务
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(),"=绑定成功！",Toast.LENGTH_LONG).show();
        return  myAIDL;
    }

    @Override  //解绑服务
    public boolean onUnbind(Intent intent) {
        Toast.makeText(getApplicationContext(),"=解绑成功！",Toast.LENGTH_LONG).show();
        return super.onUnbind(intent);
    }
}
