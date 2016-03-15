package com.pluggdd.burnandearn.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentHelper;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.view.fragment.AboutUsFragment;
import com.pluggdd.burnandearn.view.fragment.AppIntroductionFragment;
import com.pluggdd.burnandearn.view.fragment.ContactUsFragment;
import com.pluggdd.burnandearn.view.fragment.DashboardFragment;
import com.pluggdd.burnandearn.view.fragment.GoalSetupFragment;
import com.pluggdd.burnandearn.view.fragment.LoginFragment;
import com.pluggdd.burnandearn.view.fragment.OfferDetailFragment;
import com.pluggdd.burnandearn.view.fragment.OffersAndRewardsFragment;
import com.pluggdd.burnandearn.view.fragment.ShareFragment;
import com.pluggdd.burnandearn.view.fragment.TotalPointsEarnedFragment;

/**
 * Activity to hold Burn and Earn fragments..
 */
public class BurnAndEarnMainActivity extends AppCompatActivity implements FragmentInteraction {

    // Initialization of views
    private Toolbar mToolBar;
    private FragmentHelper mFragmentHelper;
    private Spinner mActivitiesTimeSpinner;
    private ImageView mBackImage;
    private TextView mToolBarTitle;
    private PreferencesManager mPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burn_and_earn_main);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mActivitiesTimeSpinner = (Spinner) findViewById(R.id.activities_time_spinner);
        mBackImage = (ImageView) findViewById(R.id.img_back);
        mToolBarTitle = (TextView) findViewById(R.id.txt_title);
        setSupportActionBar(mToolBar);
        mFragmentHelper = new FragmentHelper(getSupportFragmentManager());
        getSupportActionBar().setTitle("");
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        mPreferenceManager = new PreferencesManager(this);
        // To add Login fragment as first fragment in transaction
        if(!mPreferenceManager.getBooleanValue(getString(R.string.is_user_logged_in))){ // If user does n't logged in
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolBar.setVisibility(View.GONE);
            mFragmentHelper.addFragment(R.id.fragment_container, new LoginFragment());
        }else if(!mPreferenceManager.getBooleanValue(getString(R.string.is_goal_set))){ // If goal does n't set
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolBar.setVisibility(View.GONE);
            mFragmentHelper.addFragment(R.id.fragment_container, new GoalSetupFragment());
        }else if(!mPreferenceManager.getBooleanValue(getString(R.string.is_how_its_works_learned))){ // If user logged in and does n't learned how it will work
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mToolBar.setVisibility(View.GONE);
            mFragmentHelper.addFragment(R.id.fragment_container, new AppIntroductionFragment());
        }else{
            mToolBar.setVisibility(View.VISIBLE);
            mActivitiesTimeSpinner.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("");
            mFragmentHelper.addFragment(R.id.fragment_container, new DashboardFragment());
        }

        mBackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlebackpressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If received from Dashboard fragment , bypasss the callback to Dashboard Fragment's onActivityResult
        Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(mCurrentFragment instanceof LoginFragment || mCurrentFragment instanceof DashboardFragment || mCurrentFragment instanceof ShareFragment){
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // If received from Dashboard fragment , bypasss the callback to Dashboard Fragment's onRequestPermissionResult
        Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(mCurrentFragment instanceof DashboardFragment){
            mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (mCurrentFragment instanceof LoginFragment || mCurrentFragment instanceof GoalSetupFragment || mCurrentFragment instanceof AppIntroductionFragment || mCurrentFragment instanceof DashboardFragment) {
            super.onBackPressed();
        } else { // Dont finish actvity just popstack fragment from backstack
            invalidateOptionsMenu();
            handlebackpressed();
        }
    }

    @Override
    public void changeFragment(Bundle extras) {
        switch (extras.getString(getString(R.string.page_flag))) {
            case "LoginFragment":
                if (extras.getString(getString(R.string.button_pressed)).equalsIgnoreCase(getString(R.string.social_login))) {
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new GoalSetupFragment(), false);
                } else {
                    finish();
                }
                break;
            case "GoalSetupFragment":
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                mFragmentHelper.replaceFragment(R.id.fragment_container, new AppIntroductionFragment(), false);
                break;
            case "AppIntroductionFragment":
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                mToolBar.setVisibility(View.VISIBLE);
                mActivitiesTimeSpinner.setVisibility(View.VISIBLE);
                mBackImage.setVisibility(View.GONE);
                getSupportActionBar().setTitle("");
                mToolBarTitle.setText("");
                getSupportActionBar().setIcon(R.mipmap.ic_launcher);
                invalidateOptionsMenu();
                mFragmentHelper.replaceFragment(R.id.fragment_container, new DashboardFragment(), false);
                break;
            case "DashboardFragment":
                mActivitiesTimeSpinner.setVisibility(View.GONE);
                mBackImage.setVisibility(View.VISIBLE);
                mToolBarTitle.setVisibility(View.VISIBLE);
                getSupportActionBar().setIcon(0);
                mToolBarTitle.setText("Amazon");
                mFragmentHelper.replaceFragment(R.id.fragment_container, new OfferDetailFragment(), true);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        switch (item.getItemId()) {
            case R.id.action_offers_and_rewards:
                if (mCurrentFragment instanceof OffersAndRewardsFragment == false) {
                    mActivitiesTimeSpinner.setVisibility(View.GONE);
                    mBackImage.setVisibility(View.VISIBLE);
                    mToolBarTitle.setVisibility(View.VISIBLE);
                    getSupportActionBar().setIcon(0);
                    mToolBarTitle.setText(getString(R.string.action_offers_and_rewards));
                    if(mCurrentFragment instanceof DashboardFragment == false)
                        mFragmentHelper.removeFromBackStack();
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new OffersAndRewardsFragment(), true);
                    /*if(mCurrentFragment instanceof DashboardFragment)
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new OffersAndRewardsFragment(), true);
                    else
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new OffersAndRewardsFragment(), false);*/
                }
                break;
            case R.id.action_share:
                if (mCurrentFragment instanceof ShareFragment == false) {
                    mActivitiesTimeSpinner.setVisibility(View.GONE);
                    mBackImage.setVisibility(View.VISIBLE);
                    mToolBarTitle.setVisibility(View.VISIBLE);
                    getSupportActionBar().setIcon(0);
                    mToolBarTitle.setText(getString(R.string.share));
                    if(mCurrentFragment instanceof DashboardFragment == false)
                        mFragmentHelper.removeFromBackStack();
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new ShareFragment(), true);
                    /*if(mCurrentFragment instanceof DashboardFragment)
                       mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), true);
                    else
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), false);*/
                }
                break;
            case R.id.action_how_it_works:
                mToolBar.setVisibility(View.GONE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                /*if(mCurrentFragment instanceof DashboardFragment == false)
                    mFragmentHelper.removeFromBackStack();*/
                mFragmentHelper.replaceFragment(R.id.fragment_container, new AppIntroductionFragment(), false);
                break;
            case R.id.action_about_us:
                if (mCurrentFragment instanceof AboutUsFragment == false) {
                    mActivitiesTimeSpinner.setVisibility(View.GONE);
                    mBackImage.setVisibility(View.VISIBLE);
                    mToolBarTitle.setVisibility(View.VISIBLE);
                    getSupportActionBar().setIcon(0);
                    mToolBarTitle.setText(getString(R.string.action_about_us));
                    if(mCurrentFragment instanceof DashboardFragment == false)
                        mFragmentHelper.removeFromBackStack();
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), true);
                    /*if(mCurrentFragment instanceof DashboardFragment)
                       mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), true);
                    else
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), false);*/
                }
                break;
            case R.id.action_contact_us:
                if (mCurrentFragment instanceof ContactUsFragment == false) {
                    mActivitiesTimeSpinner.setVisibility(View.GONE);
                    mBackImage.setVisibility(View.VISIBLE);
                    mToolBarTitle.setVisibility(View.VISIBLE);
                    getSupportActionBar().setIcon(0);
                    mToolBarTitle.setText(getString(R.string.action_contact_us));
                    if(mCurrentFragment instanceof DashboardFragment == false)
                        mFragmentHelper.removeFromBackStack();
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new ContactUsFragment(), true);
                    /*if(mCurrentFragment instanceof DashboardFragment)
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new ContactUsFragment(), true);
                    else
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new ContactUsFragment(), false);*/
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mPreferenceManager.getStringValue(getString(R.string.facebook_share)).equalsIgnoreCase("yes"))
            menu.findItem(R.id.action_share).setEnabled(false);
        return super.onPrepareOptionsMenu(menu);
    }

   /**
     * To handle backpressed event while navigating from inner fragments
     */
    private void handlebackpressed() {
        mActivitiesTimeSpinner.setVisibility(View.VISIBLE);
        mBackImage.setVisibility(View.GONE);
        mToolBarTitle.setVisibility(View.GONE);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle("");
        mFragmentHelper.removeFromBackStack();
    }

}
