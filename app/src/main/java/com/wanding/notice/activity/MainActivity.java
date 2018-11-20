package com.wanding.notice.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.wanding.notice.R;
import com.wanding.notice.application.BaseApplication;
import com.wanding.notice.fragment.MainHomeFragment;
import com.wanding.notice.fragment.MainMyFragment;
import com.wanding.notice.fragment.MainStatisFragment;
import com.wanding.notice.getui.PushNoticeService;
import com.wanding.notice.update.util.DownLoadAsyncTask;
import com.wanding.notice.update.util.HttpURLConUtil;
import com.wanding.notice.update.util.UpdateInfo;
import com.wanding.notice.update.util.UpdateUrl;
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.Utils;
import com.wanding.notice.view.ControlScrollViewPager;

import java.io.File;


public class MainActivity extends FragmentActivity implements ViewPager.OnPageChangeListener,View.OnClickListener{

    private Context context;
    private static final String TAG = "MainActivity";
    private ControlScrollViewPager mViewPager;
    private RelativeLayout homeLayout, statisLayout,myLayout;//首页，统计，我的
    private ImageView homeImg, statisImg,myImg;
    private TextView homeText, statisText,myText;

    /** mViewPager适配器 */
    private ViewPagerAdapter mAdapter;
    /** fragment 首页，设置*/
    private MainHomeFragment homeFragment;
//    private MainStatisFragment statisFragment;
    private MainMyFragment myFragmnet;

    public Activity activity;
    private UpdateInfo info;
    private Dialog mDialog;


    private static final int REQUEST_PERMISSION = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        BaseApplication.getInstance().addActivity(this);
        //注册权限以及初始化推送服务
        registerPermission();



        initView();
        initFragment();

        //初始化Push（该PUS负责接收各种消息）
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(),com.wanding.notice.getui.PushNoticeIntentService.class);
        // cpu 架构
        Log.e("Tag", "cpu arch = " + (Build.VERSION.SDK_INT < 21 ? Build.CPU_ABI : Build.SUPPORTED_ABIS[0]));

        // 检查 so 是否存在
        File file = new File(this.getApplicationInfo().nativeLibraryDir + File.separator + "libgetuiext2.so");
        Log.e("Tag", "libgetuiext2.so exist = " + file.exists());



        /**
         * 比对版本号/读取更新信息/下载APK/安装
         */
        CheckVersionTask();
    }

    /*
     * 从服务器获取xml解析并进行版本号比对
     */
    private void CheckVersionTask(){


        new Thread(){
            public void run() {

                String versionName = "";
                try {
                    versionName = Utils.getVersionName(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //获取服务器保存版本信息的路径
                String path = "";
                if(NitConfig.isFormal){
                    path = UpdateUrl.url;
                    Log.e("更新版本地址：","生产环境-----");
                }else{
                    path = UpdateUrl.testUrl;
                    Log.e("更新版本地址：","测试环境-----");
                }

                //解析xml文件封装成对象
                info =  HttpURLConUtil.getUpdateInfo(path);
                Log.i(TAG,"版本号为："+info.getVersion());
                Log.i(TAG,"下载路径为："+info.getUrl());
                String xmlVersionName = info.getVersion();
                if(xmlVersionName.equals(versionName)){
                    Log.i(TAG,"版本号相同无需升级");

                }else{
                    Log.i(TAG,"版本号不同 ,提示用户升级 ");
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }

            };

        }.start();

    }

    private void sendMessage(int what,String text){
        Message msg = new Message();
        msg.what = what;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //对话框提示用户升级程序
                    showUpdateDialog();
                    break;
                case 2://安装新版本
                    update();
                    break;

            }
        }
    };

    /**
     * 安装apk
     */
    void update() {


        Intent intent = new Intent(Intent.ACTION_VIEW);
    	 	 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"noticeapp.apk")),
                "application/vnd.android.package-archive");
        startActivity(intent);
        BaseApplication.getInstance().exit();

        /**
         * 程序的安装请注意：默认是不支持安装非市场程序的 因此判断一下
         * 下面是界面设置变动修改的settings信息。1是允许 0是不允许
         */
        int result = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        System.out.println("安装是否允许："+result);
        if (result == 0) {
            /**
             * 在这里可以自定义Dialog解除设置未知源(代替进入系统设置界面更改的操作麻烦)
             * dialog上添加允许安装此应用（代码执行：Settings.Global.putInt(getContentResolver(),Settings.Global.INSTALL_NON_MARKET_APPS,true?1:0);
             * 并添加相关权限
             * <user-permission Android:name="android.permission.WRITE_SCURE_SETTINGS"/>
             * <user-permission android:name="android.permission.WRITE_SETTINGS"/>
             * ）
             */
            System.out.println("禁止安装未知来源");
        }else{

        }
    }

    /**
     * 弹出版本升级提示框
     */
    private void showUpdateDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.app_update_hint_dialog, null);
        //版本号：
        TextView tvVersion=(TextView) view.findViewById(R.id.app_update_hint_tvVersion);
        tvVersion.setText("v"+info.getVersion());
        //描述信息

        //进度条
        final ProgressBar mProgressBar = view.findViewById(R.id.app_update_hint_progressBar);
        RelativeLayout layoutMsg = view.findViewById(R.id.app_update_hint_layoutMsg);
        mProgressBar.setVisibility(View.INVISIBLE);
        layoutMsg.setVisibility(View.INVISIBLE);
        //操作按钮
        final Button btUpdate = (Button) view.findViewById(R.id.app_update_hint_btUpdate);
        mDialog = new Dialog(this,R.style.dialog);
        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
        dialogWindow.setAttributes(params);
        mDialog.setContentView(view);
        btUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                btUpdate.setText("正在下载");
                downFile();

                mDialog.dismiss();

            }
        });
        //点击屏幕和物理返回键dialog不消失
        mDialog.setCancelable(false);
        mDialog.show();
    }

    /**
     * 开始下载
     */
    private void downFile(){
        //开始下载
        DownLoadAsyncTask downLoad=new DownLoadAsyncTask(context, handler,info);
        downLoad.execute(info.getUrl());
    }

    /**
     * 初始化控件
     */
    private void initView() {
        context = MainActivity.this;
        activity = MainActivity.this;
        mViewPager = findViewById(R.id.main_activity_mViewPager);
        homeLayout = findViewById(R.id.main_buttom_tab_homeLayout);
        statisLayout = findViewById(R.id.main_buttom_tab_statisLayout);
        myLayout = findViewById(R.id.main_buttom_tab_myLayout);
        homeImg = findViewById(R.id.main_buttom_tab_homeImg);
        statisImg = findViewById(R.id.main_buttom_tab_statisImg);
        myImg = findViewById(R.id.main_buttom_tab_myImg);
        homeText = findViewById(R.id.main_buttom_tab_homeText);
        statisText = findViewById(R.id.main_buttom_tab_statisText);
        myText = findViewById(R.id.main_buttom_tab_myText);

        //注册Viewpager滑动监听事件
        mViewPager.addOnPageChangeListener(this);
        //注册tab点击时间
        homeLayout.setOnClickListener(this);
        statisLayout.setOnClickListener(this);
        myLayout.setOnClickListener(this);
    }

    /** 初始化Fragment */
    private void initFragment(){
        //初始化fragment
        homeFragment = new MainHomeFragment();
//        statisFragment = new MainStatisFragment();
        myFragmnet = new MainMyFragment();
        //初始化Adapter
        mAdapter=new ViewPagerAdapter(getSupportFragmentManager());
        //预加载界面数(ViewPager预加载默认数是1个，既设置0也没效果，他会默认把相邻界面数据预加载)
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(mAdapter);
        //初始化默认加载界面
        mViewPager.setCurrentItem(0);
        homeImg.setImageDrawable(getResources().getDrawable(R.drawable.main_query_checd_icon));
        homeText.setTextColor(getResources().getColor(R.color.blue_409EFF));
    }

    /**
     * 初始化所有tab
     */
    private void resetImg(){
        homeImg.setImageDrawable(getResources().getDrawable(R.drawable.main_query_nochecd_icon));
        homeText.setTextColor(getResources().getColor(R.color.main_tab_text_9a9a9a));
        statisImg.setImageDrawable(getResources().getDrawable(R.drawable.main_statis_nochecd_icon));
        statisText.setTextColor(getResources().getColor(R.color.main_tab_text_9a9a9a));
        myImg.setImageDrawable(getResources().getDrawable(R.drawable.main_my_nochecd_icon));
        myText.setTextColor(getResources().getColor(R.color.main_tab_text_9a9a9a));
    }


    /**
     * 定义viewPager左右滑动的适配器
     */
    public class ViewPagerAdapter extends FragmentPagerAdapter{

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                return homeFragment;
            }
//            else if(position == 1){
//                return statisFragment;
//            }
            else{
                return myFragmnet;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            super.destroyItem(container, position, object);
        }

        @Override
        public void destroyItem(@NonNull View container, int position, @NonNull Object object) {
//            super.destroyItem(container, position, object);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        //先初始化所有Tab
        resetImg();
        if(position == 0){
            homeImg.setImageDrawable(getResources().getDrawable(R.drawable.main_query_checd_icon));
            homeText.setTextColor(getResources().getColor(R.color.blue_409EFF));
//            mViewPager.setCurrentItem(0);
        }
//        else if(position == 1){
//            statisImg.setImageDrawable(getResources().getDrawable(R.drawable.main_statis_checd_icon));
//            statisText.setTextColor(getResources().getColor(R.color.blue_409EFF));
////            mViewPager.setCurrentItem(1);
//        }
        else{
            myImg.setImageDrawable(getResources().getDrawable(R.drawable.main_my_checd_icon));
            myText.setTextColor(getResources().getColor(R.color.blue_409EFF));
//            mViewPager.setCurrentItem(2);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_buttom_tab_homeLayout:
                //先初始化所有Tab
                resetImg();
                homeImg.setImageDrawable(getResources().getDrawable(R.drawable.main_query_checd_icon));
                homeText.setTextColor(getResources().getColor(R.color.blue_409EFF));
                mViewPager.setCurrentItem(0);
                break;
            case R.id.main_buttom_tab_statisLayout:
                //先初始化所有Tab
                resetImg();
                statisImg.setImageDrawable(getResources().getDrawable(R.drawable.main_statis_checd_icon));
                statisText.setTextColor(getResources().getColor(R.color.blue_409EFF));
                mViewPager.setCurrentItem(1);
                break;
            case R.id.main_buttom_tab_myLayout:
                //先初始化所有Tab
                resetImg();
                myImg.setImageDrawable(getResources().getDrawable(R.drawable.main_my_checd_icon));
                myText.setTextColor(getResources().getColor(R.color.blue_409EFF));
                mViewPager.setCurrentItem(2);
                break;
        }
    }


    /**
     * 注册权限
     * Android6.0默认安装禁止SD卡的读写权限，以下方式打开权限
     *
     *（如果权限已授权，直接初始化推送服务，否则弹出权限开通对话款，监听开通回调，如已授权，初始化推送服务）
     */
    private void registerPermission(){
        try {
            PackageManager pkgManager = getPackageManager();

            // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
            boolean sdCardWritePermission =
                    pkgManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

            // read phone state用于获取 imei 设备信息
            boolean phoneSatePermission =
                    pkgManager.checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

            if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission || !phoneSatePermission) {
                requestPermission();
            } else {
                PushManager.getInstance().initialize(this, PushNoticeService.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPermission() {
        try {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                    REQUEST_PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            if (requestCode == REQUEST_PERMISSION) {
                if ((grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    PushManager.getInstance().initialize(this, com.wanding.notice.getui.PushNoticeService.class);
                } else {
                    Log.e("Tag", "We highly recommend that you need to grant the special permissions before initializing the SDK, otherwise some "
                            + "functions will not work");
                    PushManager.getInstance().initialize(this, com.wanding.notice.getui.PushNoticeService.class);
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 双击Back键退出应用
     */
    private long firstTime=0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;// 更新firstTime
                    return true;
                } else { // 两次按键小于2秒时，退出应用
                    System.exit(0);

                    // 退出应用时关闭计时
                    // handlerNew.removeCallbacks(runnable);
                }
                break;


        }
        return super.onKeyDown(keyCode, event);
    }
}
