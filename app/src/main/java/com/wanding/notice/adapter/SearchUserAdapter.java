package com.wanding.notice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wanding.notice.R;
import com.wanding.notice.bean.SearchUserResult;

import java.util.List;

/**
 * 查询门店，款台ListView Adapter
 */
public class SearchUserAdapter extends BaseAdapter{

    private Context context;
    private List<SearchUserResult> lssur;
    private LayoutInflater inflater;

    public SearchUserAdapter(Context context,List<SearchUserResult> lssur) {
        this.context = context;
        this.lssur = lssur;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lssur.size();
    }

    @Override
    public Object getItem(int position) {
        return lssur.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        public TextView tvName;
    }

    @Override
    public View getView(int position, View subView, ViewGroup parent) {
        SearchUserResult obj = lssur.get(position);
        ViewHolder vh = null;
        if(subView == null){
            subView = inflater.inflate(R.layout.search_user_activity_item,null);
            vh = new ViewHolder();
            vh.tvName = subView.findViewById(R.id.search_user_item_tvName);
            subView.setTag(vh);
        }else{
            vh = (ViewHolder) subView.getTag();
        }
        String valueStr = obj.getValue();
        vh.tvName.setText(valueStr);
        return subView;
    }
}
