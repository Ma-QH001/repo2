package com.example.contactcopy;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.mob.MobSDK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


public class RegisterActivity extends Activity implements View.OnClickListener{

    String APPKEY = "2d63114ed33bb";
    String APPSECRETE = "4128b09dc8d3e9b26249b5456db5f464";
    // 手机号输入框
    private EditText inputPhoneEt;

    // 验证码输入框
    private EditText inputCodeEt;

    // 获取验证码按钮
    private Button requestCodeBtn;

    // 注册按钮
    private Button commitBtn;
    //密码输入
    private EditText password;
    private String temp;

    //倒计时
    int i = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();		//初始化控件
    }

    private void init() {
        inputPhoneEt = (EditText) findViewById(R.id.login_input_phone_et);
        inputCodeEt = (EditText) findViewById(R.id.login_input_code_et);
        requestCodeBtn = (Button) findViewById(R.id.login_request_code_btn);
        password=(EditText)findViewById(R.id.password);
        commitBtn = (Button) findViewById(R.id.login_commit_btn);
        requestCodeBtn.setOnClickListener(this);
        if(getIntent().getStringExtra("mark").equals("forget")){
            password.setFocusableInTouchMode(false);
            password.setKeyListener(null);
            password.setClickable(false);
            password.setFocusable(false);
            commitBtn.setText("找回密码");
        }else if(getIntent().getStringExtra("mark").equals("Register")){
            password.setFocusableInTouchMode(true);
            commitBtn.setText("注册");
        }else if(getIntent().getStringExtra("mark").equals("reset")){
            password.setFocusableInTouchMode(true);
            commitBtn.setText("修改密码");
        }
        commitBtn.setOnClickListener(this);

        // 启动短信验证sdk
        MobSDK.init(this, APPKEY, APPSECRETE);
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        //注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);
    }


    @Override
    public void onClick(View v) {
        String phoneNums = inputPhoneEt.getText().toString();
        switch (v.getId()) {
            case R.id.login_request_code_btn:
                // 1. 通过规则判断手机号
                if (!judgePhoneNums(phoneNums)) {
                    return;
                } // 2. 通过sdk发送短信验证
                SMSSDK.getVerificationCode("86", phoneNums);

                // 3. 把按钮变成不可点击，并且显示倒计时（正在获取）
                requestCodeBtn.setClickable(false);
                requestCodeBtn.setText("重新发送(" + i + ")");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; i > 0; i--) {
                            handler.sendEmptyMessage(-9);
                            if (i <= 0) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(-8);
                    }
                }).start();
                break;

            case R.id.login_commit_btn:
                //将收到的验证码和手机号提交再次核对
                SMSSDK.submitVerificationCode("86", phoneNums, inputCodeEt
                        .getText().toString());
                //Thread newThread=new RegisterActivity.NewThread();
                //newThread.start();
                break;
        }
    }

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == -9) {
                requestCodeBtn.setText("重新发送(" + i + ")");
            } else if (msg.what == -8) {
                requestCodeBtn.setText("获取验证码");
                requestCodeBtn.setClickable(true);
                i = 30;
            } else {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                Log.e("event", "event=" + event);
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 短信注册成功后，返回MainActivity,然后提示
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功
                        Toast.makeText(getApplicationContext(), "提交验证码成功",
                                Toast.LENGTH_SHORT).show();
                        Thread newThread=new RegisterActivity.NewThread();
                        newThread.start();
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Toast.makeText(getApplicationContext(), "正在获取验证码",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        ((Throwable) data).printStackTrace();
                    }
                }
            }
        }
    };
    public Handler handler_result=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            temp=(String)bundle.get("result");
            if(commitBtn.getText().toString().equals("找回密码")){
                showDialog(temp);
            }else{
                if(temp.equals("true")){
                    Toast.makeText(RegisterActivity.this,commitBtn.getText().toString()+"成功",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(intent);
                } else{
                    Toast.makeText(RegisterActivity.this,"请求失败,请重试...",Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private boolean judgePhoneNums(String phoneNums) {
        if (isMatchLength(phoneNums, 11)
                && isMobileNO(phoneNums)) {
            return true;
        }
        Toast.makeText(this, "手机号码输入有误！", Toast.LENGTH_SHORT).show();
        return false;
    }

    public static boolean isMatchLength(String str, int length) {
        if (str.isEmpty()) {
            return false;
        } else {
            return str.length() == length ? true : false;
        }
    }

    /**
     * 筛选非法号码
     * @param mobileNums
     * @return
     */

    public static boolean isMobileNO(String mobileNums) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        String telRegex = "[1][3456789]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobileNums))
            return false;
        else
            return mobileNums.matches(telRegex);
    }


    @Override
    protected void onDestroy() {
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }

    public String MakePath(){
        String address=null;
        if(!commitBtn.getText().toString().equals("找回密码")){
            address="http://"+MainActivity.IpAddress+":8080/Contract/Servlet/InsertUser?id="+inputPhoneEt.getText().toString()+"&password=" +
                    password.getText().toString();
        }else{
            address="http://"+MainActivity.IpAddress+":8080/Contract/Servlet/FindPasswordServlet?id="+inputPhoneEt.getText().toString();
        }
        return address;
    }

    /**
     * 访问服务器添加用户
     */
    class NewThread extends Thread{
        @Override
        public void run() {
            try {
                String address = MakePath();
                String method="GET";
                String jsonContent = getJsonContent(address,method);
                if(!jsonContent.isEmpty()){
                    JSONObject obj = JSONObject.parseObject(jsonContent);
                    Bundle bundle = new Bundle();
                    bundle.putString("result",(String) obj.get("result"));
                    Message msg=new Message();
                    msg.setData(bundle);
                    msg.what =0;
                    handler_result.sendMessage(msg);
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
            in = new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
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
    private void showDialog(String password){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("密码提示");
        builder.setMessage("您的密码为："+password);
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog dialog=builder.create();
        dialog.show();

    }


}
