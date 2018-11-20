package com.wanding.notice.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wanding.notice.R;
import com.wanding.notice.adapter.SearchUserAdapter;
import com.wanding.notice.base.BaseActivity;
import com.wanding.notice.bean.SearchUserResult;
import com.wanding.notice.bean.UserBean;
import com.wanding.notice.httputils.HttpURLConnectionUtil;
import com.wanding.notice.httputils.NetworkUtils;
import com.wanding.notice.utils.GsonUtils;
import com.wanding.notice.utils.NitConfig;
import com.wanding.notice.utils.ToastUtils;
import com.wanding.notice.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 检索用户界面（检索门店，款台）
 *
 */
public class SearchUserActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private Context context;
    private static final String TAG = "SearchUserActivity";
    private ImageView imgBack;
    private EditText etSearch;
    private TextView tvSearch;
    private ListView mListView;

    private com.wanding.notice.bean.UserBean userBean;
    private int searchType;//1表示查门店，2表示查款台
    private String queryMid = "",querySid = "",queryEid = "",titleText;

    private List<SearchUserResult> lssur = new ArrayList<SearchUserResult>();
    private SearchUserAdapter mAdapter;

    public static final int RESULT_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_user_activity);
        context = SearchUserActivity.this;
        initView();
        initData();
        initListener();

    }

    /** 初始化数据  */
    private void initData(){
        Intent in = getIntent();
        userBean = (UserBean) in.getSerializableExtra("user");
        queryMid = in.getStringExtra("queryMid");
        querySid = in.getStringExtra("querySid");
        queryEid = in.getStringExtra("queryEid");
        titleText = in.getStringExtra("titleText");
        Log.e(TAG,"选择门店款台接收的值：MID="+queryMid+"，SID="+querySid+"EID="+queryEid+",titleText="+titleText);
        //默认数据
        if(Utils.isNotEmpty(getRole())){
            if(getRole().equals("shop")){
                lssur.clear();
                SearchUserResult obj = new SearchUserResult();
                obj.setValue("全部门店");
                lssur.add(0,obj);
            }else if(getRole().equals("store")){
                lssur.clear();
                SearchUserResult obj = new SearchUserResult();
                obj.setValue("全部款台");
                lssur.add(0,obj);
            }else{

            }
        }
        mAdapter = new SearchUserAdapter(context,lssur);
        mListView.setAdapter(mAdapter);

    }



    /**  初始化控件 */
    private void initView(){
        imgBack = findViewById(R.id.search_header_imgBack);
        etSearch = findViewById(R.id.search_header_etSearch);
        tvSearch = findViewById(R.id.search_header_tvSearch);
        mListView = findViewById(R.id.search_user_listView);
    }
    /**  注册监听 */
    private void initListener(){
        imgBack.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
    }

    /**
     * 查询门店，终端
     */
    private void SearchUserList(final String url,final String name){
        new Thread(){
            @Override
            public void run() {
                try {
                    // 拼装JSON数据，向服务端发起请求
                    JSONObject userJSON = new JSONObject();
                    if(searchType == 1){
                        //type==1：为商户时查门店
                        userJSON.put("mid",userBean.getMid());
                        userJSON.put("sname",name);
                    }else if(searchType == 2){
                        //type==2：为门店时查终端（款台）
                        userJSON.put("storeId",userBean.getSid());
                        userJSON.put("ename",name);
                    }


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
                    SearchUserResultJson(jsonStr_1);
                    break;
                case 201:
                    String errorJsonText = (String) msg.obj;
//                    ToastUtils.showText(context,errorJsonText);
                    break;
                case 400:
                    String errorServiceText = (String) msg.obj;
//                    ToastUtils.showText(context,errorServiceText);
                    break;
            }
        }
    };

    /** 处理返回JSON  */
    private void SearchUserResultJson(String json){
        try {
            JSONObject job = new JSONObject(json);
            String status = job.getString("status");
            String dataJson = job.getString("data");
            if(status!=null&&status.equals("200")){
                JSONObject arrayJob = new JSONObject(dataJson);
                String arrayJson = "";
                if(searchType == 1){
                    arrayJson = arrayJob.getString("storeList");
                    Gson gson = GsonUtils.getGson();
                    lssur.clear();
                    lssur=gson.fromJson(arrayJson, new TypeToken<List<SearchUserResult>>() {  }.getType());
                    if(lssur.size()<=0){
                        ToastUtils.showText(context,"未搜索到相关名称的门店");
                    }
                    SearchUserResult obj = new SearchUserResult();
                    obj.setValue("全部门店");
                    lssur.add(0,obj);
                }else if(searchType == 2){
                    arrayJson = arrayJob.getString("emplyeeList");
                    Gson gson = GsonUtils.getGson();
                    lssur.clear();
                    lssur=gson.fromJson(arrayJson, new TypeToken<List<SearchUserResult>>() {  }.getType());
                    if(lssur.size()<=0){
                        ToastUtils.showText(context,"未搜索到相关名称的款台");
                    }
                    SearchUserResult obj = new SearchUserResult();
                    obj.setValue("全部款台");
                    lssur.add(0,obj);
                }

                mAdapter = new SearchUserAdapter(context,lssur);
                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

            }else{
                sendMessage(NetworkUtils.SERVICE_CODE,NetworkUtils.SERVICE_TEXT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            sendMessage(NetworkUtils.JSON_IO_CODE,NetworkUtils.JSON_IO_TEXT);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.search_header_imgBack:

                Intent in = new Intent();
                in.putExtra("queryMid",queryMid);
                in.putExtra("querySid",querySid);
                in.putExtra("queryEid",queryEid);
                in.putExtra("titleText",titleText);
                setResult(RESULT_CODE, in);
                Log.e(TAG,"选择门店款台接收再返回的值：MID="+queryMid+"，SID="+querySid+",titleText="+titleText);
                finish();
                break;
            case R.id.search_header_tvSearch://搜索
                String etTextStr = etSearch.getText().toString().trim();
                if(Utils.isEmpty(etTextStr)){
                    ToastUtils.showText(context,"请输入搜索关键字！");
                    return;
                }
                if(Utils.isNotEmpty(getRole())){
                    if(getRole().equals("shop")){
                        searchType = 1;
                        String url = NitConfig.searchStoreUrl;
                        SearchUserList(url,etTextStr);
                    }else if(getRole().equals("store")){
                        searchType = 2;
                        String url = NitConfig.searchTerminalUrl;
                        SearchUserList(url,etTextStr);
                    }else{

                    }
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent in = new Intent();
                in.putExtra("queryMid",queryMid);
                in.putExtra("querySid",querySid);
                in.putExtra("queryEid",queryEid);
                in.putExtra("titleText",titleText);
                setResult(RESULT_CODE, in);

                Log.e(TAG,"选择门店款台接收再返回的值：MID="+queryMid+"，SID="+querySid+"EID="+queryEid+",titleText="+titleText);
                finish();

                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent in = new Intent();
        if(position==0){
            if(getRole().equals("shop")){
                titleText = "全部门店";
            }else if(getRole().equals("store")){
                titleText = "全部款台";
            }
            in.putExtra("queryMid",userBean.getMid());
            in.putExtra("querySid",userBean.getSid());
            in.putExtra("queryEid",userBean.getEid());
            in.putExtra("titleText",titleText);
            Log.e(TAG,"0选择门店款台接收再返回的值：MID="+userBean.getMid()+"，SID="+userBean.getSid()+"EID="+userBean.getEid()+",titleText="+titleText);
            setResult(RESULT_CODE, in);
            finish();
        }else{
            SearchUserResult seaResult = lssur.get(position);
            Log.e(TAG,"seaResult:"+seaResult.getId());
            titleText = seaResult.getValue();
            if(searchType == 1){
                in.putExtra("queryMid",userBean.getMid());
                in.putExtra("querySid",String.valueOf(seaResult.getId()));
                in.putExtra("queryEid",userBean.getEid());
                in.putExtra("titleText",titleText);
                Log.e(TAG,"1选择门店款台接收再返回的值：MID="+userBean.getMid()+"，SID="+String.valueOf(seaResult.getId())+"EID="+userBean.getEid()+",titleText="+titleText);
                setResult(RESULT_CODE, in);
                finish();

            }else if(searchType == 2){

                in.putExtra("queryMid",userBean.getMid());
                in.putExtra("querySid",userBean.getSid());
                in.putExtra("queryEid",String.valueOf(seaResult.getEid()));
                in.putExtra("titleText",titleText);
                Log.e(TAG,"2选择门店款台接收再返回的值：MID="+userBean.getMid()+"，SID="+userBean.getSid()+"EID="+String.valueOf(seaResult.getEid())+",titleText="+titleText);
                setResult(RESULT_CODE, in);
                finish();
            }
        }
    }
}
