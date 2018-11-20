package com.wanding.notice.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wanding.notice.R;
import com.wanding.notice.base.BaseActivity;

/** 设置界面 */
public class SettingActivity extends BaseActivity implements OnClickListener{

	private Context context;
	private ImageView imgBack;
	private TextView tvTitle;
	private RelativeLayout voiceSettingLayout;//语音播报设置




	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settting_activity);
		initView();
		initListener();
	}
	
	/** 
	 * 初始化界面控件
	 */
	private void initView(){
		context = SettingActivity.this;
		imgBack = (ImageView) findViewById(R.id.header_back);
		tvTitle = (TextView) findViewById(R.id.header_title);
		voiceSettingLayout = findViewById(R.id.setting_activity_voiceSettingLayout);
		
		tvTitle.setText("设置");

	}

	private void initListener(){
		imgBack.setOnClickListener(this);
		voiceSettingLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent in = null;
		switch (v.getId()) {
		case R.id.header_back://返回
			finish();
			break;
		case R.id.setting_activity_voiceSettingLayout://语音播报
			in = new Intent();
			in.setClass(context, VoiceSettingActivity.class);
			startActivity(in);
			break;
		
		}
	}
}
