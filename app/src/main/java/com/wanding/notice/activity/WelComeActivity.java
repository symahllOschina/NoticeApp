package com.wanding.notice.activity;

import android.content.Intent;
import android.os.Bundle;

import com.wanding.notice.R;
import com.wanding.notice.base.BaseActivity;
import android.os.Handler;
/**
 * 欢迎界面
 */
public class WelComeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(WelComeActivity.this, LoginActivity.class));
                finish();
            }
        }, 2000);
    }
}
