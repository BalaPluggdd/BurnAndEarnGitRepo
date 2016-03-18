package com.pluggdd.burnandearn.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentHelper;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.view.fragment.PointsFragment;
import com.pluggdd.burnandearn.view.fragment.TrendsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to hold Burn and Earn fragments..
 */
public class BurnAndEarnMainActivity extends BaseActivity /*implements FragmentInteraction*/ {

    // Initialization of views
    private View mParentView;
    private Toolbar mToolBar;
    private TabLayout mTabLayout;
    private ViewPager mTabViewPager;
    private FragmentHelper mFragmentHelper;
    //private Spinner mActivitiesTimeSpinner;
    //private ImageView mBackImage;
    //private TextView mToolBarTitle;
    private PreferencesManager mPreferenceManager;
    private final int REQUEST_CODE_RESOLUTION = 100, REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 101, REQUEST_GETACCOUNTS_PERMISSIONS_REQUEST_CODE = 102, REQUEST_BOTH_PERMISSION_CODE = 103;
    private boolean mIsPermissionRequestRaised;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burn_and_earn_main);
        mParentView = findViewById(R.id.parent_view);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        //mActivitiesTimeSpinner = (Spinner) findViewById(R.id.activities_time_spinner);
       // mBackImage = (ImageView) findViewById(R.id.img_back);
       // mToolBarTitle = (TextView) findViewById(R.id.txt_title);
        setSupportActionBar(mToolBar);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabViewPager = (ViewPager) findViewById(R.id.tab_viewpager);
        mFragmentHelper = new FragmentHelper(getSupportFragmentManager());
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        setUpHomePageTabs();
        /*mPreferenceManager = new PreferencesManager(this);
        // To add Login fragment as first fragment in transaction
        if (!mPreferenceManager.getBooleanValue(getString(R.string.is_user_logged_in))) { // If user does n't logged in
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            hideAppBarComponents();
            mFragmentHelper.addFragment(R.id.fragment_container, new LoginFragment());
        } else if (!mPreferenceManager.getBooleanValue(getString(R.string.is_goal_set))) { // If goal does n't set
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            hideAppBarComponents();
            mFragmentHelper.addFragment(R.id.fragment_container, new ProfileFragment());
        } else if (!mPreferenceManager.getBooleanValue(getString(R.string.is_how_its_works_learned))) { // If user logged in and does n't learned how it will work
            // To set translucent status bar
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            hideAppBarComponents();
            mFragmentHelper.addFragment(R.id.fragment_container, new AppIntroductionFragment());
        } else {
            //mActivitiesTimeSpinner.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("");
            setUpHomePageTabs();
            //mFragmentHelper.addFragment(R.id.fragment_container, new PointsFragment());
        }*/

        /*mBackImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlebackpressed();
            }
        });*/
    }

    private void setUpHomePageTabs() {
        // To add Tabs
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PointsFragment(), "Points");
        adapter.addFragment(new TrendsFragment(), "Trends");
        mTabViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mTabViewPager);
    }

    private void hideAppBarComponents() {
        mToolBar.setVisibility(View.GONE);
        mTabViewPager.setVisibility(View.GONE);
        mTabLayout.setVisibility(View.GONE);
    }

    private void showAppBarComponents() {
        mToolBar.setVisibility(View.VISIBLE);
        mTabViewPager.setVisibility(View.VISIBLE);
        mTabLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If received from Dashboard fragment , bypasss the callback to Dashboard Fragment's onActivityResult
        /*Fragment mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (mCurrentFragment instanceof LoginFragment || mCurrentFragment instanceof PointsFragment || mCurrentFragment instanceof ShareFragment) {
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
        }*/

    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("Permission Result", "onRequestPermissionResult");
        switch (requestCode) {
            case REQUEST_BOTH_PERMISSION_CODE: {
                mIsPermissionRequestRaised = true;
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.GET_ACCOUNTS, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
                    setUpHomePageTabs();
                } else {
                    Snackbar.make(
                            mParentView,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.settings, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Build intent that displays the App settings screen.
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .show();

                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkLocationPermissions() && checkGetAccountsPermissions()) {
            setUpHomePageTabs();
        } else if (mIsPermissionRequestRaised) {
            Snackbar.make(
                    mParentView,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.settings, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }*/



    /**
     * Return the current state of the permissions needed.
     */
    /*private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(BurnAndEarnMainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkGetAccountsPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(BurnAndEarnMainActivity.this,
                Manifest.permission.GET_ACCOUNTS);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }*/

    /*@Override
    public void changeFragment(Bundle extras) {
        switch (extras.getString(getString(R.string.page_flag))) {
            case "LoginFragment":
                if (extras.getString(getString(R.string.button_pressed)).equalsIgnoreCase(getString(R.string.social_login))) {
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new ProfileFragment(), false);
                } else {
                    finish();
                }
                break;
            case "ProfileFragment":
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                mFragmentHelper.replaceFragment(R.id.fragment_container, new AppIntroductionFragment(), false);
                break;
            case "AppIntroductionFragment":
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                showAppBarComponents();
                //mActivitiesTimeSpinner.setVisibility(View.VISIBLE);
                mBackImage.setVisibility(View.GONE);
                getSupportActionBar().setTitle("");
                mToolBarTitle.setText("");
                getSupportActionBar().setIcon(R.mipmap.ic_launcher);
                setUpHomePageTabs();
                invalidateOptionsMenu();
                mFragmentHelper.replaceFragment(R.id.fragment_container, new Fragment(), false);
                break;
            case "PointsFragment":
                //mActivitiesTimeSpinner.setVisibility(View.GONE);
                mBackImage.setVisibility(View.VISIBLE);
                mToolBarTitle.setVisibility(View.VISIBLE);
                getSupportActionBar().setIcon(0);
                mToolBarTitle.setText("Amazon");
                mFragmentHelper.replaceFragment(R.id.fragment_container, new OfferDetailFragment(), true);
                break;
        }
    }*/

    /*@Override
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
                startActivity(new Intent(BurnAndEarnMainActivity.this,OfferAndRewardsActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                *//*if (mCurrentFragment instanceof OffersAndRewardsFragment == false) {
                    mTabLayout.setVisibility(View.GONE);
                    mTabViewPager.setVisibility(View.GONE);
                    mActivitiesTimeSpinner.setVisibility(View.GONE);
                    mBackImage.setVisibility(View.VISIBLE);
                    mToolBarTitle.setVisibility(View.VISIBLE);
                    getSupportActionBar().setIcon(0);
                    mToolBarTitle.setText(getString(R.string.action_offers_and_rewards));
                    if (mCurrentFragment instanceof PointsFragment == false)
                        mFragmentHelper.removeFromBackStack();
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new OffersAndRewardsFragment(), true);
                    *//**//*if(mCurrentFragment instanceof PointsFragment)
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new OffersAndRewardsFragment(), true);
                    else
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new OffersAndRewardsFragment(), false);*//**//*
                }*//*
                break;
            case R.id.action_share:
                startActivity(new Intent(BurnAndEarnMainActivity.this,ShareActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                *//*if (mCurrentFragment instanceof ShareFragment == false) {
                    mTabLayout.setVisibility(View.GONE);
                    mTabViewPager.setVisibility(View.GONE);
                    mActivitiesTimeSpinner.setVisibility(View.GONE);
                    mBackImage.setVisibility(View.VISIBLE);
                    mToolBarTitle.setVisibility(View.VISIBLE);
                    getSupportActionBar().setIcon(0);
                    mToolBarTitle.setText(getString(R.string.share));
                    if (mCurrentFragment instanceof PointsFragment == false)
                        mFragmentHelper.removeFromBackStack();
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new ShareFragment(), true);
                    *//**//*if(mCurrentFragment instanceof PointsFragment)
                       mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), true);
                    else
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), false);*//**//*
                }*//*
                break;
            case R.id.action_how_it_works:
                startActivity(new Intent(BurnAndEarnMainActivity.this,InitialSetUpActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                *//*hideAppBarComponents();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                *//**//*if(mCurrentFragment instanceof PointsFragment == false)
                    mFragmentHelper.removeFromBackStack();*//**//*
                mFragmentHelper.replaceFragment(R.id.fragment_container, new AppIntroductionFragment(), false);*//*
                break;
            case R.id.action_about_us:
                startActivity(new Intent(BurnAndEarnMainActivity.this,AboutUsActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                *//*if (mCurrentFragment instanceof AboutUsFragment == false) {
                    mTabLayout.setVisibility(View.GONE);
                    mTabViewPager.setVisibility(View.GONE);
                    mActivitiesTimeSpinner.setVisibility(View.GONE);
                    mBackImage.setVisibility(View.VISIBLE);
                    mToolBarTitle.setVisibility(View.VISIBLE);
                    getSupportActionBar().setIcon(0);
                    mToolBarTitle.setText(getString(R.string.action_about_us));
                    if (mCurrentFragment instanceof PointsFragment == false)
                        mFragmentHelper.removeFromBackStack();
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), true);
                    *//**//*if(mCurrentFragment instanceof PointsFragment)
                       mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), true);
                    else
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new AboutUsFragment(), false);*//**//*
                }*//*
                break;
            case R.id.action_contact_us:
                startActivity(new Intent(BurnAndEarnMainActivity.this,ContactUsActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                *//*if (mCurrentFragment instanceof ContactUsFragment == false) {
                    mTabLayout.setVisibility(View.GONE);
                    mTabViewPager.setVisibility(View.GONE);
                    mActivitiesTimeSpinner.setVisibility(View.GONE);
                    mBackImage.setVisibility(View.VISIBLE);
                    mToolBarTitle.setVisibility(View.VISIBLE);
                    getSupportActionBar().setIcon(0);
                    mToolBarTitle.setText(getString(R.string.action_contact_us));
                    if (mCurrentFragment instanceof PointsFragment == false)
                        mFragmentHelper.removeFromBackStack();
                    mFragmentHelper.replaceFragment(R.id.fragment_container, new ContactUsFragment(), true);
                    *//**//*if(mCurrentFragment instanceof PointsFragment)
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new ContactUsFragment(), true);
                    else
                        mFragmentHelper.replaceFragment(R.id.fragment_container, new ContactUsFragment(), false);*//**//*
                }*//*
                break;

        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(R.id.action_home);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * To handle backpressed event while navigating from inner fragments
     */
    private void handlebackpressed() {
        //mActivitiesTimeSpinner.setVisibility(View.VISIBLE);
        mTabLayout.setVisibility(View.VISIBLE);
        mTabViewPager.setVisibility(View.VISIBLE);
       // mBackImage.setVisibility(View.GONE);
        //mToolBarTitle.setVisibility(View.GONE);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle("");
        mFragmentHelper.removeFromBackStack();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void checkAndBuildGoogleApiClient() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<String>();
            final List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.GET_ACCOUNTS))
                permissionsNeeded.add("Get Accounts");
            if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                permissionsNeeded.add("Location");
            if (permissionsList.size() > 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),REQUEST_BOTH_PERMISSION_CODE);
                return;
            }

        } else {

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(BurnAndEarnMainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }


}
