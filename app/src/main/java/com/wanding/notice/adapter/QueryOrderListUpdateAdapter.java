package com.wanding.notice.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wanding.notice.R;
import com.wanding.notice.activity.OrderDetailsActivity;
import com.wanding.notice.bean.OrderDetailData;
import com.wanding.notice.query.util.QueryUtil;
import com.wanding.notice.utils.DateTimeUtil;
import com.wanding.notice.utils.Utils;

import java.util.List;

/**
 *  首页列表Adapter
 */
public class QueryOrderListUpdateAdapter extends BaseAdapter{


    private Context context;
    private List<OrderDetailData> lsOrder;
    private LayoutInflater inflater;

    public QueryOrderListUpdateAdapter(Context context, List<OrderDetailData> lsOrder) {
        this.context = context;
        this.lsOrder = lsOrder;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return lsOrder.size();
    }

    @Override
    public Object getItem(int position) {
        return lsOrder.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{

        LinearLayout itemViewLayout;
        ImageView imgOrderPayType;//交易类型
        TextView tvOrderPayType;//交易类型状态
        TextView tvOrderTime;
        TextView tvOrderTotal;
    }

    @Override
    public View getView(int position, View subView, ViewGroup parent) {
        OrderDetailData order = lsOrder.get(position);
        ViewHolder vh = null;
        if(subView == null){
            subView = inflater.inflate(R.layout.main_fragment_home_recyview_item,null);
            vh = new ViewHolder();
            vh.itemViewLayout = subView.findViewById(R.id.main_home_item_itemView);
            vh.imgOrderPayType = subView.findViewById(R.id.main_home_item_imgOrderPayType);
            vh.tvOrderPayType  = subView.findViewById(R.id.main_home_item_tvOrderPayType);
            vh.tvOrderTime = subView.findViewById(R.id.main_home_item_tvOrderTime);
            vh.tvOrderTotal  = subView.findViewById(R.id.main_home_item_tvOrderTotal);
            subView.setTag(vh);
        }else{
            vh = (ViewHolder) subView.getTag();

        }

        //交易类别:微信支付收款、微信退款成功
        String payTypeStr = order.getPayWay();
        String payType = "";
        if(Utils.isNotEmpty(payTypeStr)){
            payType = QueryUtil.getPayTypeName(payTypeStr);
        }
        if(payType.equals("微信")){
            vh.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.wxin_log_icon));
        }else if(payType.equals("支付宝")){
            vh.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.ali_log_icon));
        }else if(payType.equals("翼支付")){
            vh.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.best_log_icon));
        }else if(payType.equals("贷记卡")){
            vh.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.debit_log_icon));
        }else if(payType.equals("借记卡")){
            vh.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.credit_log_icon));
        }else if(payType.equals("银联二维码")){
            vh.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.unionpay_log_icon));
        }
        //交易类型（0,正向 收款成功，1，反向，退款成功）
        String orderTypeStr = order.getOrderType();
        String statusStr = order.getStatus();
        String orderType = "";
        String symbolStr = "";

        if(Utils.isNotEmpty(statusStr)&&statusStr.equals("5")){
            orderType = "支付状态未知";
        }else{
            if(Utils.isNotEmpty(orderTypeStr)){
                if(orderTypeStr.equals("0")){
                    orderType = "支付收款成功";
                }else if(orderTypeStr.equals("1")){
                    orderType = "退款成功";
                    symbolStr = "-";
                }
            }
        }


        vh.tvOrderPayType.setText(payType+orderType);

        //订单交易时间
        String orderTimeStr = order.getPayTime();
        String orderPayTime = "";
        if(orderTimeStr!=null&&!orderTimeStr.equals("")&&!orderTimeStr.equals("null")){
            orderPayTime = DateTimeUtil.stampToFormatDate(Long.parseLong(orderTimeStr), "yyyy-MM-dd HH:mm:ss");
        }
        vh.tvOrderTime.setText(orderPayTime);

        //订单交易金额
        String orderTotalStr = order.getGoodsPrice();
        String orderTotal = "";
        if(orderTotalStr!=null&&!orderTotalStr.equals("")&&!orderTotalStr.equals("null")){
            orderTotal = orderTotalStr;
        }
        vh.tvOrderTotal.setText(symbolStr+orderTotal);


        return subView;
    }
}
