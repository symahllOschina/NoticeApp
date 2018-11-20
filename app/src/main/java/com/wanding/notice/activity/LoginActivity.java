package com.wanding.notice.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.igexin.sdk.PushManager;
import com.wanding.notice.R;
import com.wanding.notice.base.BaseActivity;
import com.wanding.notice.bean.UserBean;
import com.wanding.notice.httputils.HttpURLConnectionUtil;
import com.wanding.notice.httputils.NetworkUtils;
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
import java.net.SocketTimeoutException;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity implements OnClickListener{

    private static final String TAG = "LoginActivity";
    private Context context;

    private ImageView imgBack;
    private TextView tvTitle;
    private AutoCompleteTextView etAccount;
    private EditText etPasswd;
    private Button btLogin;

    private UserBean userBean;

    String accountStr = "";
    String passwdStr = "";
    String clientId = "";
    private SharedPreferencesUtil sharedPreferencesUtil;
    private SharedPreferencesUtil sharedPreferencesUtilSwitch;
    private SharedPreferencesUtil sharedPreferencesUtilClientId;

    private ProgressDialog loginDialog;//登录提示框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = LoginActivity.this;
        sharedPreferencesUtil = new SharedPreferencesUtil(LoginActivity.this,"userInfo");
        sharedPreferencesUtilSwitch = new SharedPreferencesUtil(context,"voiceSwitch");
        sharedPreferencesUtilClientId = new SharedPreferencesUtil(context,"ClientId");
        clientId = (String) sharedPreferencesUtilClientId.getSharedPreference("cid","");

        initData();

    }

    /**
     * 初始化应用信息
     */
    private void initData(){
        if(sharedPreferencesUtil.contain("account")){
            Log.e("登录保存的状态key：", "Key存在");
            accountStr = (String) sharedPreferencesUtil.getSharedPreference("account","");
            passwdStr = (String) sharedPreferencesUtil.getSharedPreference("passwd","");
            doLogin(accountStr,passwdStr);
        }else{
            Log.e("登录保存的状态key：", "Key不存在");
            setContentView(R.layout.login_activity);
            initView();
            initListener();
        }
    }

    /** 初始化View,控件 */
    private void initView(){
        imgBack = findViewById(R.id.header_back);
        tvTitle = findViewById(R.id.header_title);
        etAccount = (AutoCompleteTextView) findViewById(R.id.login_activity_etAccount);
        etPasswd = (EditText) findViewById(R.id.login_activity_etPasswd);
        btLogin = (Button) findViewById(R.id.login_activity_btSubmit);

        imgBack.setVisibility(View.INVISIBLE);
        tvTitle.setText("登录");
    }

    /** 注册监听  */
    private void initListener(){
        btLogin.setOnClickListener(this);
    }

    //尝试登录
    private void attemptLogin() {
        //记录焦点
        boolean cancel = false;
        View focusView = null;

        accountStr = etAccount.getText().toString();
        passwdStr = etPasswd.getText().toString();
        if(Utils.isEmpty(accountStr)){
            ToastUtils.showText(context,"用户名不能为空！");
            return;
        }

        if(Utils.isEmpty(passwdStr)){
            ToastUtils.showText(context,"密码不能为空！");
            return;
        }
        //都不为空的情况下判断用户名密码是否正确（格式是否正确，比如用户名为手机号时手机号是否为11位等）
        //这里直接提交服务器验证
        doLogin(accountStr,passwdStr);


        if (!TextUtils.isEmpty(passwdStr) && !isPasswordValid(passwdStr)) {
//            etPasswd.setError(getString(R.string.error_invalid_password));
            focusView = etPasswd;
            cancel = true;
        }
        if (TextUtils.isEmpty(accountStr)) {
//            etAccount.setError(getString(R.string.error_field_required));
            focusView = etAccount;
            cancel = true;
        } else if (!isEmailValid(accountStr)) {
//            etAccount.setError(getString(R.string.error_invalid_email));
            focusView = etAccount;
            cancel = true;
        }
        if (cancel) {

            focusView.requestFocus();
        } else {


        }
    }

    private boolean isEmailValid(String email) {

        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }


    /** 登录（提交到服务器验证）
     *  account和password参数
     *  测试登录账号100014510111密码123456
     */
    private void doLogin(final String account,String passwd){
        if(!NetworkUtils.isNetworkAvailable(this)){
            ToastUtils.showText(LoginActivity.this,"请检查网络是否连接...");
            return;
        }

        loginDialog = new ProgressDialog(this);// 新建了一个进度条
        loginDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条风格，风格为圆形，旋转的
        loginDialog.setCancelable(true);// 按返回键取消
        loginDialog = ProgressDialog.show(this, null, "正在登录中,请稍后.....");
        final String url = NitConfig.doLoginUrl;
        Log.e("doLogin请求地址：",url);
        //对参数密码值经过MD5加密再传值（加密方式：MD5.MD5Encode(密码+账号)）
//        final String accountStr = "1000145101";
//        final String passwdStr = "123456";
        final String MD5PasswdStr = MD5.MD5Encode(passwd+account);
        new Thread(){
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("account",account);
                    userJSON.put("password",MD5PasswdStr);
                    String content = String.valueOf(userJSON);
                    Log.e("发起请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content);
                    //{"data":{"name":"大客户会员款台","sid":"1971","eid":"89","mid":"200","role": "store","roleId":"89","account":"100014510111"},"message":"","status":200}
                    Log.e("返回字符串结果：", jsonStr);
                    int msg = 1;
                    String text = jsonStr;
                    sendMessage(msg,text);
                }catch (SocketTimeoutException e){

                }catch (IOException e){
                    e.printStackTrace();
                    sendMessage(NetworkUtils.JSON_IO_CODE,NetworkUtils.JSON_IO_TEXT);
                }catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.SERVICE_CODE,NetworkUtils.SERVICE_TEXT);
                }
            }
        }.start();

    }

    /**
     * 查询Alias绑定状态
     */
    private void queryAliasStatus(){
        final String url = NitConfig.queryAliasStatusUrl;
        Log.e("queryAliasStatus请求地址：",url);
        new Thread(){
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("clientId",clientId);
                    String content = String.valueOf(userJSON);
                    Log.e("发起请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content);
                    //{"data":{"name":"大客户会员款台","sid":"1971","eid":"89","mid":"200","role": "store","roleId":"89","account":"100014510111"},"message":"","status":200}
                    Log.e("返回字符串结果：", jsonStr);
                    int msg = 2;
                    String text = jsonStr;
                    sendMessage(msg,text);
                } catch (JSONException e) {
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                    sendMessage(NetworkUtils.JSON_IO_CODE,NetworkUtils.JSON_IO_TEXT);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.SERVICE_CODE,NetworkUtils.SERVICE_TEXT);
                }
            }
        }.start();

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    //{"data":{"name":"大客户会员款台","sid":"1971","eid":"89","mid":"200","role": "store","roleId":"89","account":"100014510111"},"message":"","status":200}
                    String jsonStr_1 = (String) msg.obj;
                    GsonLoginReturnJSON(jsonStr_1);
                    if(loginDialog!=null){
                        loginDialog.dismiss();
                    }
                    break;
                case 2:
                    //{"data":"1000145","message":"查询成功","status":200}
                    String jsonStr_2 = (String) msg.obj;
                    queryAliasStatusJSON(jsonStr_2);
                    if(loginDialog!=null){
                        loginDialog.dismiss();
                    }
                    break;
                case 201:
                    String errorJsonText = (String) msg.obj;
                    ToastUtils.showText(context,errorJsonText);
                    if(loginDialog!=null){
                        loginDialog.dismiss();
                    }
                    break;
                case 400:
                    String errorServiceText = (String) msg.obj;
                    ToastUtils.showText(context,errorServiceText);
                    if(loginDialog!=null){
                        loginDialog.dismiss();
                    }
                    break;
            }
        }
    };

    private void GsonLoginReturnJSON(String json){
        //{"data":{"errorMsg":"登陆失败！请输入正确账号和密码！"},"message":"","status":300}
        //{"data":{"name":"大客户会员款台","sid":"1971","eid":"89","mid":"200","role": "store","roleId":"89","account":"100014510111"},"message":"","status":200}
        try {
            JSONObject job = new JSONObject(json);
            String status = job.getString("status");
            String dataJson = job.getString("data");
            if(status!=null&&status.equals("200")){
                //保存用户名和密码
                sharedPreferencesUtil.put("account",accountStr);
                sharedPreferencesUtil.put("passwd",passwdStr);
                Gson gson = GsonUtils.getGson();
                userBean = gson.fromJson(dataJson,UserBean.class);
                try {
                    MySerialize.saveObject("user",this,MySerialize.serialize(userBean));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    sendMessage(NetworkUtils.JSON_IO_CODE,NetworkUtils.JSON_IO_TEXT);
                }

                boolean switchChecked = true;
                sharedPreferencesUtilSwitch.put("switchChecked",switchChecked);

                if(Utils.isNotEmpty(clientId)){
                    Log.e("登录完成后ClientId状态不为空:",clientId);
                    //查询别名绑定状态
                    queryAliasStatus();
                }else{
                    intoMainActivity();
                    Log.e("登录完成后ClientId状态为空:","");
                }
            }else if(status!=null&&status.equals("300")){
                String message = job.getString("message");
                ToastUtils.showText(context,message);
                sharedPreferencesUtil.clear();

                initData();

            }else{
                sendMessage(NetworkUtils.SERVICE_CODE,NetworkUtils.SERVICE_TEXT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            sendMessage(NetworkUtils.JSON_IO_CODE,NetworkUtils.JSON_IO_TEXT);
        }


    }

    private void queryAliasStatusJSON(String str){
        //{"data":"1000145","message":"查询成功","status":200}
        try {
            JSONObject job = new JSONObject(str);
            String statusJson = job.getString("status");
            String dataJson = job.getString("data");
            String messageJson = job.getString("message");
            String userId = userBean.getAccount();
            if(statusJson.equals("200")){
                if(dataJson.equals(userId)){
                    intoMainActivity();
                }else{
                    bindAlias(userId);
                }

            }else{
                bindAlias(userId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 绑定别名
     */
    private void bindAlias(String userId){

        if (Utils.isNotEmpty(userId)){
            try {
                PushManager.getInstance().bindAlias(this, userId);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "bindAlias = " + userId);
            }
            intoMainActivity();
            Log.e(TAG, "bindAlias = " + userId);
        }
    }

    private void sendMessage(int what,String text){
        Message msg = new Message();
        msg.what = what;
        msg.obj = text;
        handler.sendMessage(msg);
    }

    private void intoMainActivity(){
        Intent in = new Intent();
        in.setClass(LoginActivity.this,MainActivity.class);
        startActivity(in);
        //跳转动画效果
        overridePendingTransition(R.anim.in_from, R.anim.to_out);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_activity_btSubmit://提交、登录
//                intoMainActivity();
                attemptLogin();
                break;
        }
    }
}

