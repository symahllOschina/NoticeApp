package com.wanding.notice.utils;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;

import com.wanding.notice.R;

public class TimeCountUtil extends CountDownTimer {

	private Activity mActivity;//当前Activity
	private Button btn;//计时按钮/重新发送
	
	/**
	 * 重写构造方法，并传入以上参数
	 * 在这个构造方法里需要传入三个参数，一个是Activity，一个是总的时间millisInFuture，
	 * 一个是countDownInterval，然后就是你在哪个按钮上做这个事，就把这个按钮传过来就可以了
	 */
	 
	public TimeCountUtil(long millisInFuture, long countDownInterval,
			Activity mActivity, Button btn) {
		super(millisInFuture, countDownInterval);
		this.mActivity = mActivity;
		this.btn = btn;
	}
	
	/***
	 * 点击按钮开始倒计时
	 */
	@SuppressLint("NewApi")
	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		btn.setClickable(false);//设置不能点击
		btn.setText(millisUntilFinished / 1000 + "秒后重新获取");//设置倒计时时间

		//设置按钮为灰色，这时是不能点击的
		btn.setBackground(mActivity.getResources().getDrawable(R.color.gray_e5e5e5));
		btn.setTextColor(mActivity.getResources().getColor(R.color.grey_666666));
		Spannable span = new SpannableString(btn.getText().toString());//获取按钮的文字
		//将倒计时时间显示为红色
		span.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		btn.setText(span);
	}

	

	/**
	 * 倒计时结束
	 */
	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		btn.setText("重新获取");
		btn.setClickable(true);//重新获得点击
//		btn.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_btn_back));//还原背景色
		btn.setBackgroundColor(mActivity.getResources().getColor(R.color.blue_409EFF));
		btn.setTextColor(mActivity.getResources().getColor(R.color.white_ffffff));
	}
	
	
	
// //	然后在需要用这个的方法里new一个对象，然后调用start();方法就可以啦
//
//	CountUtil timeCountUtil timeCountUtil = new TimeCountUtil(this, 60000, 1000, verficationBtn);
//	                timeCountUtil.start();
//
//	                              // 获取验证码
//	getVerificationCode(phoneNum);

}
