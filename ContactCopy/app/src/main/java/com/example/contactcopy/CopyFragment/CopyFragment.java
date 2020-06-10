package com.example.contactcopy.CopyFragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.contactcopy.Adapter.listViewAdapter;
import com.example.contactcopy.Entity.person;
import com.example.contactcopy.FirstPageActivity;
import com.example.contactcopy.MainActivity;
import com.example.contactcopy.R;
import com.example.contactcopy.base.BaseFragment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CopyFragment extends BaseFragment implements View.OnClickListener {
    private Button getContract;
    private Button copy;
    private TextView qty;
    private ListView listView;
    List<person> contractlist=new ArrayList<>();
    ArrayAdapter<person> adapter;
    String id;
    private String temp;
    Dialog dialog;
    @Override
    public View initView() {
        View view=View.inflate(mContext, R.layout.fragment_copy,null);
        getContract=(Button)view.findViewById(R.id.getContract);
        copy=(Button)view.findViewById(R.id.copy);
        listView=(ListView)view.findViewById(R.id.listview);
        qty=(TextView)view.findViewById(R.id.qty);
        copy.setOnClickListener(this);
        getContract.setOnClickListener(this);
        return view;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        id= ((FirstPageActivity) activity).getTitles();
    }

    @Override
    public void initData(){
        super.initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.getContract:
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.READ_CONTACTS},1);
                }else {
                    List<person> list=readContacts();
                    listView.setAdapter(new listViewAdapter(getActivity(), list));
                    qty.setText("共读取"+list.size()+"条信息");
                }
                break;
            case R.id.copy:
                if(contractlist.isEmpty()){
                    Toast.makeText(getContext(),"请先导入联系人",Toast.LENGTH_SHORT).show();
                }else {
                    dialog=new Dialog(getContext(),R.style.dialog);
                    dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.loading);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    Thread newThread=new CopyFragment.NewThread();
                    newThread.start();
                }
                break;
        }
    }


    public String parseJson(List<person> list){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("data",JSON.toJSON(list));
        return jsonObject.toString();
    }


    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            temp=(String)bundle.get("result");
            if(temp.equals("true")){
                dialog.dismiss();
                Toast.makeText(getContext(),"备份成功",Toast.LENGTH_LONG).show();
            }else if(temp.equals("NULL")){
                Toast.makeText(getContext(),"没有备份数据",Toast.LENGTH_LONG).show();
            }
        }
    };


    public List<person> readContacts(){
        Cursor cursor=null;
        try{
            cursor=getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
            if(cursor!=null){
                while(cursor.moveToNext()){
                    //联系人姓名
                    String contract=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    person p=new person(contract,number);
                    contractlist.add(p);
                }
            }
            return contractlist;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    List<person> list=readContacts();
                    listView.setAdapter(new listViewAdapter(getActivity(), list));
                }else {
                    Toast.makeText(getContext(),"该权限必须打开",Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }
    class NewThread extends Thread{
        @Override
        public void run() {
            Looper.prepare();
            final String urlPath="http://"+ MainActivity.IpAddress+":8080/Contract/Servlet/InsertPerson";
            URL url;
            try
            {
                StringBuffer sb = new StringBuffer();
                url = new URL(urlPath);
                String content=parseJson(readContacts());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);//设置允许输出
                conn.setRequestMethod("POST");
                conn.setRequestProperty("User-Agent", "Fiddler");
                conn.setRequestProperty("Content-Type", "application/json");
                OutputStream os = conn.getOutputStream();
                os.write(content.getBytes());
                os.close();
                /*服务器返回的响应码*/
                int code = conn.getResponseCode();
                if(code == 200)
                {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String responseData;
                    while((responseData = in.readLine()) != null)
                    {
                        sb.append(responseData);
                    }
                    if(!sb.toString().isEmpty()){
                        //解析json
                        JSONObject jsonObject = JSONObject.parseObject(sb.toString());
                        Bundle bundle = new Bundle();
                        bundle.putString("result",(String) jsonObject.get("result"));
                        Message msg=new Message();
                        msg.setData(bundle);
                        msg.what =0;
                        handler.sendMessage(msg);
                    }
                    in.close();
                }
                else
                {
                    Toast.makeText(getContext(),"数据提交失败", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e)
            {
                // TODO: handle exception
                throw new RuntimeException(e);
            }
            Looper.loop();
        }

    }


}
