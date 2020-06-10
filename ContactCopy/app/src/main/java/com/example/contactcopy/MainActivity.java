package com.example.contactcopy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
public class MainActivity extends Activity implements View.OnClickListener {
    public static String IpAddress=null;
    private EditText teleNumber;
    private EditText password;
    private Button login;
    private Button register;
    private Button connect;
    private String temp;
    private TextView forget;
    private EditText ip;
    int p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Init();
    }
    public void Init(){
        ip=(EditText)findViewById(R.id.ip);
        connect=(Button)findViewById(R.id.connect);
        forget=(TextView)findViewById(R.id.forget);
        teleNumber=(EditText)findViewById(R.id.teleNumber);
        password=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login);
        register=(Button)findViewById(R.id.register);
        connect.setOnClickListener(this);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        forget.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login:
                if(IpAddress!=null){
                    if(!teleNumber.getText().toString().isEmpty()){
                        if(!password.getText().toString().isEmpty()){
                            Thread newThread=new NewThread();
                            newThread.start();
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this,"服务器未连接",Toast.LENGTH_LONG).show();
                }


                break;
            case R.id.register:
                if(IpAddress!=null){
                    Intent intent_register=new Intent(MainActivity.this,RegisterActivity.class);
                    intent_register.putExtra("mark","Register");
                    startActivity(intent_register);
                }else{
                    Toast.makeText(MainActivity.this,"服务器未连接",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.forget:
                if(IpAddress!=null){
                    Intent intent_forget=new Intent(MainActivity.this,RegisterActivity.class);
                    intent_forget.putExtra("mark","forget");
                    startActivity(intent_forget);
                }else{
                    Toast.makeText(MainActivity.this,"服务器未连接",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.connect:
                if(!ip.getText().toString().isEmpty()){
                    IpAddress=ip.getText().toString();
                    Log.d("服务器地址：",IpAddress);
                }else {
                    Toast.makeText(MainActivity.this,"IP为空",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            temp=(String)bundle.get("result");
            if(temp.equals("true")){
                Intent intent=new Intent(MainActivity.this,FirstPageActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("id", teleNumber.getText().toString());
                bundle1.putString("password",password.getText().toString());
                intent.putExtras(bundle1);
                startActivity(intent);
            }else {
                Toast.makeText(MainActivity.this,"输入信息有误",Toast.LENGTH_LONG).show();
            }
        }
    };

    class NewThread extends Thread{
        @Override
        public void run() {
            try {
                String address = "http://"+IpAddress+":8080\\Contract\\/Servlet/UserCheck?id="+teleNumber.getText().toString()+"&password=" +
                        password.getText().toString();
                String method="GET";
                String jsonContent = getJsonContent(address,method);
                if(!jsonContent.isEmpty()){
                    JSONObject obj = JSONObject.parseObject(jsonContent);
                    Bundle bundle = new Bundle();
                    bundle.putString("result",(String) obj.get("result"));
                    Message msg=new Message();
                    msg.setData(bundle);
                    msg.what =0;
                    handler.sendMessage(msg);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public String getJsonContent(String path,String method){
        URL url;
        BufferedReader in;
        StringBuffer sb = new StringBuffer();
        try {
            url = new URL(path);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(30000);
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(method);
            // 设置User-Agent: Fiddler
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
            // 设置contentType
            conn.setRequestProperty("Content-Type", "application/json");
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String str;
            while ((str = in.readLine()) != null){
                sb.append(str);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
