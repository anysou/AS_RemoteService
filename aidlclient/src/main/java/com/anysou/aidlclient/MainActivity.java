package com.anysou.aidlclient;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anysou.as_remoteservice.MyAIDL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private MyAIDL myAIDL;   //定义远程服务的AIDL接口
    private Intent intent;   //定义意图
    private final String serviceAction = "com.anysou.as_remoteservice.MyAidlService"; //调用远程服务的Action名，与服务器的清单文件设置的一致
    private final String servicePackage = "com.anysou.as_remoteservice";  //调用远程服务的包名一致
    private final String ServicePackageClass = "com.anysou.as_remoteservice.MainActivity";  //调用远程服务器的类
    private final String MethodStatic = "getStaticString";
    private final String Method = "getString";
    private Context ServiceContext = null;
    private Class ServiceClass = null;
    private String msg = "";


    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        intent = new Intent();
        //由于是隐式启动Service 所以要添加对应的action，A和之前服务端的一样。
        intent.setAction(serviceAction);
        //android 5.0以后直设置action不能启动相应的服务，需要设置packageName或者Component。
        intent.setPackage(servicePackage); //packageName 需要和服务端的一致.

        //判断服务器是否和自己是同一个uid
        try {
            //获取UID，验证是否相同
            PackageManager pm = getPackageManager();
            ApplicationInfo myAI = pm.getApplicationInfo(getPackageName(), 0);
            ApplicationInfo serviceAI = pm.getApplicationInfo(servicePackage, 0);
            msg = "myAI="+Integer.toString(myAI.uid,10)+"，serviceAI="+Integer.toString(serviceAI.uid,10);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            msg = e.toString();
        }

        try {
            // 通过远程服务器APP包名，获取该上下文
            ServiceContext = this.createPackageContext(servicePackage,CONTEXT_INCLUDE_CODE|Context.CONTEXT_IGNORE_SECURITY);
            ServiceClass = ServiceContext.getClassLoader().loadClass(ServicePackageClass); // 获取类
        } catch (Exception e) {
            msg = msg+"\n"+e.toString();
        }
        textView.setText(msg);
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

    //调用服务器APP的静态方法
    public void callstatic(View view) {
        try {
            //获取方法：getMethod方法中参数，第一个是方法名，第二个是方法名中的参数类型
            Method method = ServiceClass.getDeclaredMethod(MethodStatic,String.class,int.class);
            Object object = ServiceClass.newInstance();  //可以根据传入的参数,调用任意构造构造函数
            msg = ((String)method.invoke(object,"调用静态方法成功",100));
            textView.setText(msg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            msg = msg+"\n"+e.toString();
            textView.setText(msg);
        }
    }

    //调用服务器APP的非静态方法
    public void call(View view) {

        try {
            //获取方法：getMethod方法中参数，第一个是方法名，第二个是方法名中的参数类型
            Method method = ServiceClass.getDeclaredMethod(Method,String.class);
            Object object = ServiceClass.newInstance();  //可以根据传入的参数,调用任意构造构造函数
            msg = ((String)method.invoke(object,"调用静态非方法成功"));
            textView.setText(msg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            msg = msg+"\n"+e.toString();
            textView.setText(msg);
        }
    }



    // 发送给吐司
    private void sendToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }



}
