package com.wanding.notice.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.wanding.notice.R;
import com.wanding.notice.base.BaseActivity;
import com.wanding.notice.httputils.HttpURLConnectionUtil;
import com.wanding.notice.httputils.NetworkUtils;
import com.wanding.notice.utils.MySerialize;
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.SharedPreferencesUtil;
import com.wanding.notice.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/** 语音设置界面 */
public class VoiceSettingActivity extends BaseActivity implements OnClickListener,CompoundButton.OnCheckedChangeListener{

	private Context context;
    private static final String TAG = "VoiceSettingActivity";

    private ImageView imgBack;
	private TextView tvTitle;

	private Switch voiceSwitch;//是否播报
	private TextView tvClientId;


	private com.wanding.notice.bean.UserBean userBean;
	private SharedPreferencesUtil sharedPreferencesUtil1;
	private SharedPreferencesUtil sharedPreferencesUtil2;




	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_setting_activity);
		initView();
		initListener();
		initData();


	}
	
	/** 
	 * 初始化界面控件
	 */
	private void initView(){
		context = VoiceSettingActivity.this;
		imgBack = (ImageView) findViewById(R.id.header_back);
		tvTitle = (TextView) findViewById(R.id.header_title);

		voiceSwitch = findViewById(R.id.voice_setting_voiceSwitch);
		tvClientId = findViewById(R.id.voice_setting_tvClientId);
		tvTitle.setText("语音设置");

	}

	private void initListener(){
		imgBack.setOnClickListener(this);
		voiceSwitch.setOnCheckedChangeListener(this);

	}

	/** 初始化数据 */
	private void initData(){
		//取出对象
		try {
			userBean=(com.wanding.notice.bean.UserBean) MySerialize.deSerialization(MySerialize.getObject("user", context));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		sharedPreferencesUtil1 = new SharedPreferencesUtil(context,"voiceSwitch");
		//取出保存的开关值
		boolean switchChecked = (boolean) sharedPreferencesUtil1.getSharedPreference("switchChecked",false);
		if(switchChecked){
			voiceSwitch.setChecked(true);
			voiceSwitch.setSwitchTextAppearance(context,R.style.switch_true);
			Log.e(TAG,"Switch打开");
		}else{
			voiceSwitch.setChecked(false);
			voiceSwitch.setSwitchTextAppearance(context,R.style.switch_false);
			Log.e(TAG,"Switch关闭");
		}
		sharedPreferencesUtil2 = new SharedPreferencesUtil(context,"ClientId");
		String clientId = (String) sharedPreferencesUtil2.getSharedPreference("cid","");
		tvClientId.setText(clientId);

		//查询别名绑定状态
		queryAliasStatus(clientId);
	}

	/**
	 * 查询Alias绑定状态
	 */
	private void queryAliasStatus(final String clientId){
		final String url = NitConfig.queryAliasStatusUrl;
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
					int msg = 1;
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


	private void sendMessage(int what,String text){
		Message msg = new Message();
		msg.what = what;
		msg.obj = text;
		handler.sendMessage(msg);
	}

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 1:
					//{"data":"1000145","message":"查询成功","status":200}
					String jsonStr_2 = (String) msg.obj;
					queryAliasStatusJSON(jsonStr_2);
					break;
				case 201:
					String errorJsonText = (String) msg.obj;
					break;
				case 400:
					String errorServiceText = (String) msg.obj;
					break;
			}
		}
	};

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
				PushManager.getInstance().bindAlias(VoiceSettingActivity.this, userId);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "bindAlias = " + userId);
			}
			Log.e(TAG, "bindAlias = " + userId);
		}
	}

	@Override
	public void onClick(View v) {
		Intent in = null;
		switch (v.getId()) {
		case R.id.header_back:
			finish();
			break;
		
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			//设置开状态时的字体颜色
			voiceSwitch.setSwitchTextAppearance(context,R.style.switch_true);

			//保存打开状态
			boolean switchChecked = true;
			sharedPreferencesUtil1.put("switchChecked",switchChecked);
            Log.e(TAG,"Switch打开");
		}else{
			//设置关状态时的字体颜色
			voiceSwitch.setSwitchTextAppearance(context,R.style.switch_false);
			//保存关闭状态
			boolean switchChecked = false;
			sharedPreferencesUtil1.put("switchChecked",switchChecked);
            Log.e(TAG,"Switch关闭");
		}
	}
}
