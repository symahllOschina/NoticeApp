package com.wanding.notice.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.wanding.notice.utils.DecimalUtil;
import com.wanding.notice.utils.GsonUtils;
import com.wanding.notice.utils.MD5;
import com.wanding.notice.utils.MySerialize;
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.SharedPreferencesUtil;
import com.wanding.notice.utils.ToastUtils;
import com.wanding.notice.utils.Utils;
import com.wanding.notice.view.CustomDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 订单详情
 */
public class OrderDetailsActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private static final String TAG = "OrderDetailsActivity";
    private ImageView imgBack;
    private TextView tvTitle;

    private ImageView imgPayState;
    private TextView tvPayState;

    private View viewRefundAmount;
    private RelativeLayout layoutRefundAmount;

    private TextView tvPayMoneyTitle,tvPayMoney,tvRefundAmount,tvPayType,tvBusName,tvTerminalName,tvPayWaterNum,tvPayCreateTime,tvPayOrderId;



    private Button btRefund;//退款


    private UserBean userBean;
    private OrderDetailData order;
    private String isHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail_activity);
        context = OrderDetailsActivity.this;

        initView();
        initListener();
        initData();



    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isHistory.equals("N")){
            String url = NitConfig.getOrderDetailsUrl;
            getOrderDetails(url);
            Log.e("订单详情：","实时订单");
        }else if(isHistory.equals("Y")){
            String url = NitConfig.getOrderHistoryDetailsUrl;
            getOrderDetails(url);

            Log.e("订单详情：","历史订单");
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        imgBack = findViewById(R.id.header_back);
        tvTitle = findViewById(R.id.header_title);

        imgPayState = findViewById(R.id.order_details_imgPayState);
        tvPayState = findViewById(R.id.order_details_tvPayState);

        viewRefundAmount = findViewById(R.id.order_details_viewRefundAmount);
        layoutRefundAmount = findViewById(R.id.order_details_layoutRefundAmount);

        tvPayMoneyTitle = findViewById(R.id.order_details_tvPayMoneyTitle);
        tvPayMoney = findViewById(R.id.order_details_tvPayMoney);
        tvRefundAmount = findViewById(R.id.order_details_tvRefundAmount);
        tvBusName = findViewById(R.id.order_details_tvBusName);
        tvTerminalName = findViewById(R.id.order_details_tvTerminalName);
        tvPayType = findViewById(R.id.order_details_tvPayType);
        tvPayWaterNum = findViewById(R.id.order_details_tvPayWaterNum);
        tvPayCreateTime = findViewById(R.id.order_details_tvPayCreateTime);
        tvPayOrderId = findViewById(R.id.order_details_tvPayOrderId);

        btRefund = findViewById(R.id.order_details_btRefund);

        tvTitle.setText("交易详情");
    }

    /**
     * 事件监听
     */
    private void initListener() {
        imgBack.setOnClickListener(this);
        btRefund.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
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
        isHistory = (String) in.getSerializableExtra("isHistory");

    }

    /**
     * 更新界面数据
     */
    private void updateView(){
        //private String role;//角色：("shop","商户"),("employee","员工"),("store","门店"),
        //判断是支付交易还是退款交易 0正向 ,1退款,其中正向包括支付交易和退款交易
        String roleStr = userBean.getRole();
        String orderTypeStr = order.getOrderType();
        String orderStateStr = order.getStatus();
        if(roleStr.equals("employee")){
            if(isHistory.equals("N")){
                btRefund.setVisibility(View.VISIBLE);
            }else {
                btRefund.setVisibility(View.GONE);
            }
        }else{
            if(isHistory.equals("N")){
                btRefund.setVisibility(View.VISIBLE);
            }else {
                btRefund.setVisibility(View.GONE);
            }
        }

        //交易状态
        if(Utils.isNotEmpty(orderTypeStr)){
            if(Utils.isNotEmpty(orderStateStr)){
                if(orderTypeStr.equals("0")){
                    imgPayState.setImageDrawable(getResources().getDrawable(R.drawable.success_icon));
                    tvPayState.setText("收款成功");
                    tvPayMoneyTitle.setText("收款金额");
                    btRefund.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_blue_frame1dp_radius10));
                    btRefund.setTextColor(getResources().getColor(R.color.white_ffffff));
                    btRefund.setEnabled(true);
                    if(orderStateStr.equals("4")){
                        imgPayState.setImageDrawable(getResources().getDrawable(R.drawable.success_icon));
                        tvPayState.setText("收款成功");
                        tvPayMoneyTitle.setText("收款金额");
                        btRefund.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_gray_frame1dp_radius10));
                        btRefund.setTextColor(getResources().getColor(R.color.white_f8f8f8));
                        btRefund.setEnabled(false);
                    }
//                refundAmountLayout.setVisibility(View.GONE);
                }else if(orderTypeStr.equals("1")){
                    imgPayState.setImageDrawable(getResources().getDrawable(R.drawable.refund_icon));
                    tvPayState.setText("已退款");
                    tvPayMoneyTitle.setText("退款金额");
                    btRefund.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_gray_frame1dp_radius10));
                    btRefund.setTextColor(getResources().getColor(R.color.white_f8f8f8));
                    btRefund.setEnabled(false);
//                refundAmountLayout.setVisibility(View.GONE);
                }
            }
        }


        //交易金额
        String payMoneyStr = order.getGoodsPrice();
        if(Utils.isNotEmpty(payMoneyStr)){
            tvPayMoney.setText(payMoneyStr);
        }
        //退款金额
        String refundAmountStr = order.getRefundAmount();
        String refundAmount = "0.00";
        if(Utils.isNotEmpty(orderTypeStr)){
            if(orderTypeStr.equals("0")){
                viewRefundAmount.setVisibility(View.VISIBLE);
                layoutRefundAmount.setVisibility(View.VISIBLE);
                if(Utils.isNotEmpty(refundAmountStr)){
                    refundAmount = refundAmountStr;
                }

            }else if(orderTypeStr.equals("1")){
                viewRefundAmount.setVisibility(View.GONE);
                layoutRefundAmount.setVisibility(View.GONE);
            }

        }
        tvRefundAmount.setText(refundAmount);

        //交易类别
        String payTypeStr = order.getPayWay();
        String payType = "未知";
        if(Utils.isNotEmpty(payTypeStr)){
            payType = QueryUtil.getPayTypeName(payTypeStr);
        }
        tvPayType.setText(payType);

        //门店名称
        String busNameStr = order.getStoreName();
        if(Utils.isNotEmpty(busNameStr)){
            tvBusName.setText(busNameStr);
        }
        //终端名称
        String terminalNameStr = order.getUsername();
        if(Utils.isNotEmpty(terminalNameStr)){
            tvTerminalName.setText(terminalNameStr);
        }
        //终端流水
        String transactionIdStr = order.getTransactionId();
        if(Utils.isNotEmpty(transactionIdStr)){
            tvPayWaterNum.setText(transactionIdStr);
        }
        //日期时间
        String orderTimeStr = order.getPayTime();
        String orderPayTime = "";
        if(Utils.isNotEmpty(orderTimeStr)){
            orderPayTime = DateTimeUtil.stampToFormatDate(Long.parseLong(orderTimeStr), "yyyy年MM月dd日 HH:mm");
        }
        tvPayCreateTime.setText(orderPayTime);
        //订单号
        String orderIdStr = order.getOrderId();
        String orderId = "";
        if(Utils.isNotEmpty(orderIdStr)){
            orderId = orderIdStr;
        }
        tvPayOrderId.setText(orderId);

    }

    /**
     * 订单详情 api/app/200/1/queryOrderDetail
     * 入参：orderId（订单主键id）
     */
    private void getOrderDetails(final String url) {

        new Thread() {
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("orderId", order.getId());
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
                    OrderDetailJSON(jsonStr_1);
                    break;
                case 201:
                    String errorJsonText = (String) msg.obj;
//                    ToastUtils.showText(context, errorJsonText);

                    break;
                case 400:
                    String errorServiceText = (String) msg.obj;
//                    ToastUtils.showText(context, errorServiceText);

                    break;
            }
        }
    };

    private void OrderDetailJSON(String str) {

        try {
            JSONObject job = new JSONObject(str);
            if (job.getString("status").equals("200")) {
                JSONObject dataObj = new JSONObject(job.getString("data"));
                String orderJson = dataObj.getString("order");
                Gson gjson  =  GsonUtils.getGson();
                order = gjson.fromJson(orderJson, OrderDetailData.class);
                updateView();

            } else {
                Toast.makeText(context, "查询失败！", Toast.LENGTH_LONG).show();
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
            case R.id.order_details_btRefund:
                Intent in = new Intent();
                in.setClass(OrderDetailsActivity.this,RefundActivity.class);
                in.putExtra("order",order);
                startActivity(in);
                break;

        }
    }
}
