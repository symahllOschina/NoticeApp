package com.wanding.notice.httputils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OKhttp3请求帮助类:
 * 使用Handler实现子线程和主线程之间的交互
 * 定义三个接口调用分别返回String，byte[],和JSONObject
 * 同时在支持表单提交Post 请求
 * 复用Handler而不是一直实例化
 * <p>
 * //借鉴：https://www.2cto.com/kf/201712/704283.html
 */
public class OKHttp3Utils {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    //定义成员变量
    private static OkHttpClient mOkHttpClient;
    private volatile static OKHttp3Utils mOkHttpUtils;//防止多个线程同时访问
    private static Handler handler;

    //生成构造方法，完成初始化参数
    public OKHttp3Utils() {
        mOkHttpClient = new OkHttpClient();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    ((RequestCallBack) msg.obj).onSuccess(msg.getData().getString("data"));
                } else {
                    ((RequestCallBack) msg.obj).onFailure(msg.getData().getString("error"));
                }
            }
        };
    }


    //使用单例模式，通过获取的方式拿到对象
    public static OKHttp3Utils getInstance() {
        if (mOkHttpUtils == null) {
            synchronized (OkHttpClient.class) {
                if (mOkHttpUtils == null) {
                    mOkHttpUtils = new OKHttp3Utils();
                }
            }
        }
        return mOkHttpUtils;
    }


    /**
     * 回调接口便于修改框架
     */
    public interface RequestCallBack<T> {
        void onSuccess(T result);

        void onFailure(String error);
    }


    /**
     * get请求
     */
    public static String get(String url, final RequestCallBack requestCallBack) {
        Request request = new Request.Builder()
                .url(url).addHeader("", "")
                .get()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                if (requestCallBack != null) {
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getMessage());
                    message.setData(bundle);
                    message.what = 0;
                    message.obj = requestCallBack;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (requestCallBack != null) {
                    String s = "";
                    try {
                        s = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("data", s);
                    message.setData(bundle);
                    message.what = 1;
                    message.obj = requestCallBack;
                    handler.sendMessage(message);
                }
            }
        });
        return url;
    }


    /**
     * post无参请求
     */
    public static void post(String url, final RequestCallBack requestCallBack) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("api-key", "j=pp=ytGv6nso1uJMYa37ouT=kk=")
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (requestCallBack != null) {
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getMessage());
                    message.setData(bundle);
                    message.what = 0;
                    message.obj = requestCallBack;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (requestCallBack != null) {
                    String s = "";
                    try {
                        s = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("data", s);
                    message.setData(bundle);
                    message.what = 1;
                    message.obj = requestCallBack;
                    handler.sendMessage(message);
                }
            }
        });
    }


    /**
     * post 请求
     *
     * @param url
     * @param requestJson
     * @param requestCallBack
     */
    public static void post(String url, JSONObject requestJson, final RequestCallBack requestCallBack) {
        RequestBody body = RequestBody.create(JSON, requestJson.toString());
        Request request = new Request.Builder()
                .url(url)
                .addHeader("api-key", "j=pp=ytGv6nso1uJMYa37ouT=kk=")
                .post(body)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (requestCallBack != null) {
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("error", e.getMessage());
                    message.setData(bundle);
                    message.what = 0;
                    message.obj = requestCallBack;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (requestCallBack != null) {
                    String s = "";
                    try {
                        s = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putString("data", s);
                    message.setData(bundle);
                    message.what = 1;
                    message.obj = requestCallBack;
                    handler.sendMessage(message);
                }
            }
        });
    }
}
