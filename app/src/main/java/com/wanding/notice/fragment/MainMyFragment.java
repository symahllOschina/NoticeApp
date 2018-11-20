package com.wanding.notice.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.wanding.notice.R;
import com.wanding.notice.activity.BusInfoActivity;
import com.wanding.notice.activity.LoginActivity;
import com.wanding.notice.activity.MainActivity;
import com.wanding.notice.activity.SettingActivity;
import com.wanding.notice.application.BaseApplication;
import com.wanding.notice.base.BaseFragment;
import com.wanding.notice.utils.SharedPreferencesUtil;
import com.wanding.notice.utils.ToastUtils;

import de.hdodenhof.circleimageview.CircleImageView;

/** 我的Fragment */
public class MainMyFragment extends BaseFragment implements View.OnClickListener{


    private int mCurIndex = -1;
    /** 标志位，标志已经初始化完成 */
    private boolean isPrepared;
    /** 是否已被加载过一次，第二次就不再去请求数据了 */
    private boolean mHasLoadedOnce;
    private boolean onResume=true;//onResume()方法初始化不执行

    private Context context;
    private CircleImageView circleImageView;
    private TextView tvMyName;
    private RelativeLayout busInfoLayout,settingLayout,signOutLayout;//商户信息，设置,切换账号

    private SharedPreferencesUtil sharedPreferencesUtil;
    private SharedPreferencesUtil sharedPreferencesUtilSwitch;
    private com.wanding.notice.bean.UserBean userBean;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_my,null,false);
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

        tvMyName.setText(userBean.getName());

        if(userBean!=null){
            //private String role;//角色：("shop","商户"),("employee","员工"),("store","门店"),
            String roleStr = userBean.getRole();
            if(roleStr.equals("shop")){
                settingLayout.setVisibility(View.GONE);
            }else if(roleStr.equals("store")){
                settingLayout.setVisibility(View.GONE);
            }else if(roleStr.equals("employee")){
                settingLayout.setVisibility(View.VISIBLE);

            }
        }

    }

    /** 初始化控件 */
    private void initView(View view){
        context = getActivity();
        circleImageView = view.findViewById(R.id.main_fragment_my_circleImageView);
        tvMyName = view.findViewById(R.id.main_fragment_my_myName);
        busInfoLayout = view.findViewById(R.id.main_fragment_my_busInfoLayout);
        settingLayout = view.findViewById(R.id.main_fragment_my_settingLayout);
        signOutLayout = view.findViewById(R.id.main_fragment_my_signOutLayout);
    }

    /** 注册监听 */
    private void initListener(){
        busInfoLayout.setOnClickListener(this);
        settingLayout.setOnClickListener(this);
        signOutLayout.setOnClickListener(this);
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

    }

    /**
     *  显示确认提示框
     **/
    private void showConfirmDialog(){
        View view = LayoutInflater.from(context).inflate(R.layout.confirm_hint_dialog, null);
        TextView btok = (TextView) view.findViewById(R.id.confirm_hint_dialog_tvOk);
        TextView btCancel = (TextView) view.findViewById(R.id.confirm_hint_dialog_tvCancel);
        final Dialog myDialog = new Dialog(context,R.style.dialog);
        Window dialogWindow = myDialog.getWindow();
        WindowManager.LayoutParams params = myDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
        dialogWindow.setAttributes(params);
        myDialog.setContentView(view);
        btok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //清除保存的用户信息
                sharedPreferencesUtil = new SharedPreferencesUtil(context,"userInfo");
                sharedPreferencesUtil.clear();
                //更改语音播报设置
                sharedPreferencesUtilSwitch = new SharedPreferencesUtil(context,"voiceSwitch");
                boolean switchChecked = false;
                sharedPreferencesUtilSwitch.put("switchChecked",switchChecked);
//                //解绑别名
//                String alias = userBean.getAccount();
//                PushManager.getInstance().unBindAlias(getContext(), alias, false);

                Intent intent=new Intent();
                intent.setClass(getContext(), LoginActivity.class);
                startActivity(intent);
                //关闭应用
                BaseApplication.getInstance().exit();
                myDialog.dismiss();

            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                myDialog.dismiss();
            }
        });
        myDialog.show();
    }

    @Override
    public void onClick(View v) {
        Intent in = null;
        switch (v.getId()){
            case R.id.main_fragment_my_busInfoLayout:
                in = new Intent();
                in.setClass(context, BusInfoActivity.class);
                startActivity(in);
                break;
            case R.id.main_fragment_my_settingLayout:
                in = new Intent();
                in.setClass(context, SettingActivity.class);
                startActivity(in);
                break;
            case R.id.main_fragment_my_signOutLayout:
                showConfirmDialog();
                break;
        }
    }
}
