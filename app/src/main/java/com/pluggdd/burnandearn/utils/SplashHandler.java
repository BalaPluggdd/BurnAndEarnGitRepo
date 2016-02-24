package com.pluggdd.burnandearn.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.view.fragment.LoginFragment;



public class SplashHandler extends Handler
{

    private FragmentManager mFragmentManager;
    private Context mContext;
    private Toolbar mToolBar;

    public SplashHandler(Context context,FragmentManager fragmentManager,Toolbar toolbar)
    {
        mFragmentManager = fragmentManager;
        mContext = context;
        mToolBar = toolbar;
    }

    public void handleMessage(Message message)
    {
        mToolBar.setVisibility(View.VISIBLE);
        SharedPreferences mSharedPreference = mContext.getSharedPreferences(mContext.getString(R.string.preference),Context.MODE_PRIVATE);
        if(mSharedPreference.getBoolean(mContext.getString(R.string.how_its_works_learned),false))
            new FragmentHelper(mFragmentManager).replaceFragment(R.id.fragment_container, new LoginFragment(),false);
        else
            new FragmentHelper(mFragmentManager).replaceFragment(R.id.fragment_container, new LoginFragment(), false);
    }
}
