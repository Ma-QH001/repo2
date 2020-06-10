package com.example.contactcopy.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.contactcopy.Entity.person;
import com.example.contactcopy.R;

import java.util.List;

public class listViewAdapter extends BaseAdapter {
    private List<person> data;
    private LayoutInflater layoutInflater;
    private Context context;
    public listViewAdapter(Context context,List<person> data){
        this.context=context;
        this.data=data;
        this.layoutInflater=LayoutInflater.from(context);
    }
    final class zujian{
        public TextView tv_contact_name;
        public TextView tv_contact_phones;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        zujian z=null;
        if(convertView==null){
            z=new zujian();
            convertView=layoutInflater.inflate(R.layout.item_list,null);
            z.tv_contact_name=(TextView) convertView.findViewById(R.id.tv_contact_name);
            z.tv_contact_phones=(TextView) convertView.findViewById(R.id.tv_contact_phones);
            convertView.setTag(z);
        }else{
            z=(zujian)convertView.getTag();
        }
        //绑定数据
        z.tv_contact_name.setText(data.get(position).getContract());
        z.tv_contact_phones.setText(data.get(position).getNumber());
        return convertView;
    }
}
