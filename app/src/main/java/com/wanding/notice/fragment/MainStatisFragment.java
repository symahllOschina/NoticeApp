package com.wanding.notice.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wanding.notice.R;
import com.wanding.notice.activity.LoginActivity;
import com.wanding.notice.application.BaseApplication;
import com.wanding.notice.base.BaseFragment;
import com.wanding.notice.bean.SearchUserResult;
import com.wanding.notice.bean.StatisData;
import com.wanding.notice.bean.StatisListData;
import com.wanding.notice.bean.UserBean;
import com.wanding.notice.httputils.HttpURLConnectionUtil;
import com.wanding.notice.httputils.NetworkUtils;
import com.wanding.notice.statis.util.StatisDateTime;
import com.wanding.notice.utils.DecimalUtil;
import com.wanding.notice.utils.GsonUtils;
import com.wanding.notice.utils.MySerialize;
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.SharedPreferencesUtil;
import com.wanding.notice.utils.ToastUtils;
import com.wanding.notice.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/** 统计图Fragment */
public class MainStatisFragment extends BaseFragment implements View.OnClickListener{


    private int mCurIndex = -1;
    /** 标志位，标志已经初始化完成 */
    private boolean isPrepared;
    /** 是否已被加载过一次，第二次就不再去请求数据了 */
    private boolean mHasLoadedOnce;
    private boolean onResume=true;//onResume()方法初始化不执行

    private Context context;
    private LineChartView lineChart;
    private LinearLayout lineChartHintLayout;
    private ImageView lineChartHintImg;
    private Button btMoney,btNumber;//金额，笔数

    private UserBean userBean;
    private String type = "1";
    private boolean isRequest = true;

    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_statis,null,false);
        initView(view);
        initListener();
        initData();
        isPrepared = true;
        lazyLoad();
        //因为共用一个Fragment视图，所以当前这个视图已被加载到Activity中，必须先清除后再加入Activity
        ViewGroup parent = (ViewGroup)view.getParent();
        if(parent != null) {
            parent.removeView(view);
        }
        onResume=false;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(onResume){
            //请求数据
        }

    }

    /** 初始化控件 */
    private void initView(View view){
        context = getActivity();
        lineChart = view.findViewById(R.id.main_fragment_statis_lineChartView);
        lineChartHintLayout = view.findViewById(R.id.main_fragment_statis_lineChartViewHintLayout);
        lineChartHintImg = view.findViewById(R.id.main_fragment_statis_lineChartViewHintImg);
        btMoney = view.findViewById(R.id.main_fragment_statis_btMoney);
        btNumber = view.findViewById(R.id.main_fragment_statis_btNumber);

    }

    /** 注册监听 */
    private void initListener(){
        btMoney.setOnClickListener(this);
        btNumber.setOnClickListener(this);
    }

    private void initData(){
        //取出对象
        try {
            userBean=(UserBean) MySerialize.deSerialization(com.wanding.notice.utils.MySerialize.getObject("user", context));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        //默认数据
        if(Utils.isNotEmpty(getRole())){
            if(getRole().equals("shop")){
                lineChartHintLayout.setVisibility(View.GONE);
                btMoney.setEnabled(true);
                btNumber.setEnabled(true);
                isRequest = true;
            }else if(getRole().equals("store")){
                lineChartHintLayout.setVisibility(View.VISIBLE);
                lineChartHintImg.setImageDrawable(getResources().getDrawable(R.drawable.statis_hint_bg3));
                btMoney.setEnabled(false);
                btNumber.setEnabled(false);
                isRequest = false;
            }else{
                lineChartHintLayout.setVisibility(View.VISIBLE);
                lineChartHintImg.setImageDrawable(getResources().getDrawable(R.drawable.statis_hint_bg4));
                btMoney.setEnabled(false);
                btNumber.setEnabled(false);
                isRequest = false;
            }
        }
    }

    /**
     * 获取角色
     * 角色：("shop","商户"),("employee","员工"),("store","门店")
     */
    private String getRole(){
        if(userBean!=null){
            return userBean.getRole();
        }
        return "";
    }

    /**
     * 重写父类方法（fragment可见时加载界面数据）
     */
    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible|| mHasLoadedOnce) {
            return;
        }
        //测试：请求数据
//        mPointValues.clear();
//        mAxisXValues.clear();
//        String[] date = {"10-22","11-22","12-22","1-22","6-22","5-23","5-22","6-22","5-23","5-22"};//X轴的标注
//        int[] score= {500,2900,3100,850,1100,1700,1800,1300,1200,600,1000,100,1900,2100,800,0,900};//图表的数据点
//        getAxisXLables(date);//获取x轴的标注
//        getAxisPoints(score);//获取坐标点
//        initLineChart();//初始化
        if(isRequest){
            getStatisData();
        }


    }

    /**
     * 测试：设置X 轴的显示
     */
    private void getAxisXLables(String[] date){
        for (int i = 0; i < date.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(date[i]));
        }
    }

    /**
     * 测试：图表的每个点的显示
     */
    private void getAxisPoints(int[] score){
        for (int i = 0; i < score.length; i++) {
            mPointValues.add(new PointValue(i, score[i]));
        }
    }

    /**
     * 设置X 轴的显示
     */
    private void getAxisXLables(List<String> lsStr){
        Collections.reverse(lsStr);
        for (int i = 0; i < lsStr.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(lsStr.get(i)));
        }
    }

    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints(List<Integer> lsInt){

        for (int i = 0; i < lsInt.size(); i++) {
            mPointValues.add(new PointValue(i, lsInt.get(i)));
        }
    }

    /**
     * 图表的每个点的显示
     */
    private void getAxisPointsDou(List<Float> lsDou){

        for (int i = 0; i < lsDou.size(); i++) {
            mPointValues.add(new PointValue(i, lsDou.get(i)));
        }
    }

    private void initLineChart(){
        //参考：https://blog.csdn.net/u012534831/article/details/51505683
        //参考1：https://blog.csdn.net/qq_35563053/article/details/65628813

        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(true);//是否填充曲线的面积
        line.setHasLabels(false);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
//        axisX.setTextColor(Color.RED);  //设置字体颜色
//        axisX.setName("日期");  //表格名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(7); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

       // Y轴标注根据数据的大小自动设置Y轴上限
        Axis axisY = new Axis();  //Y轴
        if(type.equals("1")){
            axisY.setName("                  ");//y轴标注
        }else{
            axisY.setName("                  ");//y轴标注
        }
        axisY.setTextSize(9);//设置字体大小
        axisY.setHasLines(true);// Y 轴分割线
        data.setAxisYLeft(axisY);  //Y轴设置在左边
//        data.setAxisYRight(axisY);  //y轴设置在右边


        /*
        // 固定Y轴标注数据个数的解决方案
        Axis axisY = new Axis().setHasLines(true);
        if(type.equals("1")){
            axisY.setName("                  ");//y轴标注
        }else{
            axisY.setName("                  ");//y轴标注
        }
        axisY.setTextSize(9);//设置字体大小
        axisY.setHasLines(true);// Y 轴分割线
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        axisY.setMaxLabelChars(6);//max label length, for example 60
        List<AxisValue> values = new ArrayList<>();
        for(int i = 0; i < 50000; i+= 1000){
            AxisValue value = new AxisValue(i);
            String label = "";
            value.setLabel(label);
            values.add(value);
        }
        axisY.setValues(values);
        */



        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的v.left，v.right代表图标显示数据的下标（如全部显示则为：v.left = 0;v.right=mPointValues.length-1 ）
         */
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= mPointValues.size()-1;
        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);
    }


    /**
     * 获取统计数据
     */
    /**
     * 退款请求
     */
    private void getStatisData(){
        final String url = NitConfig.getStatisDataUrl;
        new Thread(){
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();

                    userJSON.put("role", userBean.getRole());
                    userJSON.put("roleId",userBean.getRoleId());
                    userJSON.put("startTime", StatisDateTime.getStartTimeStamp());
                    userJSON.put("endTime",StatisDateTime.getEndTimeStamp());
                    userJSON.put("type",type);
                    String content = String.valueOf(userJSON);
                    Log.e("发起请求参数：", content);
                    String jsonStr = HttpURLConnectionUtil.doPos(url,content);
                    Log.e("返回字符串结果：", jsonStr);
                    int msg = 1;
                    String text = jsonStr;
                    sendMessage(msg,text);

                } catch (JSONException e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.JSON_IO_CODE,NetworkUtils.JSON_IO_TEXT);
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
                    String jsonStr_1 = (String) msg.obj;
                    StatisResultJSON(jsonStr_1);
                    break;
                case 201:
                    String errorJsonText = (String) msg.obj;
                    ToastUtils.showText(context,errorJsonText);
                    break;
                case 400:
                    String errorServiceText = (String) msg.obj;
                    ToastUtils.showText(context,errorServiceText);
                    break;
            }
        }
    };

    private void StatisResultJSON(String str){
        try {
            JSONObject job = new JSONObject(str);
            if(job.getString("status").equals("200")){
                String dataJson = job.getString("data");
                Gson gjson  =  GsonUtils.getGson();
                java.lang.reflect.Type type = new TypeToken<StatisListData>() {}.getType();
                StatisListData statis = gjson.fromJson(dataJson, type);
                getList(statis);
            }else{
                ToastUtils.showText(getContext(),"查询失败！");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**  */
    private void getList(StatisListData statis){
        List<StatisData> statisList = new ArrayList<StatisData>();
        statisList = statis.getMerDataSumList();
        if(statisList.size()>0){
            List<String> lsDate = new ArrayList<String>();
            List<Integer> lsInt = new ArrayList<Integer>();
            List<Float> lsFloat = new ArrayList<Float>();
            lsDate.clear();
            lsInt.clear();
            lsFloat.clear();
            for (int i = 0;i<statisList.size();i++){
                StatisData statisData = statisList.get(i);
                Long dateLong = statisData.getDate();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");
                Date date = new Date(dateLong);
                String dateStr = simpleDateFormat.format(date);
                Log.e("MianStatisFragment转换时间","转换的时间："+dateStr);
                if(type.equals("1")){
                    Double moneyDou = statisData.getMoney();
                    float moneyFloat = (float)moneyDou.doubleValue();
                    Log.e("float金额：",moneyFloat+"");
                    BigDecimal b = new BigDecimal(moneyFloat);
                    float f1 =  b.setScale(2,  BigDecimal.ROUND_HALF_UP).floatValue();
                    Log.e("float金额保留两位小数：",f1+"");
                    lsFloat.add(f1);
                }else{
                    Integer moneyInt = DecimalUtil.DoubleToInteger(statisData.getMoney());
                    lsInt.add(moneyInt);
                }

                lsDate.add(dateStr);

            }
            mPointValues.clear();
            mAxisXValues.clear();
            getAxisXLables(lsDate);//获取x轴的标注
            if(type.equals("1")){
                getAxisPointsDou(lsFloat);//获取坐标点
            }else{
                getAxisPoints(lsInt);//获取坐标点
            }
            initLineChart();//初始化
        }else{
            lineChartHintLayout.setVisibility(View.VISIBLE);
            lineChartHintImg.setImageDrawable(getResources().getDrawable(R.drawable.statis_hint_bg));
        }
    }

    /**  */
    private void showNotHintDialog(int type){
        View view = LayoutInflater.from(context).inflate(R.layout.statis_nohint_dialog, null);
        ImageView imgView = view.findViewById(R.id.statis_nohint_img);
        TextView text = (TextView) view.findViewById(R.id.statis_nohint_text);
        if(type == 1){
            imgView.setVisibility(View.GONE);
            text.setVisibility(View.VISIBLE);
        }else if(type == 2){
            imgView.setVisibility(View.VISIBLE);
            text.setVisibility(View.GONE);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.statis_hint_bg3));
        }else if(type == 3){
            imgView.setVisibility(View.VISIBLE);
            text.setVisibility(View.GONE);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.statis_hint_bg4));
        }
        final Dialog myDialog = new Dialog(context,R.style.dialog);
        Window dialogWindow = myDialog.getWindow();
        WindowManager.LayoutParams params = myDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
        dialogWindow.setAttributes(params);
        myDialog.setContentView(view);
        myDialog.show();
        myDialog.setCancelable(false);
    }


    @Override
    public void onClick(View v) {
        Intent in = null;
        switch (v.getId()){
            case R.id.main_fragment_statis_btMoney://金额
                if(!type.equals("1")){
                    type = "1";
                    btMoney.setTextColor(getResources().getColor(R.color.white_ffffff));
                    btMoney.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_blue_frame1dp_radius0));
                    btNumber.setTextColor(getResources().getColor(R.color.blue_409EFF));
                    btNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_white_frame1dp_radius0));

                    getStatisData();

                }
                break;
            case R.id.main_fragment_statis_btNumber://笔数
                if(!type.equals("2")){
                    type = "2";
                    btNumber.setTextColor(getResources().getColor(R.color.white_ffffff));
                    btNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_blue_frame1dp_radius0));
                    btMoney.setTextColor(getResources().getColor(R.color.blue_409EFF));
                    btMoney.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_white_frame1dp_radius0));

                    getStatisData();

                }





                break;

        }
    }
}
