package com.pluggdd.burnandearn.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentHelper;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.view.fragment.ProfileFragment;
import com.pluggdd.burnandearn.view.fragment.RegistrationFragment;

public class ProfileActivity extends BaseActivity implements FragmentInteraction {

    private FragmentHelper mFragmentHelper;
    private String mPageFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFragmentHelper = new FragmentHelper(getSupportFragmentManager());
        mPageFlag = getIntent().getExtras().getString(getString(R.string.page_flag));
        if (mPageFlag.equalsIgnoreCase("profile_menu"))
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFragmentHelper.addFragment(R.id.fragment_container, RegistrationFragment.getInstance(mPageFlag));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mPageFlag.equalsIgnoreCase("profile_menu"))
            menu.removeItem(R.id.action_profile);
        else {
            menu.clear();

        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    @Override
    public void changeFragment(Bundle extras) {
        Intent intent = new Intent(ProfileActivity.this, BurnAndEarnMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        finish();
    }
}
