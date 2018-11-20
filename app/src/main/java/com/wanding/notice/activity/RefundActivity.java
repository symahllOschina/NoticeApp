package com.wanding.notice.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wanding.notice.R;
import com.wanding.notice.base.BaseActivity;
import com.wanding.notice.bean.OrderDetailData;
import com.wanding.notice.bean.UserBean;
import com.wanding.notice.httputils.HttpURLConnectionUtil;
import com.wanding.notice.httputils.NetworkUtils;
import com.wanding.notice.utils.DecimalUtil;
import com.wanding.notice.utils.MD5;
import com.wanding.notice.utils.MySerialize;
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.SharedPreferencesUtil;
import com.wanding.notice.utils.TimeCountUtil;
import com.wanding.notice.utils.ToastUtils;
import com.wanding.notice.utils.Utils;
import com.wanding.notice.view.CustomDialog;
import com.wanding.notice.view.MoneyEditText;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 *退款界面
 */
public class RefundActivity extends BaseActivity implements View.OnClickListener{

    private Context context;
    private static final String TAG = "RefundActivity";

    private ImageView imgBack;
    private TextView tvTitle;

    private EditText etMoney,etVerCode;

    private Button btGetVerCode;//获取验证码
    private Button btSubmit;//退款按钮


    private UserBean userBean;
    private OrderDetailData order;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private String accountStr,passwdStr,MD5PasswdStr;
    private Dialog hintDialog;// 加载数据时对话框
    private boolean btRefund = true;
    private TimeCountUtil timeCountUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refund_activity);
        context = RefundActivity.this;
        sharedPreferencesUtil = new SharedPreferencesUtil(context,"userInfo");
        initView();
        initListener();
        initData();
    }

    private void initView(){
        imgBack = findViewById(R.id.header_back);
        tvTitle = findViewById(R.id.header_title);
        tvTitle.setText("退款");

        etMoney = findViewById(R.id.refund_activity_etPrice);
        MoneyEditText.setPricePoint(etMoney);
        etVerCode = findViewById(R.id.refund_activity_etVerCode);

        btGetVerCode = findViewById(R.id.refund_activity_btGetVerCode);
        btSubmit = findViewById(R.id.refund_activity_btOk);
    }

    private void initListener(){
        imgBack.setOnClickListener(this);
        btGetVerCode.setOnClickListener(this);
        btSubmit.setOnClickListener(this);
    }

    private void initData(){

        //取出对象
        try {
            userBean = (UserBean) MySerialize.deSerialization(com.wanding.notice.utils.MySerialize.getObject("user", context));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        Intent in = getIntent();
        order = (OrderDetailData) in.getSerializableExtra("order");

        if (sharedPreferencesUtil.contain("account")) {
            Log.e("登录保存的状态key：", "Key存在");
            accountStr = (String) sharedPreferencesUtil.getSharedPreference("account","");
            passwdStr = (String) sharedPreferencesUtil.getSharedPreference("passwd", "");
            Log.e(TAG,"保存用户名："+accountStr+",密码："+passwdStr);

        }
        MD5PasswdStr = MD5.MD5Encode(passwdStr+accountStr);
    }

    /**
     * 获取退款验证码
     */
    private void getVerCode(){
        btRefund = false;
        final String url = NitConfig.getVerCodeUrl;
        new Thread() {
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("orderId", order.getOrderId());
                    userJSON.put("sid", userBean.getSid());
                    userJSON.put("mid", userBean.getMid());
                    String content = String.valueOf(userJSON);
                    Log.e("发起请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url, content);
                    Log.e("返回字符串结果：", jsonStr);
                    int msg = 2;
                    String text = jsonStr;
                    sendMessage(msg, text);

                }catch (IOException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.JSON_IO_CODE, NetworkUtils.JSON_IO_TEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.SERVICE_CODE, NetworkUtils.SERVICE_TEXT);
                }
            }
        }.start();
    }


    /**
     * 退款请求
     */
    private void RefundReQuest(final String price,final String etVerCodeStr) {
        btRefund = false;
        hintDialog= CustomDialog.CreateDialog(context, "    退款中...");
        hintDialog.show();
        hintDialog.setCancelable(false);
        final String url = NitConfig.refundRequestUrl;
        new Thread() {
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("orderId", order.getOrderId());
                    if(Utils.isNotEmpty(price)){
                        userJSON.put("amount", price);
                    }else{
                        userJSON.put("amount", order.getGoodsPrice());
                    }
                    userJSON.put("verCode", etVerCodeStr);
                    userJSON.put("desc", "");
                    userJSON.put("passWord", MD5PasswdStr);
                    userJSON.put("role", userBean.getRole());
                    userJSON.put("roleId", userBean.getRoleId());
                    String content = String.valueOf(userJSON);
                    Log.e("发起请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url, content);
                    Log.e("返回字符串结果：", jsonStr);
                    int msg = 1;
                    String text = jsonStr;
                    sendMessage(msg, text);

                }catch (SocketTimeoutException e){
                    //服务器响应超时：服务器已经收到了请求但是没有给客户端进行有效的返回
                }catch (ConnectTimeoutException e){
                    //服务器请求超时，指在请求的时候无法客户端无法连接上服务端
                }catch (TimeoutException e){
                    //连接超时
                }catch (IOException e) {
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
                    String jsonStr_2 = (String) msg.obj;
                    RefundResultJSON(jsonStr_2);
                    hintDialog.dismiss();
                    btRefund = true;
                    break;
                case 2:
                    String jsonStr_1 = (String) msg.obj;
                    getVerCodeReturnJson(jsonStr_1);
                    btRefund = true;
                    break;
                case 201:
                    String errorJsonText = (String) msg.obj;
                    if(hintDialog!=null){
                        hintDialog.dismiss();
                    }
                    btRefund = true;
                    break;
                case 400:
                    String errorServiceText = (String) msg.obj;
                    if(hintDialog!=null){
                        hintDialog.dismiss();
                    }
                    btRefund = true;
                    break;
            }
        }
    };

    private void getVerCodeReturnJson(String jsonStr){
        //{"data":null,"message":"发送成功！","status":200}
        try {
            JSONObject job = new JSONObject(jsonStr);
            String status = job.getString("status");
            String message = job.getString("message");
            if(status.equals("200")){
                ToastUtils.showText(RefundActivity.this,"验证码发送成功！");

            }else{
                timeCountUtil.cancel();
                timeCountUtil.onFinish();
                ToastUtils.showText(RefundActivity.this,message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void RefundResultJSON(String str) {
        //{"data":{},"message":"交易失败","status":300}
        try {
            JSONObject job = new JSONObject(str);
            String status = job.getString("status");
            String message = job.getString("message");
            if(status.equals("200")){
                ToastUtils.showText(context,"退款成功！");
                finish();
            }else if(status.equals("300")){
                ToastUtils.showText(context,message);
            }else{
                ToastUtils.showText(context,"退款失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.header_back:
                finish();
                break;
            case R.id.refund_activity_btGetVerCode://获取验证码
                //防止连续点击
                if(Utils.isFastClick()){
                    return;
                }
                timeCountUtil = new TimeCountUtil(60000, 1000, RefundActivity.this, btGetVerCode);
                timeCountUtil.start();
                getVerCode();
                break;
            case R.id.refund_activity_btOk:
                if(btRefund){
                    String etVerCodeStr = etVerCode.getText().toString().trim();
                    String etPriceStr = etMoney.getText().toString().trim();
                    String etPrice = "";

                    if(Utils.isEmpty(etVerCodeStr)){
                        ToastUtils.showText(RefundActivity.this,"验证码不能为空！");
                        return;
                    }


                    if(Utils.isNotEmpty(etPriceStr)){
                        etPrice = DecimalUtil.scaleNumber(etPriceStr);
                        double dou_etPrice = Double.parseDouble(etPrice);

                        String ordrPriceStr = order.getGoodsPrice();
                        double dou_price = Double.parseDouble(ordrPriceStr);
                        if(dou_etPrice>dou_price){
                            ToastUtils.showText(RefundActivity.this,"退款金额不能大于支付金额！");
                            return;
                        }
                    }
                    if(timeCountUtil!=null){
                        timeCountUtil.cancel();
                        timeCountUtil.onFinish();
                    }
                    RefundReQuest(etPrice,etVerCodeStr);


                }
                break;
        }
    }
}
