package com.wanding.notice.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.igexin.sdk.PushManager;
import com.wanding.notice.R;
import com.wanding.notice.activity.OrderDetailsActivity;
import com.wanding.notice.adapter.QueryOrderListAdapter;
import com.wanding.notice.adapter.QueryOrderListUpdateAdapter;
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
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.SharedPreferencesUtil;
import com.wanding.notice.utils.ToastUtils;
import com.wanding.notice.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 首页Fragment */
@SuppressLint("ValidFragment")
public class QuerySamedarUpdateFragment extends BaseFragment implements AdapterView.OnItemClickListener{


    private int mCurIndex = -1;
    /** 标志位，标志已经初始化完成 */
    private boolean isPrepared;
    /** 是否已被加载过一次，第二次就不再去请求数据了 */
    private boolean mHasLoadedOnce;
    private boolean onResume=true;//onResume()方法初始化不执行


    private Context context;
    private static final String TAG = "QuerySamedarFragment";

    private TextView tvTotalMoney,tvTotalNum;//总金额，总笔数
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;


    //交易列表
    private List<OrderDetailData> lsOrder = new ArrayList<OrderDetailData>();
    //交易总条数
    private int orderListTotalCount = 0;
    //每次上拉获取的条数
    private int getMoerNum = 0;
    private QueryOrderListUpdateAdapter mAdapter;
    String isHistory = "";
    /** 分页 */
    private int pageNum = 1;//默认加载第一页
    private static final int pageNumCount = 20;//默认一页加载xx条数据（死值不变）
    private static final int REFRESH = 100;
    private static final int LOADMORE = 200;
    private static final int NOLOADMORE = 300;
    private boolean loadMore = false;//loadMore为true表示加载更多操作，false表示刷新操作
    // 当前窗口可见项总数
    private View loadMoreView;
    int visibleLastIndex = 0;    //最后的可视项索引
    int visibleItemCountNum;
    private int mPosition;
    private int lvChildTop;

    private UserBean userBean;
    private String payTypeStr = "",payStartTimeStr = "",payEndTimeStr = "",payStateStr = "";//
    private String queryMid = "",querySid = "",queryEid = "";

    String clientId = "";

    private SharedPreferencesUtil sharedPreferencesUtilClientId;

    @SuppressLint("ValidFragment")
    public QuerySamedarUpdateFragment(UserBean userBean, String queryMid, String querySid, String queryEid, String payTypeStr, String payStartTimeStr, String payEndTimeStr, String payStateStr) {
        this.userBean = userBean;
        this.queryMid = queryMid;
        this.querySid = querySid;
        this.queryEid = queryEid;
        this.payTypeStr = payTypeStr;
        this.payStartTimeStr = payStartTimeStr;
        this.payEndTimeStr = payEndTimeStr;
        this.payStateStr = payStateStr;
        Log.e(TAG,"构造方法执行-------------");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.query_sameday_fragment_update,null,false);
        context = view.getContext();
        Log.e(TAG,"onCreateView方法执行-------------");
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
        sharedPreferencesUtilClientId = new SharedPreferencesUtil(context,"ClientId");
        clientId = (String) sharedPreferencesUtilClientId.getSharedPreference("cid","");
        if(onResume){
            if(pageNum == 1){
                getOrderList(pageNum,pageNumCount);
            }else{
                pageNum = 1;
                getOrderList(pageNum,lsOrder.size());
            }
        }
    }

     /**  初始化数据 */
    private void initData(){

    }

    /** 初始化控件 */
    private void initView(View view){
        tvTotalMoney = view.findViewById(R.id.query_statis_bar_tvMoney);
        tvTotalNum = view.findViewById(R.id.query_statis_bar_tvNum);
        mSwipeRefreshLayout = view.findViewById(R.id.query_sameday_update_swipeRefreshLayout);
        mListView = view.findViewById(R.id.query_sameday_update_listView);
/** 获取上拉加载布局并初始化view  */
        loadMoreView = getLayoutInflater().inflate(R.layout.load_more, null);
        loadMoreView.setVisibility(View.GONE);
        mListView.addFooterView(loadMoreView);
        mListView.setFooterDividersEnabled(false);

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
//            	//刷新数据(刷新主要获取第一页的新数据)
                if(pageNum == 1){
                    getOrderList(pageNum,pageNumCount);
                }else{
                    pageNum = 1;
                    getOrderList(pageNum,lsOrder.size());
                }

            }
        });
        //给listview设置一个滑动的监听
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {


            //当滑动状态发生改变的时候执行
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    //当不滚动的时候
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:

                        mPosition = mListView.getFirstVisiblePosition();
                        View v = mListView.getChildAt(0);
                        lvChildTop = (v == null) ? 0 : v.getTop();

                        int itemsLastIndex = mAdapter.getCount() - 1;    //数据集最后一项的索引
                        int lastIndex = itemsLastIndex + 1;                //加上底部的loadMoreView项
                        //判断是否是最底部
                        //if (view.getLastVisiblePosition() == (view.getCount()) - 1) { //或者
                        if (visibleLastIndex == lastIndex) {
                            loadMoreView.setVisibility(View.VISIBLE);
                            //加载数据
                            loadMore = true;//记录该操作为刷新操作还是加载更多操作
                            if(lsOrder.size()<=orderListTotalCount&&getMoerNum==pageNumCount){
                                //已取出数据条数<=服务器端总条数&&上一次上拉取出的条数 == 规定的每页取出条数时代表还有数据库还有数据没取完
                                pageNum = pageNum + 1;
                                getOrderList(pageNum,pageNumCount);
                            }else{
                                //没有数据执行两秒关闭view
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        Message msg = new Message();
                                        msg.what = NOLOADMORE;
                                        msg.arg1 = visibleLastIndex - visibleItemCountNum + 1;
                                        handler.sendMessage(msg);

                                    }
                                }, 1000);

                            }
                        }
                        break;
                }
            }

            //正在滑动的时候执行
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                visibleItemCountNum = visibleItemCount;
                visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
            }
        });




    }

    /** 注册监听 */
    private void initListener(){
        mListView.setOnItemClickListener(this);
    }



    /**
     * 重写父类方法（fragment可见时加载界面数据）
     */
    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible|| mHasLoadedOnce) {
            return;
        }
        //请求数据
        getOrderList(pageNum,pageNumCount);
    }

    public void setPamase(String queryMid,String querySid,String queryEid,String payTypeStr,String payStartTimeStr,String payEndTimeStr,String payStateStr){
        this.queryMid = queryMid;
        this.querySid = querySid;
        this.queryEid = queryEid;
        this.payTypeStr = payTypeStr;
        this.payStartTimeStr = payStartTimeStr;
        this.payEndTimeStr = payEndTimeStr;
        this.payStateStr = payStateStr;

        pageNum = 1;
        getOrderList(pageNum,pageNumCount);
    }

    /**
     * 获取实时交易明细
     **/
    private void getOrderList(final int pageNum,final int pageCount){
        final String  url = NitConfig.queryOrderDayListUrl;
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
                    jsonStr.put("startTime", QueryDateTime.getStartTimeStamp(payStartTimeStr));
                    jsonStr.put("endTime",QueryDateTime.getEndTimeStamp(payEndTimeStr));
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

    /**
     * 查询Alias绑定状态
     */
    private void queryAliasStatus(){
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
                    int msg = 2;
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
                case REFRESH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
                case LOADMORE:
                       //数据集变化后,通知adapter
                    int position = msg.arg1;
                    mListView.setSelection(position);    //设置选中项
                    loadMoreView.setVisibility(View.GONE);
                    break;
                case NOLOADMORE:
                    int no_position = msg.arg1;
                    mListView.setSelection(no_position);    //设置选中项
                    loadMoreView.setVisibility(View.GONE);
                    ToastUtils.showText(getContext(),"没有更多了");
                    break;
                case 1:
                    String orderJsonStr=(String) msg.obj;
                    GsonOrderListReturnJson(orderJsonStr);
                    onResume = true;
                    break;
                case 2:
                    //{"data":"1000145","message":"查询成功","status":200}
                    String jsonStr_2 = (String) msg.obj;
                    queryAliasStatusJSON(jsonStr_2);
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
                Log.e("查询数据：", lsOrder.size()+""+"条");
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
                mAdapter = new QueryOrderListUpdateAdapter(context, lsOrder);
                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                //关闭上拉或下拉View，刷新Adapter
                if(pageNum > 1){
                    if(loadMore){
                        Message msg1 = new Message();
                        msg1.what = LOADMORE;
                        msg1.arg1 = visibleLastIndex - visibleItemCountNum + 1;
                        handler.sendMessage(msg1);
                    }else{
                        handler.sendEmptyMessageDelayed(REFRESH, 2000);
                    }

                }else{
                    if(!loadMore){
                        handler.sendEmptyMessageDelayed(REFRESH, 2000);
                    }
                }

                //此方法正常恢复到ListView滚动位置
                mListView.setSelectionFromTop(mPosition, lvChildTop);


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
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            handler.sendEmptyMessageDelayed(REFRESH, 0);
        }


        //查询别名绑定状态
        queryAliasStatus();
    }

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
                PushManager.getInstance().bindAlias(getContext(), userId);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "bindAlias = " + userId);
            }
            Log.e(TAG, "bindAlias = " + userId);
        }
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OrderDetailData order = lsOrder.get(position);
        Intent in = new Intent();
        in.setClass(getContext(), OrderDetailsActivity.class);
        in.putExtra("order",order);
        in.putExtra("isHistory",isHistory);
        startActivity(in);
    }
}
