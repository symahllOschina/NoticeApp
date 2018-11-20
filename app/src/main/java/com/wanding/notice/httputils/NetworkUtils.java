package com.wanding.notice.httputils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

/**
 * 获取网络状态帮助类
 * isNetworkAvailable：判断是否连接网络（只能判断当前是否连接网络，不能判断网络是否可用）
 * getConnectedType：获取当前连接网络的类型（-1：没有网络 1：WIFI网络 2：wap网络 3：net网络 ）
 * isWifiConnected： 判断Wifi是否可用
 * isMobileConnected：判断Mobile网络是否可用
 * ping：此方法比较特殊（解决在Android环境下的网络是否可用，其原理就是访问一个域名，访问成功拿到数据代表网络可用
 *          但实际项目中如果不是特别需要建议不要使用，会影响数据请求速度等问题）
 */
public class NetworkUtils {

    public static final int JSON_IO_CODE = 201;
    public static final String JSON_IO_TEXT = "数据请求失败！";
    public static final int SERVICE_CODE = 400;
    public static final String SERVICE_TEXT = "服务请求异常！";

    /**
     *  检查当前网络状态(只能判断当前是否连接网络，不能判断网络是否可用)
     *  必须添加访问当前网络状态权限
     *   *	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
     */
    public static boolean isNetworkAvailable(Activity activity){
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //新版本调用方法获取网络状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        }else{

            //旧版本调用方法
            if (connectivityManager == null)
            {
                return false;
            }else
            {
                // 获取NetworkInfo对象
                NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

                if (networkInfo != null && networkInfo.length > 0)
                {
                    for (int i = 0; i < networkInfo.length; i++)
                    {
                        System.out.println(i + "===状态===" + networkInfo[i].getState());
                        System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                        // 判断当前网络状态是否为连接状态
                        if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * 获取当前网络连接的类型信息
     *	   -1：没有网络
     *		1：WIFI网络
     *		2：wap网络
     *		3：net网络
     */
    public static int getConnectedType(Context context) {
        if (context != null)
        {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable())
            {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    /**
     *  判断WIFi 是否可用
     */
    public boolean isWifiConnected(Context context) {
        if (context != null)
        {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null)
            {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断MOBILE网络是否可用
     */
    public boolean isMobileConnected(Context context) {
        if (context != null)
        {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null)
            {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网
     *  Android 环境下： ping -c 1 -w 100 sina.cn
     -c: 表示次数，1 为1次 -w: 表示deadline, time out的时间，单位为秒，10为10秒。
     连起来的意思是，ping 主机sina.cn 一次，超时为10秒
     */
    public static  boolean ping() {
        String result = null;
        try {
            String ip = "http://www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + ip);// ping网址3次
            /**读取ping的内容，可以不加
             InputStream input = p.getInputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(input));
             StringBuffer stringBuffer = new StringBuffer();
             String content = "";
             while ((content = in.readLine()) != null) {
             stringBuffer.append(content);
             }
             Log.d("------ping-----", "result content : " + stringBuffer.toString());
             */
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("----result---", "result = " + result);
        }
        return false;
    }

}
