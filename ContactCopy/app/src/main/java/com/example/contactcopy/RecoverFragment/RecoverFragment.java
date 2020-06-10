package com.example.contactcopy.RecoverFragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.contactcopy.Adapter.listViewAdapter;
import com.example.contactcopy.Entity.person;
import com.example.contactcopy.FirstPageActivity;
import com.example.contactcopy.MainActivity;
import com.example.contactcopy.R;
import com.example.contactcopy.Util.ExcelUtil;
import com.example.contactcopy.base.BaseFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RecoverFragment extends BaseFragment implements View.OnClickListener {
    private Button getData;
    private Button OutContract;
    private ListView listView_get;
    private TextView getQty;
    private ImageView downToExcel;
    private static List<person> Data = new ArrayList<>();
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    //接收id值
    String id;
    Dialog dialog;
    @Override
    public View initView() {
        View view = View.inflate(mContext, R.layout.fragment_recover, null);
        getData = (Button) view.findViewById(R.id.getData);
        downToExcel=(ImageView)view.findViewById(R.id.downToExcel);
        downToExcel.setOnClickListener(this);
        getQty=(TextView) view.findViewById(R.id.getQty);
        OutContract = (Button) view.findViewById(R.id.OutContract);
        listView_get = (ListView) view.findViewById(R.id.listview_get);
        getData.setOnClickListener(this);
        OutContract.setOnClickListener(this);
        return view;
    }
    public void showLoading(){
        dialog=new Dialog(getContext(),R.style.dialog);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 申请读写权限
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getData:
                showLoading();
                Thread newThread = new RecoverFragment.NewThread();
                newThread.start();
                break;
            case R.id.OutContract:
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.WRITE_CONTACTS},1);
                }else {
                    showLoading();
                    for (person p : Data) {
                        addContact(getContext(),p.getContract(), p.getNumber());
                    }
                    dialog.dismiss();
                    Toast.makeText(getContext(),"还原成功，请前去系统通讯录查看",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.downToExcel:
                verifyStoragePermissions(getActivity());
                if(Data.isEmpty()){
                    Toast.makeText(getContext(),"数据为空，请先获取数据！",Toast.LENGTH_LONG).show();
                }else{
                    OutExcel();
                }
                break;
        }
    }
    /**
     * 导出为Excel文档
     */
    public void OutExcel(){
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String excelFileName = "/备份精灵.xls";
        String[] title = {"姓名","号码"};
        String sheetName = "demoSheetName";
        filePath = filePath+excelFileName;
        ExcelUtil.initExcel(filePath, title);
        ExcelUtil.writeObjListToExcel(Data, filePath, getContext());
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    for (person p : Data) {
                        addContact(getContext(),p.getContract(), p.getNumber());
                    }
                }else {
                    Toast.makeText(getContext(),"该权限必须打开",Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }

    /**
     * 接收从Activity传来的值
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        id = ((FirstPageActivity) activity).getTitles();
    }

    public String parseJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        return jsonObject.toString();
    }

    class NewThread extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            final String urlPath = "http://"+ MainActivity.IpAddress+":8080/Contract/Servlet/QueryPerson";
            URL url;
            try {
                StringBuffer sb = new StringBuffer();
                url = new URL(urlPath);
                String content = parseJson();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);//设置允许输出
                conn.setRequestMethod("POST");
                conn.setRequestProperty("User-Agent", "Fiddler");
                conn.setRequestProperty("Content-Type", "application/json");
                OutputStream os = conn.getOutputStream();
                os.write(content.getBytes());
                os.close();
                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String responseData;
                    while ((responseData = in.readLine()) != null) {
                        sb.append(responseData);
                    }
                    if (!sb.toString().isEmpty()) {
                        //解析json
                        //Log.d("KKKKKK",sb.toString());
                        JSONObject jsonObject = JSONObject.parseObject(sb.toString());
                        JSONArray jsonArray = (JSONArray) jsonObject.get("data");
                        List<person> Datas = new ArrayList<>();
                        if (jsonArray.size() > 0) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject job = jsonArray.getJSONObject(i);
                                String data1 = job.getString("contract");
                                String data2 = job.getString("tele");
                                person p = new person(data1,data2);
                                Datas.add(p);
                            }
                        }
                        Bundle bundle=new Bundle();
                        bundle.putParcelableArrayList("Datas", (ArrayList<? extends Parcelable>) Datas);
                        Message msg=new Message();
                        msg.setData(bundle);
                        handlers.sendMessage(msg);
                    }else {
                        Toast.makeText(getContext(), "数据为空", Toast.LENGTH_SHORT).show();
                    }
                    in.close();
                }
            } catch (Exception e) {
                // TODO: handle exception
                dialog.dismiss();
                Toast.makeText(getContext(),"数据为空",Toast.LENGTH_LONG).show();
            }
            Looper.loop();
        }
    }

    public Handler handlers = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            Data=bundle.getParcelableArrayList("Datas");
            listView_get.setAdapter(new listViewAdapter(getActivity(), Data));
            getQty.setText("联系人信息（"+Data.size()+"条）");
            dialog.dismiss();
        }
    };


    /**
     * 向系统通讯录插入数据
     *
     * @param name
     * @param phoneNumber
     */
    public void addContact(Context context,String name, String phoneNumber) {
        ContentValues values = new ContentValues();

        // 向RawContacts.CONTENT_URI空值插入，
        // 先获取Android系统返回的rawContactId
        // 后面要基于此id插入值
        Uri rawContactUri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        // 内容类型
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        // 联系人名字
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        // 向联系人URI添加联系人名字
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // 联系人的电话号码
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
        // 电话类型
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        // 向联系人电话号码URI添加电话号码
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();
    }
}
