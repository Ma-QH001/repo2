package com.example.contactcopy;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.contactcopy.Adapter.ContentAdapter;
import com.example.contactcopy.CopyFragment.CopyFragment;
import com.example.contactcopy.Entity.User;
import com.example.contactcopy.RecoverFragment.RecoverFragment;
import com.example.contactcopy.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;


public class FirstPageActivity extends FragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private FrameLayout frameLayout;
    private RadioButton rb_copy;
    private RadioButton rb_recover;
    private RadioGroup rgMain;
    private ArrayList<BaseFragment> fragment;
    private int position=0;
    private ImageView user;
    private List<String> list = new ArrayList<String>();
    DrawerLayout drawerLayout;
    ListView listView;
    /**
     * Fragment缓存
     */
    private Fragment tempFragment;
    String id;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        initView();
        initfragment();
        initListener();
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        id=bundle.getString("id");
        password=bundle.getString("password");
        Log.d("password",password);

    }
    public String getTitles(){
        return id;
    }
    public void initView(){
        frameLayout=(FrameLayout)findViewById(R.id.frameLayout);
        rgMain=(RadioGroup)findViewById(R.id.rg_main);
        rb_copy=(RadioButton)findViewById(R.id.rb_copy);
        rb_recover=(RadioButton)findViewById(R.id.rb_recover);
        user=(ImageView)findViewById(R.id.user);
        user.setOnClickListener(this);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        listView = findViewById(R.id.left_listview);
        initData();
        ContentAdapter adapter = new ContentAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }




    private void initListener() {
        rgMain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_copy:
                        position=0;
                        change(position);
                        break;
                    case R.id.rb_recover:
                        position=1;
                        change(position);
                        break;
                    default:
                        position=0;
                        change(position);
                        break;
                }
                /*BaseFragment baseFragment=getFragment(position);

                switchFragment(tempFragment,baseFragment);*/
            }
        });

        rgMain.check(R.id.rb_copy);
    }
    public void initData() {
        list.add("我的信息");
        list.add("退出App");
    }

    /**
     * 切换页面
     * @param local
     */
    public void change(int local){
        if(local==0){
            CopyFragment fragment=new CopyFragment();
            Bundle bundle = new Bundle();
            bundle.putString("id",id);
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout,fragment);
            fragmentTransaction.commit();
        }else{
            RecoverFragment fragment=new RecoverFragment();
            Bundle bundle = new Bundle();
            bundle.putString("id",id);
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout,fragment);
            fragmentTransaction.commit();
        }
    }

    private void initfragment(){
        fragment=new ArrayList<>();
        fragment.add(new CopyFragment());
        fragment.add(new RecoverFragment());
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user:
                drawerLayout.openDrawer(Gravity.LEFT);/*重点，LEFT是xml布局文件中侧边栏布局所设置的方向*/
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                showDialog();
                break;
            case 1:
                exit(0);
                break;
            default:
                break;
        }
        drawerLayout.closeDrawer(Gravity.LEFT);/*重点，自动关闭侧边栏*/
    }
    private void showDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("个人信息");
        builder.setItems(new String[]{"绑定手机："+id,"登录密码："+password},new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton("修改密码",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent=new Intent(FirstPageActivity.this,RegisterActivity.class);
                        intent.putExtra("mark","reset");
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog dialog=builder.create();
        dialog.show();

    }

}
