package com.pluggdd.burnandearn.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.view.fragment.OfferDetailFragment;
import com.pluggdd.burnandearn.view.fragment.OffersAndRewardsFragment;
import com.pluggdd.burnandearn.view.fragment.PointsFragment;
import com.pluggdd.burnandearn.view.fragment.TrendsFragment;

import java.util.ArrayList;
import java.util.List;

public class OfferAndRewardsActivity extends BaseActivity{

    private TabLayout mTabLayout;
    private ViewPager mTabViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_and_rewards);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabViewPager = (ViewPager) findViewById(R.id.tab_viewpager);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpTabs();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(R.id.action_offers_and_rewards);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.enter_from_left,R.anim.exit_to_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private void setUpTabs() {
        // To add Tabs
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(OffersAndRewardsFragment.newInstance(1), "Week");
        adapter.addFragment(OffersAndRewardsFragment.newInstance(2), "Fortnight");
        adapter.addFragment(OffersAndRewardsFragment.newInstance(3), "Month");
        mTabViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mTabViewPager);
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


}
