package com.wanding.notice.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wanding.notice.R;
import com.wanding.notice.activity.OrderDetailsActivity;
import com.wanding.notice.bean.OrderDetailData;
import com.wanding.notice.query.util.QueryUtil;
import com.wanding.notice.utils.DateTimeUtil;
import com.wanding.notice.utils.ToastUtils;
import com.wanding.notice.utils.Utils;

import java.util.List;

/**
 *  首页列表Adapter
 */
public class QueryOrderListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


    private Context context;
    private List<OrderDetailData> lsOrder;

    //普通布局的type
    static final int TYPE_ITEM = 0;
    //脚布局
    static final int TYPE_FOOTER = 1;
    //  //上拉加载更多
    //  static final int PULL_LOAD_MORE = 0;
    //正在加载更多
    static final int LOADING_MORE = 1;
    //没有更多
    static final int NO_MORE = 2;
    //脚布局当前的状态,默认为没有更多
    int footer_state = 0;

    private String isHistory;

    public QueryOrderListAdapter(Context context, List<OrderDetailData> lsOrder, int state,String isHistory) {
        this.context = context;
        this.lsOrder = lsOrder;
        this.footer_state = state;
        this.isHistory = isHistory;
    }

    /** 初始化ItemView */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        if (viewType == TYPE_ITEM) {
            //实例化Item
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_fragment_home_recyview_item,parent,false);
            //实例化ViewHolder
            ListViewHolder viewHolder = new ListViewHolder(view);

            return viewHolder;
        } else if (viewType == TYPE_FOOTER) {
            //脚布局
            View view = View.inflate(parent.getContext(), R.layout.recycler_load_more_layout, null);
            FootViewHolder footViewHolder = new FootViewHolder(view);

            return footViewHolder;
        }
        return null;
    }

    /** 数据绑定，item赋值  */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if (vh instanceof ListViewHolder) {
            final OrderDetailData order = lsOrder.get(position);
            ListViewHolder listViewHolder = (ListViewHolder) vh;
            ((ListViewHolder) vh).itemViewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    ToastUtils.showText(context,"position");
                    Intent in = new Intent();
                    in.setClass(context, OrderDetailsActivity.class);
                    in.putExtra("order",order);
                    in.putExtra("isHistory",isHistory);
                    context.startActivity(in);

                }
            });

            //交易类别:微信支付收款、微信退款成功
            String payTypeStr = order.getPayWay();
            String payType = "";
            if(Utils.isNotEmpty(payTypeStr)){
                payType = QueryUtil.getPayTypeName(payTypeStr);
            }
            if(payType.equals("微信")){
                listViewHolder.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.wxin_log_icon));
            }else if(payType.equals("支付宝")){
                listViewHolder.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.ali_log_icon));
            }else if(payType.equals("翼支付")){
                listViewHolder.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.best_log_icon));
            }else if(payType.equals("贷记卡")){
                listViewHolder.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.debit_log_icon));
            }else if(payType.equals("借记卡")){
                listViewHolder.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.credit_log_icon));
            }else if(payType.equals("银联二维码")){
                listViewHolder.imgOrderPayType.setImageDrawable(context.getResources().getDrawable(R.drawable.unionpay_log_icon));
            }
            //交易类型（0,正向 收款成功，1，反向，退款成功）
            String orderTypeStr = order.getOrderType();
            String orderType = "";
            String symbolStr = "";
            if(Utils.isNotEmpty(orderTypeStr)){
                if(orderTypeStr.equals("0")){
                    orderType = "支付收款成功";
                }else if(orderTypeStr.equals("1")){
                    orderType = "退款成功";
                    symbolStr = "-";
                }
            }
            listViewHolder.tvOrderPayType.setText(payType+orderType);

            //订单交易时间
            String orderTimeStr = order.getPayTime();
            String orderPayTime = "";
            if(orderTimeStr!=null&&!orderTimeStr.equals("")&&!orderTimeStr.equals("null")){
                orderPayTime = DateTimeUtil.stampToFormatDate(Long.parseLong(orderTimeStr), "yyyy-MM-dd HH:mm:ss");
            }
            listViewHolder.tvOrderTime.setText(orderPayTime);

            //订单交易金额
            String orderTotalStr = order.getGoodsPrice();
            String orderTotal = "";
            if(orderTotalStr!=null&&!orderTotalStr.equals("")&&!orderTotalStr.equals("null")){
                orderTotal = orderTotalStr;
            }
            listViewHolder.tvOrderTotal.setText(symbolStr+orderTotal);

        } else if (vh instanceof FootViewHolder) {
            FootViewHolder footViewHolder = (FootViewHolder) vh;
            if (position == 0) {
                //如果第一个就是脚布局,,那就让他隐藏
                footViewHolder.mProgressBar.setVisibility(View.GONE);
                footViewHolder.tv_state.setText("");
            }
            switch (footer_state) {//根据状态来让脚布局发生改变
//        case PULL_LOAD_MORE://上拉加载
//          footViewHolder.mProgressBar.setVisibility(View.GONE);
//          footViewHolder.tv_state.setText("上拉加载更多");
//          break;
                case LOADING_MORE:
                    footViewHolder.mProgressBar.setVisibility(View.VISIBLE);
                    footViewHolder.tv_state.setText("正在加载...");
                    break;
                case NO_MORE:
                    footViewHolder.mProgressBar.setVisibility(View.GONE);
                    footViewHolder.tv_state.setText("已加载全部");
                    footViewHolder.tv_state.setTextColor(Color.parseColor("#ff00ff"));
                    break;
                    default:
                        footViewHolder.mProgressBar.setVisibility(View.GONE);
                        footViewHolder.tv_state.setText("------------  没有更多  ------------");
                        break;
            }
        }
    }




    @Override
    public int getItemViewType(int position) {
        //如果position加1正好等于所有item的总和,说明是最后一个item,将它设置为脚布局
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return lsOrder != null ? lsOrder.size() + 1 : 0;
//        return lsOrder == null ? 0 : lsOrder.size();
    }

    /**
     * 创建ViewHolder
     */
    public static class ListViewHolder extends RecyclerView.ViewHolder{

        LinearLayout itemViewLayout;
        ImageView imgOrderPayType;//交易类型
        TextView tvOrderPayType;//交易类型状态
        TextView tvOrderTime;
        TextView tvOrderTotal;

        public ListViewHolder(View itemView) {
            super(itemView);
            itemViewLayout = itemView.findViewById(R.id.main_home_item_itemView);
            imgOrderPayType = itemView.findViewById(R.id.main_home_item_imgOrderPayType);
            tvOrderPayType = itemView.findViewById(R.id.main_home_item_tvOrderPayType);
            tvOrderTime = itemView.findViewById(R.id.main_home_item_tvOrderTime);
            tvOrderTotal = itemView.findViewById(R.id.main_home_item_tvOrderTotal);
        }
    }

    /**
     * 创建加载更多ViewHolder
     */
    /**
     * 脚布局的ViewHolder
     */
    public static class FootViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;
        private TextView tv_state;


        public FootViewHolder(View itemView) {
            super(itemView);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.recycler_load_more_progressbar);
            tv_state = (TextView) itemView.findViewById(R.id.recycler_load_more_tvHint);

        }
    }

    /**
     * 改变脚布局的状态的方法,在activity根据请求数据的状态来改变这个状态
     *
     * @param state
     */
    public void changeState(int state) {
        this.footer_state = state;
        notifyDataSetChanged();
    }

    public interface OnClickItemListener {
        void onClickItem(View view, String tag);
    }

    private OnClickItemListener listener;

    public void setOnClickItemListener(OnClickItemListener listener) {
        this.listener = listener;
    }

}
