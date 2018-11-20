package com.wanding.notice.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wanding.notice.R;
import com.wanding.notice.activity.ScreetActivity;
import com.wanding.notice.activity.SearchUserActivity;
import com.wanding.notice.base.BaseFragment;
import com.wanding.notice.query.util.QueryUtil;
import com.wanding.notice.view.ControlScrollViewPager;

/** 首页Fragment */
public class MainHomeFragment extends BaseFragment implements View.OnClickListener,ViewPager.OnPageChangeListener{

    private Context context;
    private static final String TAG = "MainHomeFragment";
    private LinearLayout screenLayout,titleLayout;//筛选，标题layout
    private TextView tvTitle;//标题（根据登录角色显示）
    private ImageView imgTitle;
    private ControlScrollViewPager mViewPager;
    private RelativeLayout samedayLayout, historyLayout;//实时交易，历史交易
    private TextView samedayText, historyText;
    private View samedayView,historyView;

    /** mViewPager适配器 */
    private ViewPagerAdapter mAdapter;
    /** fragment 首页，设置*/
//    private QuerySamedarFragment samedayFragment;
//    private QueryHistoryFragment historyFragment;
    private QuerySamedarUpdateFragment samedayFragment;
    private QueryHistoryUpdateFragment historyFragment;

    private final static int REQUEST_CODE = 1;
    private final static int REQUEST_CODE2 = 2;
    private com.wanding.notice.bean.UserBean userBean;
    public String payTypeStr = "",payStartTimeStr = "",payEndTimeStr = "",payStateStr = QueryUtil.payStateArray[0];//
    public String queryMid = "",querySid = "",queryEid = "";
    private String titleText;
    private int payTypeIndex,payStateIndex;
    private int fragmentType = 1;//区分当前显示界面（默认为1，即实时交易，2时为历史交易）


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_home,null,false);
        initView(view);
        initData();
        initListener();
        initFragment();
        return view;
    }

    @Override
    protected void lazyLoad() {

    }

    /**  初始化数据 */
    private void initData(){
        //取出对象
        try {
            userBean=(com.wanding.notice.bean.UserBean) com.wanding.notice.utils.MySerialize.deSerialization(com.wanding.notice.utils.MySerialize.getObject("user", context));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        queryMid = userBean.getMid();
        querySid = userBean.getSid();
        queryEid = userBean.getEid();

        //根据登录角色更新标题栏
        updateTopBar();

    }

    /** 根据登录角色更新标题栏 */
    private void updateTopBar(){
        if(userBean!=null){
            //private String role;//角色：("shop","商户"),("employee","员工"),("store","门店"),
            String roleStr = userBean.getRole();
            if(roleStr.equals("shop")){
                titleText = "全部门店";
                tvTitle.setText(titleText);
            }else if(roleStr.equals("store")){
                titleText = "全部款台";
                tvTitle.setText(titleText);
            }else if(roleStr.equals("employee")){
                titleText = userBean.getName();
                tvTitle.setText(titleText);
                imgTitle.setVisibility(View.INVISIBLE);
            }
        }
    }


    /**  初始化控件  */
    private void initView(View view){
        context = getActivity();
        screenLayout = view.findViewById(R.id.main_header_layoutScreen);
        titleLayout = view.findViewById(R.id.main_header_layoutTitle);
        tvTitle = view.findViewById(R.id.main_header_title);
        imgTitle = view.findViewById(R.id.main_header_titleImg);
        mViewPager = view.findViewById(R.id.main_fragment_home_mViewPager);
        samedayLayout = view.findViewById(R.id.query_statis_tab_samedarLayout);
        historyLayout = view.findViewById(R.id.query_statis_tab_historyLayout);
        samedayText = view.findViewById(R.id.query_statis_tab_tvSamedar);
        historyText = view.findViewById(R.id.query_statis_tab_tvHistory);
        samedayView = view.findViewById(R.id.query_statis_tab_viewSamedar);
        historyView = view.findViewById(R.id.query_statis_tab_viewHistory);
    }

    /** 注册监听 */
    private void initListener(){
        screenLayout.setOnClickListener(this);
        titleLayout.setOnClickListener(this);
        //注册Viewpager滑动监听事件
        mViewPager.addOnPageChangeListener(this);
        //注册tab点击时间
        samedayLayout.setOnClickListener(this);
        historyLayout.setOnClickListener(this);
    }

    /** 初始化Fragment  */
    private void initFragment(){
    //初始化fragment
        samedayFragment = new QuerySamedarUpdateFragment(userBean,queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);
        historyFragment = new QueryHistoryUpdateFragment(userBean,queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);
        //初始化Adapter
        //Fragment中的嵌套Fragment不能再用getActivity().getSupportFragmentManager();要用getChildFragmentManager();
        mAdapter=new ViewPagerAdapter(getChildFragmentManager());
        //预加载界面数(ViewPager预加载默认数是1个，既设置0也没效果，他会默认把相邻界面数据预加载)
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(mAdapter);
        //初始化默认加载界面
        mViewPager.setCurrentItem(0);
        samedayText.setTextColor(getResources().getColor(R.color.blue_409EFF));
        samedayView.setBackgroundColor(getResources().getColor(R.color.blue_409EFF));
    }


    /**
     * 初始化所有tab
     */
    private void resetImg(){
        samedayText.setTextColor(getResources().getColor(R.color.black_333));
        samedayView.setBackgroundColor(getResources().getColor(R.color.white_ffffff));
        historyText.setTextColor(getResources().getColor(R.color.black_333));
        historyView.setBackgroundColor(getResources().getColor(R.color.white_ffffff));
    }


    /**
     * 定义viewPager左右滑动的适配器
     */
    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                return samedayFragment;
            }else{
                return historyFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            super.destroyItem(container, position, object);
        }

        @Override
        public void destroyItem(@NonNull View container, int position, @NonNull Object object) {
//            super.destroyItem(container, position, object);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        //先初始化所有Tab
        resetImg();
        if(position == 0){
            fragmentType = 1;
            samedayText.setTextColor(getResources().getColor(R.color.blue_409EFF));
            samedayView.setBackgroundColor(getResources().getColor(R.color.blue_409EFF));
            mViewPager.setCurrentItem(0);
        }else{
            fragmentType = 2;
            historyText.setTextColor(getResources().getColor(R.color.blue_409EFF));
            historyView.setBackgroundColor(getResources().getColor(R.color.blue_409EFF));
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == REQUEST_CODE){
            if(resultCode == ScreetActivity.RESULT_CODE){
                Bundle bundle = data.getExtras();
                payTypeStr = bundle.getString("payType");
                payStartTimeStr = bundle.getString("payStartTime");
                payEndTimeStr = bundle.getString("payEndTime");
                payStateStr = bundle.getString("payState");
                payTypeIndex = bundle.getInt("payTypeIndex");
                payStateIndex = bundle.getInt("payStateIndex");
                fragmentType = bundle.getInt("fragmentType");
                Log.e(TAG,"交易类型值："+payTypeStr+"交易时间"+payStartTimeStr+"交易结束时间"+payEndTimeStr+"交易状态"+payStateStr);
//                if(fragmentType == 1){
//                    samedayFragment.setPamase(queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);
//                }else if(fragmentType == 2){
//                    historyFragment.setPamase(queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);
//                }

                samedayFragment.setPamase(queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);
                historyFragment.setPamase(queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);

            }else if(resultCode == SearchUserActivity.RESULT_CODE){
                Bundle bundle = data.getExtras();
                queryMid = bundle.getString("queryMid");
                querySid = bundle.getString("querySid");
                queryEid = bundle.getString("queryEid");
                titleText = bundle.getString("titleText");
                tvTitle.setText(titleText);
//                if(fragmentType == 1){
//                    samedayFragment.setPamase(queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);
//                }else if(fragmentType == 2){
//                    historyFragment.setPamase(queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);
//                }
                samedayFragment.setPamase(queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);
                historyFragment.setPamase(queryMid,querySid,queryEid,payTypeStr,payStartTimeStr,payEndTimeStr,payStateStr);


                Log.e(TAG,"选择门店款台返回的值：MID="+queryMid+"，SID="+querySid+"EID="+queryEid+",titleText="+titleText);
            }
        }


    }

    @Override
    public void onClick(View v) {
        Intent in = null;
        switch (v.getId()){
            case R.id.main_header_layoutTitle://选择门店、选择终端
                if(userBean!=null){
                    //private String role;//角色：("shop","商户"),("employee","员工"),("store","门店"),
                    String roleStr = userBean.getRole();
                    if(!roleStr.equals("employee")){
                        in = new Intent();
                        in.setClass(context, SearchUserActivity.class);
                        in.putExtra("user",userBean);
                        in.putExtra("queryMid",queryMid);
                        in.putExtra("querySid",querySid);
                        in.putExtra("queryEid",queryEid);
                        in.putExtra("titleText",titleText);
                        Log.e(TAG,"选择门店款台传递的值：MID="+queryMid+"，SID="+querySid+"EID="+queryEid+",titleText="+titleText);
                        startActivityForResult(in,REQUEST_CODE);
                    }
                }
                break;
            case R.id.main_header_layoutScreen://筛选
                in = new Intent();
                in.setClass(context, ScreetActivity.class);
                in.putExtra("payType",payTypeStr);
                in.putExtra("payStartTime",payStartTimeStr);
                in.putExtra("payEndTime",payEndTimeStr);
                in.putExtra("payState",payStateStr);
                in.putExtra("payTypeIndex",payTypeIndex);
                in.putExtra("payStateIndex",payStateIndex);
                in.putExtra("fragmentType",fragmentType);
                startActivityForResult(in,REQUEST_CODE);
                Log.e(TAG,"筛选条件传递值：" +
                        "交易类型=" + payTypeStr+
                        ",起始时间="+payStartTimeStr+
                        ",结束时间="+payEndTimeStr+
                        ",交易状态="+payStateStr+
                        ",payTypeIndex="+payTypeIndex+
                        ",payStateIndex="+payStateIndex+
                ",fragmentType="+fragmentType);
                break;
            case R.id.query_statis_tab_samedarLayout://实时交易
                fragmentType = 1;
                //先初始化所有Tab
                resetImg();
                samedayText.setTextColor(getResources().getColor(R.color.blue_409EFF));
                samedayView.setBackgroundColor(getResources().getColor(R.color.blue_409EFF));
                mViewPager.setCurrentItem(0);
                break;
            case R.id.query_statis_tab_historyLayout://历史交易
                fragmentType = 2;
                //先初始化所有Tab
                resetImg();
                historyText.setTextColor(getResources().getColor(R.color.blue_409EFF));
                historyView.setBackgroundColor(getResources().getColor(R.color.blue_409EFF));
                mViewPager.setCurrentItem(1);
                break;
        }
    }
}
