package com.example.contactcopy.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.contactcopy.R;

import java.util.ArrayList;
import java.util.List;

public class ContentAdapter extends BaseAdapter {
    private List<String> list=new ArrayList<String>();
    private LayoutInflater layoutInflater;
    private Context context;

    public ContentAdapter(Context context, List<String> list) {
        this.context=context;
        this.list = list;
        this.layoutInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Itemview z=new Itemview();
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.item,null);
            z.name=(TextView) convertView.findViewById(R.id.name);
            convertView.setTag(z);
        }else{
            z=(Itemview) convertView.getTag();
        }
        //绑定数据
        z.name.setText(list.get(position));
        return convertView;
    }
    final class Itemview{
        public TextView name;
    }
}
