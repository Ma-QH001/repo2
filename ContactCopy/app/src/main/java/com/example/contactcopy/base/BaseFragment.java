package com.example.contactcopy.base;



import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;



/*
*
* 备份：CopyFragment
* 恢复:RecoverFragment
* */

public abstract class BaseFragment extends Fragment {
    protected Context mContext;

    /**
     * 被系统创建时回调
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=getActivity();
    }


    /*
    *
    * 先被执行
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initView();
    }
    /*
    *
    * 抽象类 由孩子实现，不同效果
    *
     */

    public abstract View initView();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }
    public void initData(){

    }
}
