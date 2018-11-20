package com.wanding.notice.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.wanding.notice.R;
import com.wanding.notice.base.BaseActivity;
import com.wanding.notice.bean.OrderDetailData;
import com.wanding.notice.bean.UserBean;
import com.wanding.notice.httputils.HttpURLConnectionUtil;
import com.wanding.notice.httputils.NetworkUtils;
import com.wanding.notice.query.util.QueryUtil;
import com.wanding.notice.utils.DateTimeUtil;
import com.wanding.notice.utils.GsonUtils;
import com.wanding.notice.utils.MD5;
import com.wanding.notice.utils.MySerialize;
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.SharedPreferencesUtil;
import com.wanding.notice.utils.ToastUtils;
import com.wanding.notice.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 商户信息
 */
public class BusInfoActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private static final String TAG = "BusInfoActivity";
    private ImageView imgBack;
    private TextView tvTitle;

    private TextView tvBusName,tvBusAccount;



    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_info_activity);
        context = BusInfoActivity.this;
        initView();
        initListener();
        initData();

        getBusInfo();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        imgBack = findViewById(R.id.header_back);
        tvTitle = findViewById(R.id.header_title);


        tvBusName = findViewById(R.id.bus_info_tvName);
        tvBusAccount = findViewById(R.id.bus_info_tvAccount);


        tvTitle.setText("商户信息");
    }

    /**
     * 事件监听
     */
    private void initListener() {
        imgBack.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //取出对象
        try {
            userBean = (UserBean) MySerialize.deSerialization(MySerialize.getObject("user", context));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新界面数据
     */
    private void UpdateView(String name,String account){
        tvBusName.setText("");
        tvBusAccount.setText("");
        if(Utils.isNotEmpty(name)){
            tvBusName.setText(name);
        }
        if(Utils.isNotEmpty(account)){
            tvBusAccount.setText(account);
        }
    }




    /**
     * 订单详情 api/app/200/1/queryOrderDetail
     * 入参：orderId（订单主键id）
     */
    private void getBusInfo() {
        final String url = NitConfig.getBusInfoUrl;
        new Thread() {
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("mid", userBean.getMid());
                    String content = String.valueOf(userJSON);
                    Log.e("发起请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url, content);
                    Log.e("返回字符串结果：", jsonStr);
                    int msg = 1;
                    String text = jsonStr;
                    sendMessage(msg, text);

                } catch (JSONException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.JSON_IO_CODE, NetworkUtils.JSON_IO_TEXT);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.JSON_IO_CODE, NetworkUtils.JSON_IO_TEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.SERVICE_CODE, NetworkUtils.SERVICE_TEXT);
                }
            }
        }.start();
    }



    private void sendMessage(int what, String text) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String jsonStr_1 = (String) msg.obj;
                    BusInfoJSON(jsonStr_1);
                    break;
                case 201:
                    String errorJsonText = (String) msg.obj;
                    ToastUtils.showText(context, errorJsonText);
                    break;
                case 400:
                    String errorServiceText = (String) msg.obj;
                    ToastUtils.showText(context, errorServiceText);
                    break;
            }
        }
    };

    private void BusInfoJSON(String str) {
            //{"data":{"mname":"会员大客户测试","maccount":"1000145"},"message":"查询成功","status":200}
        try {
            JSONObject job = new JSONObject(str);
            if (job.getString("status").equals("200")) {
                JSONObject dataObj = new JSONObject(job.getString("data"));
                String nameStr = dataObj.getString("mname");
                String maccountStr = dataObj.getString("maccount");
                UpdateView(nameStr,maccountStr);

            } else {
                ToastUtils.showText(context,"查询失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                finish();
                break;
        }
    }
}
