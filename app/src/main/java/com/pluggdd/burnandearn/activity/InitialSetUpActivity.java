package com.pluggdd.burnandearn.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentHelper;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.view.fragment.AppIntroductionFragment;
import com.pluggdd.burnandearn.view.fragment.ProfileFragment;
import com.pluggdd.burnandearn.view.fragment.LoginFragment;
import com.pluggdd.burnandearn.view.fragment.PointsFragment;
import com.pluggdd.burnandearn.view.fragment.ShareFragment;

public class InitialSetUpActivity extends AppCompatActivity implements FragmentInteraction {

    private PreferencesManager mPreferenceManager;
    private FragmentHelper mFragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_set_up);
        mPreferenceManager = new PreferencesManager(this);
        mFragmentHelper = new FragmentHelper(getSupportFragmentManager());
        // To add Login fragment as first fragment in transaction
        if (!mPreferenceManager.getBooleanValue(getString(R.string.is_user_logged_in))) { // If user does n't logged in
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mFragmentHelper.addFragment(R.id.fragment_container, new LoginFragment());
        } else if (!mPreferenceManager.getBooleanValue(getString(R.string.is_goal_set))) { // If goal does n't set
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mFragmentHelper.addFragment(R.id.fragment_container, ProfileFragment.getInstance("login"));
        } else if (!mPreferenceManager.getBooleanValue(getString(R.string.is_how_its_works_learned)) || getIntent().hasExtra(getString(R.string.page_flag))) { // If user logged in and does n't learned how it will work
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mFragmentHelper.addFragment(R.id.fragment_container, new AppIntroductionFragment());
        } else {
            Intent intent = new Intent(InitialSetUpActivity.this,BurnAndEarnMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If received from Dashboard fragment , bypasss the callback to Dashboard Fragment's onActivityResult
        Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (mCurrentFragment instanceof LoginFragment || mCurrentFragment instanceof PointsFragment || mCurrentFragment instanceof ShareFragment) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If received from Dashboard fragment , bypasss the callback to Dashboard Fragment's onRequestPermissionResult
        Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (mCurrentFragment instanceof PointsFragment) {
            mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void changeFragment(Bundle extras) {
        switch (extras.getString(getString(R.string.page_flag))) {
            case "LoginFragment":
                if (extras.getString(getString(R.string.button_pressed)).equalsIgnoreCase(getString(R.string.social_login))) {
                    mFragmentHelper.replaceFragment(R.id.fragment_container,ProfileFragment.getInstance("login"), false);
                } else {
                    finish();
                }
                break;
            case "ProfileFragment":
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                mFragmentHelper.replaceFragment(R.id.fragment_container, new AppIntroductionFragment(), false);
                break;
            case "AppIntroductionFragment":
                Intent intent = new Intent(InitialSetUpActivity.this,BurnAndEarnMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                finish();
                break;

        }
    }



}
