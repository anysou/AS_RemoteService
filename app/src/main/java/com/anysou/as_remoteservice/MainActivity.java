package com.anysou.as_remoteservice;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;


/***
 * 本地服务 --local service：服务运行在当前的应用程序里面
 * 远程服务 --remote service：服务运行在其他的应用程序里面
 * * 进程：所谓的进程就是指系统给每一个应用程序分配的一块独立的内存工作空间
 * * IPC：inter process communication  进程间通讯
 * * AIDL：andrid interface definition language 安卓接口定义语言
 *
 * 远程服务：使用 AIDL(Android Interfice Definition Language) 安卓接口定义语言进行 IPC(Inter Process Communication)跨进程通信。
 * Android：（本地、可通信的、前台、远程）Service使用全面介绍  https://www.jianshu.com/p/e04c4239b07e
 * Android调用远程服务中的方法（AIDL） https://blog.csdn.net/nongminkouhao/article/details/88984299
 *
 * 一、定义接口文件名： New -> AIDL -> “MyAIDL” -> Finish -> 会自动产生已个 aidl/包名文件夹/MyAIDL.aidl 接口文件
 * 二、定义接口文件方法： 在 MyAIDL.aidl 里 可以添加一些自己要的接口方法，本文中添加获取本远程服务器的APP名、版本名、版本号. 然后点 Build -> Make Project
 * 三、创建服务：New -> service -> service -> “MyServer” -> Finish -> 会产生 MyServer.java
 * 四、完善服务功能： 在 MyServer.java 文件里会自动生成 onBind 方法，这就是用于客户端 绑定服务的入口。关键：定义引用继承 AIDL 接口类 Stub。
 *     1) private MyAIDL.Stub myAIDL = new MyAIDL.Stub()  ； 同时会自动生成 AIDL文件中定义的方法。
 *     2) onBind 将定义的 AIDL实例 myAIDL 返回： return  myAIDL;
 * 五、完善清单文件： AndroidManifest.xml 。
 *     1) 在建立 MyServer文件是，已自动产生了 <service  android:name=".MyService"  android:enabled="true" //会被系统默认启动  android:exported="true" //设置可被其他进程调用></service>
 *     2）添加隐式调用的 action,注意 name 可任意定义，一般为包名+文件名； <intent-filter>   <action android:name="com.anysou.as_remoteservice.MyAidlService"/>  </intent-filter>
 *     3）android:process=":remote" //设置进程名,名字可以任意字符串。:xxx表示是本包的私有进程。目的与Activity不是同一个进程。
 * 六、服务器本 APP的显示方式：
 *     方法一： 有 MainActivity 界面，有四个按键功能。
 *     方法二： 将 MyService 服务变成“前台服务”； 在onCreate() 添加 startForeground(1, notification);，有四个按键功能。
 *     方法三： 不显示 MainActivity Layout界面，完成 MyService 启动后，就关闭 finish Activity。
 *             好处：不会因手动关闭 MainActivity 界面，而关闭服务。
 *
 *     实现方法，修改清单文件 AndroidManifest.xml：
 *     1）将 <category android:name="android.intent.category.LAUNCHER" /> 从 MainActivity 移到 MyService里；
 *     2）取消：变为前台服务通知里的调用 MainActivity操作功能：  builer.setContentIntent(pendingIntent);  //设置点击通知后的操作
 *     3）优化为不死服务：
 *        1】清单文件里 application 增加： android:persistent="true" 实现常驻应用/服务：在系统刚起来的时候，该App也会被启动起来。
 *        2】清单文件里 application 的 MyService 的 intent-filter 里添加优先级别 android:priority="1000"
 *        3】在 MyService.java里的 onStartCommand 中 手动返回START_STICKY，亲测当service因内存不足被kill，当内存又有的时候，service又被重新创建
 *        4】在 MyService.java里的 onDestroy 中  重启自己：Intent intent = new Intent(getApplicationContext(), MyService.class);  startService(intent);
 *        5】开机自启动功能： AutoReceiver.java (目前有个问题，开机不能自启动？？)
 *
 *
 * 客户端实现步骤：
 * 一、创建客户端 “callservice” 的 Project或 Module，并将服务端的 aidl文件夹整个拷贝到客户端的 main文件夹下；并 Build -> Make Project
 * 二、客户端界面放置 "绑定服务"、“调用服务里的方法”、“解绑服务” 按键。
 * 三、关键点：创建服务器连接类  public ServiceConnection serviceConnection = new ServiceConnection()
 *
 * https://blog.csdn.net/nuonuonuonuonuo/article/details/90052735
 */
public class MainActivity extends AppCompatActivity {

    private static SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏状态下显示
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //解锁
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //保持屏幕长亮
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON); //打开屏幕

        //=== 通过 轻量级的xml存储类 记录数据
        sp = getSharedPreferences("TEST", Context.MODE_PRIVATE);
        //sp = getSharedPreferences("TEST", Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("name","my name is anysou");  //通过editor对象写入数据
        edit.apply();  //提交数据存入到xml文件中


        //killActivityIcon(); //删除桌面上的APP图标
        //finish();  // 关键
    }

    // setComponentEnabledSetting 方法 启动服务组件 （如果本CLASS服务不是前台服务，只能通过这个方法来启动 NLService）
    private void killActivityIcon() {
        ComponentName thisComponent = new ComponentName(this, MainActivity.class);
        PackageManager pm = getPackageManager();
        /**componentName：组件名称（本例是服务组件。如果是app的mainActivity组件，可实现程序图标隐藏）
           newState：组件新的状态，可以设置三个值，分别是如下： 不可用状态：COMPONENT_ENABLED_STATE_DISABLED 
                   可用状态：COMPONENT_ENABLED_STATE_ENABLED ； 默认状态：COMPONENT_ENABLED_STATE_DEFAULT 
                   flags:行为标签，值可以是DONT_KILL_APP或者0。 0说明杀死包含该组件的app
         **/
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        //pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    //启动服务
    public void startServer(View view) {
        //构建启动服务的Intent对象
        Intent startIntent = new Intent(this, MyService.class);
        //调用startService()方法-传入Intent对象,以此启动服务
        startService(startIntent);
    }

    //停止服务
    public void stopServer(View view) {
        //构建停止服务的Intent对象
        Intent stopIntent = new Intent(this, MyService.class);
        //调用stopService()方法-传入Intent对象,以此停止服务
        stopService(stopIntent);
    }


    //创建ServiceConnection的匿名类, 在Activity与Service建立关联和解除关联的时候调用
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        //在Activity与Service解除关联的时候调用
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    //绑定服务
    public void BindServer(View view) {
        //构建绑定服务的Intent对象
        Intent bindIntent = new Intent(this, MyService.class);
        //调用bindService()方法,以此绑定服务
        //第一个参数:Intent对象; 第二个参数:上面创建的Serviceconnection实例
        //第三个参数:标志位; 这里传入BIND_AUTO_CREATE表示在Activity和Service建立关联后自动创建Service
        //这会使得MyService中的onCreate()方法得到执行，但onStartCommand()方法不会执行
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
    }

    //解绑服务
    public void UnBindServer(View view) {
        //调用unbindService()解绑服务
        //参数是上面创建的Serviceconnection实例
        try {
            unbindService(connection);
        } catch ( Exception e){
            sendToast("没有建立服务，不存在解绑！");
        }
    }

    // 读取存储的数据
    public void readname(View view) {
        String name = getString("ok");
        sendToast("共享获取数据："+name);
    }


    //供Test4调用的静态方法
    public static String getStaticString(String str,int n){
        return str+n+readspstatic();
    }
    //供Test4调用的非静态方法
    public String getString(String str){
        return str+"："+readsp();
    }

    public String readsp(){
        return sp.getString("name","没读到");
    }

    public static String readspstatic(){
        return sp.getString("name","没读到");
    }

    // 发送给吐司
    private void sendToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }
}
