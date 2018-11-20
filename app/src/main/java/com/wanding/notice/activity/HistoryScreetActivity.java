package com.wanding.notice.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wanding.notice.R;
import com.wanding.notice.base.BaseActivity;
import com.wanding.notice.date.picker.CustomDatePicker;
import com.wanding.notice.query.util.QueryUtil;
import com.wanding.notice.utils.DateTimeUtil;
import com.wanding.notice.utils.Utils;
import com.wanding.notice.view.WarpLinearLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/** 筛选界面  */
public class HistoryScreetActivity extends BaseActivity implements View.OnClickListener{


    private Context context;
    private static final String TAG = "ScreetActivity";
    private LinearLayout mainLayout;//仅作为弹出popupWindow时选择时间弹出选择框的坐标
    WarpLinearLayout payTypeLayout,payStateLayout;//交易类型Layout,交易状态Layout
    TextView tvStartTime,tvEndTime,tvReset,tvConfirm;//

    private int displayWidth;
    /**  条件内容  */
    private String payTypeStr,payTypeClick = QueryUtil.payTypeArray[0];//默认选中的交易类型
    private int payTypeIndex;//标记位
    private String payStartTimeStr,payEndTimeStr,payStartTimeClick = "",payEndTimeCLick = "";//交易时间：起始/结束
    private String payStateStr,payStateClick = QueryUtil.payStateArray[0];//默认选中的交易类型
    private int payStateIndex;//标记位
    private int fragmentType;//区分界面（默认为1，即实时交易，2时为历史交易）

    public static final int RESULT_CODE = 1;

    private CustomDatePicker datePicker,timePicker;
    private String dataTimeStr;//日期时间，格式yyyy-MM-dd HH:mm(这里不包含秒)
    private String dateStr;//日期，格式yyyy-MM--dd
    private String timeStr;//时间，格式HH:mm(这里不包含秒)

    private String yesterDayStr;//昨天的日期


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment_home_top_popupwindow);
        context = HistoryScreetActivity.this;
        displayWidth = Utils.getDisplayWidth((Activity)context);
        initView();
        initListener();
        initData();

        PopWindowInitTarget();



    }

    /** 初始化View */
    private void initView(){

        mainLayout = findViewById(R.id.main_home_top_popWindowLayout);
        payTypeLayout = findViewById(R.id.main_home_topPop_payTypeLayout);
        tvStartTime = findViewById(R.id.main_home_topPop_startTimeText);
        tvEndTime = findViewById(R.id.main_home_topPop_endTimeText);
        payStateLayout = findViewById(R.id.main_home_topPop_payStateLayout);
        tvReset= (TextView) findViewById(R.id.main_home_topPop_tvReset);
        tvConfirm = (TextView) findViewById(R.id.main_home_topPop_tvConfirm);

    }

    /**  注册监听 */
    private void initListener(){

        tvReset.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
        tvStartTime.setOnClickListener(this);
        tvEndTime.setOnClickListener(this);
    }

    /** 初始化数据  */
    private void initData(){
        Intent in = getIntent();
        payTypeStr = in.getStringExtra("payType");
        payStartTimeStr = in.getStringExtra("payStartTime");
        payEndTimeStr = in.getStringExtra("payEndTime");
        payStateStr = in.getStringExtra("payState");
        payTypeIndex = in.getIntExtra("payTypeIndex",payTypeIndex);
        payStateIndex = in.getIntExtra("payStateIndex",payStateIndex);
        fragmentType = in.getIntExtra("fragmentType",fragmentType);
        Log.e(TAG,"1筛选条件接收值：" +
                "交易类型=" + payTypeStr+
                ",起始时间="+payStartTimeStr+
                ",结束时间="+payEndTimeStr+
                ",交易状态="+payStateStr+
                ",payTypeIndex="+payTypeIndex+
                ",payStateIndex="+payStateIndex+
                ",fragmentType="+fragmentType);

        //初始化日期时间（即系统默认时间）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        dataTimeStr = sdf.format(new Date());
        dateStr = dataTimeStr.split(" ")[0];
        timeStr = dataTimeStr.split(" ")[1];

        //获取昨天的日期
        yesterDayStr = DateTimeUtil.getDateStr(-1,"yyyy-MM-dd");



        if(Utils.isEmpty(payTypeStr)){
            payTypeStr = QueryUtil.payTypeArray[0];

        }else{
            payTypeClick = payTypeStr;
        }
        if(fragmentType == 1){
            if(Utils.isEmpty(payStartTimeStr)){
                //为空显示系统默认时间（时分）
                tvStartTime.setText("00:00");

            }else {
                //不为空显示上次选择的时间
                if(payStartTimeStr.contains(":")){
                    tvStartTime.setText(payStartTimeStr);
                    payStartTimeClick = payStartTimeStr;
                }else{
                    tvStartTime.setText("00:00");
                }


            }
            if(Utils.isEmpty(payEndTimeStr)){
                tvEndTime.setText("23:59");
            }else {
                if(payStartTimeStr.contains(":")){
                    tvEndTime.setText(payEndTimeStr);
                    payEndTimeCLick = payEndTimeStr;
                }else{
                    tvEndTime.setText("23:59");
                }

            }
        }else if(fragmentType == 2){
            if(Utils.isEmpty(payStartTimeStr)){
                //为空显示昨天日期（年月日）
                tvStartTime.setText(yesterDayStr);
            }else{
                //不为空显示上次选择的日期
                if(payStartTimeStr.contains("-")){
                    tvStartTime.setText(payStartTimeStr);
                    payStartTimeClick = payStartTimeStr;
                }else{
                    //为空显示昨天日期（年月日）
                    tvStartTime.setText(yesterDayStr);
                }

            }
            if(Utils.isEmpty(payEndTimeStr)){
                tvEndTime.setText(yesterDayStr);
            }else {
                if(payStartTimeStr.contains("-")){
                    tvEndTime.setText(payEndTimeStr);
                    payEndTimeCLick = payEndTimeStr;
                }else{
                    tvEndTime.setText(yesterDayStr);
                }

            }
        }

        if(Utils.isEmpty(payStateStr)){
            payStateStr = QueryUtil.payStateArray[0];
        }else{
            payStateClick = payStateStr;
        }




    }

    /** popupWindow初始化筛选条件 */
    private void PopWindowInitTarget(){
        //交易类型
        PopWindowPayTypeInitTarget(payTypeStr);
        //交易时间
        PopWindowPayTimeInitTarget(payStartTimeStr,payEndTimeStr);
        //交易状态
        PopWindowPayStateInitTarget(payStateStr);
    }

    /** popupWindow重置筛选条件 */
    private void PopWindowResetTarget(){

    }

    /** 交易类型Layout初始化 */
    private void PopWindowPayTypeInitTarget(String payTypeStr){
        payTypeLayout.removeAllViews();
        final ArrayList<TextView> lstext=new ArrayList<TextView>();
        lstext.clear();
        for ( int i = 0; i < QueryUtil.payTypeArray.length; i++) {
            String payTypeText=QueryUtil.payTypeArray[i];
            final TextView tvText=new TextView(context);
            tvText.setId(i);
            tvText.setTextSize(14.0f);
            tvText.setTextColor(getResources().getColor(R.color.blue_409EFF));
            tvText.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_noselector));
            tvText.setLayoutParams(new ViewGroup.LayoutParams(displayWidth/2- Utils.dip2px(context,30), ViewGroup.LayoutParams.WRAP_CONTENT));
            tvText.setPadding(20, 15, 20, 15);
            tvText.setText(payTypeText);
            tvText.setGravity(Gravity.CENTER);
            //加入父布局
            payTypeLayout.addView(tvText);
            lstext.add(tvText);

            //默认选中上次的tab
            if(payTypeStr.equals(payTypeText)){
                tvText.setTextColor(getResources().getColor(R.color.white_ffffff));
                tvText.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_selector));
                payTypeIndex = tvText.getId();
            }

            tvText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String typeStr=((TextView) v).getText().toString();
                    if(typeStr.equals(QueryUtil.payTypeArray[0])){
                        payTypeClick = QueryUtil.payTypeArray[0];
                    }else if(typeStr.equals(QueryUtil.payTypeArray[1])){
                        payTypeClick= QueryUtil.payTypeArray[1];
                    }else if(typeStr.equals(QueryUtil.payTypeArray[2])){
                        payTypeClick= QueryUtil.payTypeArray[2];
                    }else if(typeStr.equals(QueryUtil.payTypeArray[3])){
                        payTypeClick= QueryUtil.payTypeArray[3];
                    }else if(typeStr.equals(QueryUtil.payTypeArray[4])){
                        payTypeClick= QueryUtil.payTypeArray[4];
                    }else if(typeStr.equals(QueryUtil.payTypeArray[5])){
                        payTypeClick= QueryUtil.payTypeArray[5];
                    }

                    //根据上次点击按钮 id 修改按钮的颜色
                    TextView text=lstext.get(payTypeIndex);
                    text.setTextColor(getResources().getColor(R.color.blue_409EFF));
                    text.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_noselector));
                    //设置本次点击按钮的颜色
                    payTypeIndex =v.getId();
                    text = (TextView)v;
                    text.setTextColor(getResources().getColor(R.color.white_ffffff));
                    text.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_selector));

                }
            });
        }

    }
    /** 交易时间Layout初始化 */
    private void PopWindowPayTimeInitTarget(String payStartTimeStr,String payEndTimeStr){

        //起始时间
        tvStartTime.setTextSize(14.0f);
        tvStartTime.setTextColor(getResources().getColor(R.color.blue_409EFF));
        tvStartTime.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_noselector));
        tvStartTime.setLayoutParams(new LinearLayout.LayoutParams(displayWidth/2-Utils.dip2px(context,30), LinearLayout.LayoutParams.WRAP_CONTENT));
        tvStartTime.setPadding(20, 15, 20, 15);
//        tvStartTime.setText("00:00:00");
        tvStartTime.setGravity(Gravity.CENTER);
        //结束时间
        tvEndTime.setTextSize(14.0f);
        tvEndTime.setTextColor(getResources().getColor(R.color.blue_409EFF));
        tvEndTime.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_noselector));
        tvEndTime.setLayoutParams(new LinearLayout.LayoutParams(displayWidth/2-Utils.dip2px(context,30), LinearLayout.LayoutParams.WRAP_CONTENT));
        tvEndTime.setPadding(20, 15, 20, 15);
//        tvEndTime.setText("00:00:00");
        tvEndTime.setGravity(Gravity.CENTER);


    }

    /** 交易类型Layout初始化 */
    private void PopWindowPayStateInitTarget(String payStateStr){
        payStateLayout.removeAllViews();
        final ArrayList<TextView> lstext=new ArrayList<TextView>();
        lstext.clear();
        for ( int i = 0; i < QueryUtil.payStateArray.length; i++) {
            String payStateText = QueryUtil.payStateArray[i];
            final TextView tvText=new TextView(context);
            tvText.setId(i);
            tvText.setTextSize(14.0f);
            tvText.setTextColor(getResources().getColor(R.color.blue_409EFF));
            tvText.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_noselector));
            tvText.setLayoutParams(new ViewGroup.LayoutParams(displayWidth/2-Utils.dip2px(context,30), ViewGroup.LayoutParams.WRAP_CONTENT));
            tvText.setPadding(20, 15, 20, 15);
            tvText.setText(payStateText);
            tvText.setGravity(Gravity.CENTER);
            //加入父布局
            payStateLayout.addView(tvText);
            lstext.add(tvText);

            //默认选中上次的tab
            if(payStateStr.equals(payStateText)){
                tvText.setTextColor(getResources().getColor(R.color.white_ffffff));
                tvText.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_selector));
                payStateIndex = tvText.getId();
            }

            tvText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String typeStr=((TextView) v).getText().toString();
                    if(typeStr.equals(QueryUtil.payStateArray[0])){
                        payStateClick = QueryUtil.payStateArray[0];
                    }else if(typeStr.equals(QueryUtil.payStateArray[1])){
                        payStateClick= QueryUtil.payStateArray[1];
                    }else if(typeStr.equals(QueryUtil.payStateArray[2])){
                        payStateClick= QueryUtil.payStateArray[2];
                    }else if(typeStr.equals(QueryUtil.payStateArray[3])){
                        payStateClick= QueryUtil.payStateArray[3];
                    }

                    //根据上次点击按钮 id 修改按钮的颜色
                    TextView text=lstext.get(payStateIndex);
                    text.setTextColor(getResources().getColor(R.color.blue_409EFF));
                    text.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_noselector));
                    //设置本次点击按钮的颜色
                    payStateIndex =v.getId();
                    text = (TextView)v;
                    text.setTextColor(getResources().getColor(R.color.white_ffffff));
                    text.setBackgroundDrawable(getResources().getDrawable(R.drawable.textbg_selector));

                }
            });
        }


    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent in=new Intent();
                in.putExtra("payType",payTypeStr);
                in.putExtra("payStartTime",payStartTimeStr);
                in.putExtra("payEndTime",payEndTimeStr);
                in.putExtra("payState",payStateStr);
                in.putExtra("payTypeIndex",payTypeIndex);
                in.putExtra("payStateIndex",payStateIndex);
                in.putExtra("fragmentType",fragmentType);

                setResult(RESULT_CODE, in);
                Log.e(TAG,"1筛选条件接收返回值：" +
                        "交易类型=" + payTypeStr+
                        ",起始时间="+payStartTimeStr+
                        ",结束时间="+payEndTimeStr+
                        ",交易状态="+payStateStr+
                        ",payTypeIndex="+payTypeIndex+
                        ",payStateIndex="+payStateIndex+
                        ",fragmentType="+fragmentType);
                finish();
                break;
//                return true;


        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onClick(View v) {
        Intent in = null;
        switch (v.getId()){
            case R.id.main_home_topPop_tvReset://popupWindow重置按钮
//                //交易类型
//                PopWindowPayTypeInitTarget(payTypeStr);
//                //交易时间
//                PopWindowPayTimeInitTarget(payStartTimeStr,payEndTimeStr);
//                //交易状态
//                PopWindowPayStateInitTarget(payStateStr);


                //交易类型
                payTypeClick = QueryUtil.payTypeArray[0];
                PopWindowPayTypeInitTarget(payTypeClick);
                //交易时间
                if(fragmentType == 1){
                    tvStartTime.setText(timeStr);
                    tvEndTime.setText(timeStr);
                }else if(fragmentType == 2){
                    tvStartTime.setText(yesterDayStr);
                    tvEndTime.setText(yesterDayStr);
                }
                payStartTimeClick = "";
                payEndTimeCLick = "";
                PopWindowPayTimeInitTarget(payStartTimeStr,payEndTimeStr);
                //交易状态
                payStateClick = QueryUtil.payStateArray[0];
                PopWindowPayStateInitTarget(payStateClick);
                break;
            case R.id.main_home_topPop_tvConfirm://popupWindow确定按钮

                payTypeStr = payTypeClick;
                payStartTimeStr = payStartTimeClick;
                payEndTimeStr = payEndTimeCLick;
                payStateStr = payStateClick;
                in=new Intent();
                in.putExtra("payType",payTypeStr);
                in.putExtra("payStartTime",payStartTimeStr);
                in.putExtra("payEndTime",payEndTimeStr);
                in.putExtra("payState",payStateStr);
                in.putExtra("payTypeIndex",payTypeIndex);
                in.putExtra("payStateIndex",payStateIndex);
                in.putExtra("fragmentType",fragmentType);
                Log.e(TAG,"2筛选条件接收返回值：" +
                        "交易类型=" + payTypeStr+
                        ",起始时间="+payStartTimeStr+
                        ",结束时间="+payEndTimeStr+
                        ",交易状态="+payStateStr+
                        ",payTypeIndex="+payTypeIndex+
                        ",payStateIndex="+payStateIndex+
                        ",fragmentType="+fragmentType);

                setResult(RESULT_CODE, in);
                finish();

                break;
            case R.id.main_home_topPop_startTimeText://起始时间
                if(fragmentType == 1){
                    /**
                     * 设置时间（时分）
                     */
                    timePicker = new CustomDatePicker(this,"请选择时间",new CustomDatePicker.ResultHandler() {
                        @Override
                        public void handle(String time) {
                            tvStartTime.setText(time.split(" ")[1]);
                            payStartTimeClick = time.split(" ")[1];
                        }
                    },"2007-01-01 00:00","2017-12-31 23:59");

                    timePicker.showSpecificTime(3);
                    timePicker.setIsLoop(false);
                    // HH:mm
                    timePicker.show(dataTimeStr);
                }else{
                    /**
                     * 设置年月日
                     */
                    datePicker = new CustomDatePicker(this, "请选择日期", new CustomDatePicker.ResultHandler() {
                        @Override
                        public void handle(String time) {
                            tvStartTime.setText(time.split(" ")[0]);
                            payStartTimeClick = time.split(" ")[0];
                        }
                    }, "2007-01-01 00:00", dataTimeStr);
                    datePicker.showSpecificTime(1); //不显示时和分为false
                    datePicker.setIsLoop(false);
                    datePicker.setDayIsLoop(true);
                    datePicker.setMonIsLoop(true);

                    datePicker.show(dataTimeStr);
                }

                break;
            case R.id.main_home_topPop_endTimeText://结束时间
                if(fragmentType == 1){
                    /**
                     * 设置时间（时分）
                     */
                    timePicker = new CustomDatePicker(this,"请选择时间",new CustomDatePicker.ResultHandler() {
                        @Override
                        public void handle(String time) {
                            tvEndTime.setText(time.split(" ")[1]);
                            payEndTimeCLick = time.split(" ")[1];

                        }
                    },"2007-01-01 00:00","2017-12-31 23:59");

                    timePicker.showSpecificTime(3);
                    timePicker.setIsLoop(false);
                    // HH:mm
                    timePicker.show(dataTimeStr);
                }else{
                    /**
                     * 设置年月日
                     */
                    datePicker = new CustomDatePicker(this, "请选择日期", new CustomDatePicker.ResultHandler() {
                        @Override
                        public void handle(String time) {
                            tvEndTime.setText(time.split(" ")[0]);
                            payEndTimeCLick = time.split(" ")[0];
                        }
                    }, "2007-01-01 00:00", dataTimeStr);
                    datePicker.showSpecificTime(1); //不显示时和分为false
                    datePicker.setIsLoop(false);
                    datePicker.setDayIsLoop(true);
                    datePicker.setMonIsLoop(true);

                    datePicker.show(dataTimeStr);
                }


                break;
        }
    }
}
