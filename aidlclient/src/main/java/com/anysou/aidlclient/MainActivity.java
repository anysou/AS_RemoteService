package com.anysou.aidlclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import com.anysou.as_remoteservice.MyAIDL;

public class MainActivity extends AppCompatActivity {

    private MyAIDL myAIDL;   //定义远程服务的AIDL接口
    private Intent intent;   //定义意图
    private String serviceAction = "com.anysou.as_remoteservice.MyAidlService"; //调用远程服务的Action名，与服务器的清单文件设置的一致
    private String servicePackage = "com.anysou.as_remoteservice";  //调用远程服务的包名一致

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent();
        //由于是隐式启动Service 所以要添加对应的action，A和之前服务端的一样。
        intent.setAction(serviceAction);
        //android 5.0以后直设置action不能启动相应的服务，需要设置packageName或者Component。
        intent.setPackage(servicePackage); //packageName 需要和服务端的一致.
    }

    //创建服务器连接类
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myAIDL = MyAIDL.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };



    //绑定服务
    public void BindServer(View view) {
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        sendToast("绑定服务成功!");
    }
    //解绑服务
    public void UnBindServer(View view)  {
        try {
            unbindService(serviceConnection);
            sendToast("解绑服务成功!");
        } catch (Exception e){
            sendToast("服务未建立、没法解绑!");
        }
    }
    //调用服务方法
    public void Call(View view) throws RemoteException {
        try {
            String appname = myAIDL.getgAppName();
            String versionName = myAIDL.getVersionName();
            int versionCode = myAIDL.getVersionCode();
            sendToast(appname + "：版本名=" + versionName + " 版本好=" + versionCode);
        }catch (Exception e){
            sendToast("服务已销毁、无法调用方法!");
        }
    }

    // 发送给吐司
    private void sendToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }
}
