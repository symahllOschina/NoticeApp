package com.wanding.notice.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.wanding.notice.R;
import com.wanding.notice.adapter.QueryOrderListAdapter;
import com.wanding.notice.base.BaseFragment;
import com.wanding.notice.bean.OrderDetailData;
import com.wanding.notice.bean.OrderListData;
import com.wanding.notice.bean.UserBean;
import com.wanding.notice.division.MyDividerItemDecoration;
import com.wanding.notice.httputils.HttpURLConnectionUtil;
import com.wanding.notice.httputils.NetworkUtils;
import com.wanding.notice.query.util.QueryDateTime;
import com.wanding.notice.query.util.QueryUtil;
import com.wanding.notice.utils.GsonUtils;
import com.wanding.notice.utils.MySerialize;
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页Fragment
 */
@SuppressLint("ValidFragment")
public class QueryHistoryFragment extends BaseFragment implements View.OnClickListener {


    private int mCurIndex = -1;
    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;
    /**
     * 是否已被加载过一次，第二次就不再去请求数据了
     */
    private boolean mHasLoadedOnce;
    private boolean onResume = true;//onResume()方法初始化不执行


    private Context context;
    private static final String TAG = "QuerySamedarFragment";

    private TextView tvTotalMoney, tvTotalNum;//总金额，总笔数
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;


    private RecyclerView.Adapter myAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    //交易列表
    private List<OrderDetailData> lsOrder = new ArrayList<OrderDetailData>();
    //交易总条数
    private int orderListTotalCount = 0;
    //每次上拉获取的条数
    private int getMoerNum = 0;
    /**
     * 分页
     */
    private int pageNum = 1;//默认加载第一页
    private static final int pageNumCount = 20;//默认一页加载xx条数据（死值不变）
    private static final int REFRESH = 100;
    private static final int LOADMORE = 200;
    private boolean loadMore = false;//loadMore为true表示加载更多操作，false表示刷新操作
    private int visibleLastIndex = 0;//最后的可视项索引
    int visibleItemCountNum;        // 当前窗口可见项总数

    private com.wanding.notice.bean.UserBean userBean;
    private String payTypeStr = "", payStartTimeStr = "", payEndTimeStr = "", payStateStr = "";//
    private String queryMid = "", querySid = "",queryEid = "";

    @SuppressLint("ValidFragment")
    public QueryHistoryFragment(UserBean userBean, String queryMid, String querySid, String queryEid, String payTypeStr, String payStartTimeStr, String payEndTimeStr, String payStateStr) {
        this.userBean = userBean;
        this.queryMid = queryMid;
        this.querySid = querySid;
        this.queryEid = queryEid;
        this.payTypeStr = payTypeStr;
        this.payStartTimeStr = payStartTimeStr;
        this.payEndTimeStr = payEndTimeStr;
        this.payStateStr = payStateStr;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.query_history_fragment, null, false);
        context = view.getContext();
        initView(view);
        initListener();
        initData();
        isPrepared = true;
        lazyLoad();
        //因为共用一个Fragment视图，所以当前这个视图已被加载到Activity中，必须先清除后再加入Activity
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        onResume = false;

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (onResume) {
            //请求数据
            if(pageNum == 1){
                getOrderList(pageNum,pageNumCount);
            }else{
                pageNum = 1;
                getOrderList(pageNum,lsOrder.size());
            }
        }
    }

    /**
     * 初始化控件
     */
    private void initView(View view) {
        tvTotalMoney = view.findViewById(R.id.query_statis_bar_tvMoney);
        tvTotalNum = view.findViewById(R.id.query_statis_bar_tvNum);
        mSwipeRefreshLayout = view.findViewById(R.id.query_history_swipeRefreshLayout);
        mRecyclerView = view.findViewById(R.id.query_history_recyclerView);

        //设置进度圈的大小;(这里面只有两个值SwipeRefreshLayout.LARGE和DEFAULT，后者是默认效果)
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        //设置进度圈的背景色。这里随便给他设置了一个颜色：浅绿色
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.CYAN);
        //设置进度动画的颜色。这里面最多可以指定四个颜色，先随机设置的
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_orange_dark
                , android.R.color.holo_blue_dark
                , android.R.color.holo_red_dark
                , android.R.color.widget_edittext_dark);

        //设置手势监听  (下拉刷新)
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMore = false;//记录该操作为刷新操作还是加载更多操作
                //刷新数据(刷新主要获取第一页的新数据)
                if (pageNum == 1) {
                    getOrderList(pageNum, pageNumCount);
                } else {
                    pageNum = 1;
                    getOrderList(pageNum, lsOrder.size());
                }

            }
        });

        //给recyclerView添加滑动监听
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /*
                到达底部了,如果不加!isLoading的话到达底部如果还一滑动的话就会一直进入这个方法
                就一直去做请求网络的操作,这样的用户体验肯定不好.添加一个判断,每次滑倒底只进行一次网络请求去请求数据
                当请求完成后,在把isLoading赋值为false,下次滑倒底又能进入这个方法了
                 */
                if (newState == RecyclerView.SCROLL_STATE_IDLE && visibleLastIndex + 1 == myAdapter.getItemCount()) {
                    //到达底部之后如果footView的状态不是正在加载的状态,就将 他切换成正在加载的状态

                    if (lsOrder.size() <= orderListTotalCount && getMoerNum == pageNumCount) {
                        //已取出数据条数<=服务器端总条数&&上一次上拉取出的条数 == 规定的每页取出条数时代表还有数据库还有数据没取完
                        Log.e("duanlian", "onScrollStateChanged: " + "进来了");
                        loadMore = true;
                        pageNum = pageNum + 1;
                        getOrderList(pageNum, pageNumCount);
                    } else {
                        //没有数据执行两秒关闭view
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                Message msg = new Message();
                                msg.what = LOADMORE;
                                msg.arg1 = visibleLastIndex - visibleItemCountNum + 1;
                                handler.sendMessage(msg);

                            }
                        }, 2000);

                    }

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //为RecyclerView 可见的第一item的position
                int firstVisibleItem = ((LinearLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                //拿到最后一个出现的item的位置(为RecyclerView 可见的最后一个item的position)
                visibleLastIndex = ((LinearLayoutManager) mLayoutManager).findLastCompletelyVisibleItemPosition();
                //visibleItemCount 为RecyclerView 当前可见item的数量
                int visibleItemCount = mLayoutManager.getChildCount();
                //totalItemCount 为RecyclerView 的所有item的总数量
                int totalItemCount = mLayoutManager.getItemCount();

                visibleItemCountNum = visibleItemCount;
                visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
            }
        });
        //初始化布局管理器（RecyclerView为线性垂直方式展示列表）
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        // 设置Item之间间隔样式
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(context, LinearLayoutManager.VERTICAL));

    }

    /**
     * 注册监听
     */
    private void initListener() {

    }

    /**
     * 初始化数据
     */
    private void initData() {

    }

    /**
     * 重写父类方法（fragment可见时加载界面数据）
     */
    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }
        //请求数据
        getOrderList(pageNum, pageNumCount);
    }

    public void setPamase(String queryMid, String querySid, String queryEid, String payTypeStr, String payStartTimeStr, String payEndTimeStr, String payStateStr) {
        this.queryMid = queryMid;
        this.querySid = querySid;
        this.queryEid = queryEid;
        this.payTypeStr = payTypeStr;
        this.payStartTimeStr = payStartTimeStr;
        this.payEndTimeStr = payEndTimeStr;
        this.payStateStr = payStateStr;

        pageNum = 1;
        getOrderList(pageNum, pageNumCount);
    }

    /**
     * 获取历史交易明细
     **/
    private void getOrderList(final int pageNum,final int pageCount){
        final String  url = NitConfig.queryOrderHistoryListUrl;
        new Thread(){
            @Override
            public void run() {

                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject jsonStr = new JSONObject();
                    jsonStr.put("pageNum",pageNum+"");
                    jsonStr.put("numPerPage",pageCount+"");
                    jsonStr.put("roleId", userBean.getRoleId());
                    jsonStr.put("role",userBean.getRole());
                    jsonStr.put("startTime", QueryDateTime.getStartTimeStampTo(payStartTimeStr));
                    jsonStr.put("endTime",QueryDateTime.getEndTimeStampTo(payEndTimeStr));
                    jsonStr.put("payWay", QueryUtil.getPayTypeStr(payTypeStr));
                    jsonStr.put("status","");
                    jsonStr.put("orderType",QueryUtil.getPayStateStr(payStateStr));
                    //角色：("shop","商户"),("employee","员工"),("store","门店")
                    String roleStr = userBean.getRole();
                    if(roleStr.equals("shop")){
                        //商户登录
                        jsonStr.put("mid",queryMid);
                        jsonStr.put("sid",querySid);
                        jsonStr.put("eid",queryEid);
                    }else if(roleStr.equals("store")){
                        //门店登录
                        jsonStr.put("mid",queryMid);
                        jsonStr.put("sid",querySid);
                        jsonStr.put("eid",queryEid);
                    }else if(roleStr.equals("employee")){
                        //款台登录
                        jsonStr.put("mid",queryMid);
                        jsonStr.put("sid",querySid);
                        jsonStr.put("eid",queryEid);
                    }
                    String content = String.valueOf(jsonStr);
                    Log.e("发起请求参数：", content);
                    String resultJsonStr = HttpURLConnectionUtil.doPos(url,content);
                    //{"data":{"sid":"1971","eid":"89","mid":"200","account":"100014510111"},"message":"","status":200}
                    Log.e("返回字符串结果：", resultJsonStr);
                    int msg = 1;
                    String text = resultJsonStr;
                    sendMessage(msg,text);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(NetworkUtils.SERVICE_CODE,NetworkUtils.SERVICE_TEXT);
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
                case REFRESH:
                    if(myAdapter!=null){
                        myAdapter.notifyDataSetChanged();
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                    loadMore = true;
                    break;
                case LOADMORE:   //数据集变化后,通知adapter
//                    int position = msg.arg1;
//                    mRecyclerView.setSelection(position);    //设置选中项
                    if(myAdapter!=null){
                        myAdapter.notifyDataSetChanged();
                    }
                    loadMore = false;
                    break;
                case 1:
                    String orderJsonStr=(String) msg.obj;
                    GsonOrderListReturnJson(orderJsonStr);
                    onResume = true;
                    break;
                case 201:
                    String errorJsonText = (String) msg.obj;
                    loadMore = true?false:true;
                    mSwipeRefreshLayout.setRefreshing(false);
                    onResume = true;
                    break;
                case 400:
                    String errorServiceText = (String) msg.obj;
                    loadMore = true?false:true;
                    mSwipeRefreshLayout.setRefreshing(false);
                    onResume = true;
                    break;
            }
        }
    };


    private void GsonOrderListReturnJson(String jsonStr){
        String isHistory = "";
        try {
            JSONObject job = new JSONObject(jsonStr);
            if(job.getString("status").equals("200")){
                String dataJson = job.getString("data");
                Gson gjson  =  GsonUtils.getGson();
                java.lang.reflect.Type type = new TypeToken<OrderListData>() {}.getType();
                OrderListData order = gjson.fromJson(dataJson, type);
                //获取总条数
                tvTotalMoney.setText(order.getSumAmt());
                tvTotalNum.setText(String.valueOf(order.getCountRow()));
                orderListTotalCount = order.getTotalCount();
                Log.e(TAG,"总条数："+orderListTotalCount+"");
                isHistory = order.getIsHistory();
                List<OrderDetailData> orderList = new ArrayList<OrderDetailData>();
                //获取的list
                orderList = order.getOrderList();
                getMoerNum = orderList.size();
                if(pageNum == 1){
                    lsOrder.clear();
                }
                lsOrder.addAll(orderList);
                Log.e("查询数据：", lsOrder.size()+""+"条");

                myAdapter = new QueryOrderListAdapter(context, lsOrder,0,isHistory);
                // 设置布局管理器
                mRecyclerView.setLayoutManager(mLayoutManager);
                // 设置adapter
                mRecyclerView.setAdapter(myAdapter);
                //关闭上拉或下拉View，刷新Adapter
                if(loadMore){
                    Message msg1 = new Message();
                    msg1.what = LOADMORE;
                    msg1.arg1 = visibleLastIndex - visibleItemCountNum + 1;
                    handler.sendMessage(msg1);
                }else{
                    handler.sendEmptyMessageDelayed(REFRESH, 0);
                }
            }else if(job.getString("status").equals("300")){
                lsOrder.clear();
                myAdapter = new QueryOrderListAdapter(context, lsOrder,0,isHistory);
//                myAdapter.set

                // 设置布局管理器
                mRecyclerView.setLayoutManager(mLayoutManager);
                // 设置adapter
                mRecyclerView.setAdapter(myAdapter);
//                Toast.makeText(getContext(), "查询失败！", Toast.LENGTH_LONG).show();
                myAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getContext(), "查询失败！", Toast.LENGTH_LONG).show();
            }

        } catch (JsonSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            handler.sendEmptyMessageDelayed(REFRESH, 0);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            handler.sendEmptyMessageDelayed(REFRESH, 0);
        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            handler.sendEmptyMessageDelayed(REFRESH, 0);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }
}
