package com.wanding.notice.application;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.wanding.notice.activity.MainActivity;
import com.wanding.notice.baidu.tts.util.AutoCheck;
import com.wanding.notice.baidu.tts.util.InitConfig;
import com.wanding.notice.baidu.tts.util.MySyntherizer;
import com.wanding.notice.baidu.tts.util.NonBlockSyntherizer;
import com.wanding.notice.baidu.tts.util.OfflineResource;
import com.wanding.notice.baidu.tts.util.UiMessageListener;
import com.wanding.notice.httputils.NetworkUtils;
import com.wanding.notice.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.internal.Util;


public class BaseApplication extends Application{

    private static final String TAG = "BaseApplication";

    //运用list来保存们每一个activity是关键
    private List<Activity> mList = new ArrayList<Activity>();
    //为了实现每次使用该类时不创建新的对象而创建的静态对象
    private static BaseApplication instance;


    //实例化
    public synchronized static BaseApplication getInstance(){
        if(instance==null){
            instance=new BaseApplication();
        }
        return instance;
    }

    // 保存打开的Actviity到集合中
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    private static AppHandler handler;//应用消息统一处理hander,包括推送消息和语音消息
    public static MainActivity mainActivity;
    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView == null)
     */
    public static StringBuilder payloadData = new StringBuilder();


    // ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     */
    protected String appId = "11206343";

    protected String appKey = "UVD75K0IKhBQ9IIQn1UEdkfM";

    protected String secretKey = "pVKuB9dpZ4tn5Ic1HuICgZIcNmaGuOGM";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.ONLINE;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    public static MySyntherizer synthesizer;


    /**
     * 程序被创建时执行
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"程序被创建！");
        Log.d(TAG, "BaseApplication onCreate");
        if(handler == null){
            handler = new AppHandler();
        }

        if(synthesizer!=null){
            synthesizer.release();
        }



        // 初始化TTS引擎
        initialTts();
    }


    /**
     * 程序终止时执行
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e(TAG,"程序终止！");
    }

    /** 低内存的时候执行  */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG,"低内存！");
    }

    /**
     * 程序在内存清理的时候执行(即退出应用在后台时执行)
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.e(TAG,"程序在清理内存！");
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        try {
            LoggerProxy.printable(true); // 日志打印在logcat中
            // 设置初始化参数
            // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
            SpeechSynthesizerListener listener = new UiMessageListener(handler);

            Map<String, String> params = getParams();


            // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
            InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

            // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
            // 上线时请删除AutoCheck的调用
//            AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
//                @Override
//                public void handleMessage(Message msg) {
//                    if (msg.what == 100) {
//                        AutoCheck autoCheck = (AutoCheck) msg.obj;
//                        synchronized (autoCheck) {
//                            String message = autoCheck.obtainDebugMessage();
////                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
//                            Log.e("AutoCheckMessage", message);
//                        }
//                    }
//                }
//
//            });
            synthesizer = new NonBlockSyntherizer(this, initConfig, handler); // 此处可以改为MySyntherizer 了解调用过程
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
//            toPrint("【error】:copy files from assets failed." + e.getMessage());
            // 可以用下面一行替代，在logcat中查看代码
            Log.e("【error】:", e.getMessage());
        }
        return offlineResource;
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    public static void speak(String text) {

//        String text = "百度语音，面向广大开发者永久免费开放语音合成技术。";
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }

    private static void checkResult(int result, String method) {
        if (result != 0) {
//            toPrint("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
            Log.e("error code :", result+" method:" + method );
        }
    }

    /** 定义全局个推发送消息方法，之后发送给handler处理 */
    public static void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }

    public static class AppHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://transmessionMsg:透传消息
                    String transmessionMsg = (String) msg.obj;
                    if(mainActivity != null){
                        //保存离线消息到StringBuilder中
                        payloadData.append(transmessionMsg);
                        payloadData.append("\n");

                    }

                    Log.e(TAG,"transmessionMsg透传消息："+transmessionMsg);


                    break;
                case 1://
                    String Msg = (String) msg.obj;

                    break;

            }
        }
    }

    /**
     * 在每个activity被创建时加上: BaseApplication.getInstance().addActivity(this);
     *
     * 当你想关闭时，调用BaseApplication的exit方法关闭整个程序:
     * BaseApplication.getInstance().exit();
     */
    //关闭每一个list内的activity
    public void exit() {
        try {
            for (Activity activity:mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            System.exit(0);
//            synthesizer.release();
//
//        }
    }

    //关闭除MianActivity外list内的其余的activity
    public void exit1() {
        for (int i = 0; i < mList.size(); i++) {
            Activity activity = mList.get(i);
            //当前Activity的名称为：com.mobile.android.yiloneshop.activity.MainLayoutActivity@f59a736
            Log.e("当前Activity的名称为：", activity+"");
            String activityStr = String.valueOf(activity);
            String containStr = "MainActivity";
            if( !activityStr.contains(containStr) )
            {
                if (activity != null)
                {
                    activity.finish();
                }
            }
        }
    }

    /**
     * 如果要像微信一样，所有字体都不允许随系统调节而发生大小变化
     * 在工程的Application或BaseActivity中添加下面的代码
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }




}
