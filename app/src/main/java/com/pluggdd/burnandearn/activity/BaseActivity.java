package com.pluggdd.burnandearn.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.view.fragment.ProfileFragment;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent intent = new Intent(BaseActivity.this,BurnAndEarnMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            case R.id.action_offers_and_rewards:
                startActivity(new Intent(BaseActivity.this,OfferAndRewardsActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            case R.id.action_my_offers:
                startActivity(new Intent(BaseActivity.this,MyOffersActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            case R.id.action_share:
                startActivity(new Intent(BaseActivity.this,ShareActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            case R.id.action_profile:
                startActivity(new Intent(BaseActivity.this,ProfileActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            case R.id.action_how_it_works:
                Intent how_it_works_intent = new Intent(BaseActivity.this,InitialSetUpActivity.class);
                how_it_works_intent.putExtra(getString(R.string.page_flag),getString(R.string.action_how_it_works));
                startActivity(how_it_works_intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            case R.id.action_about_us:
                startActivity(new Intent(BaseActivity.this,AboutUsActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;
            case R.id.action_contact_us:
                startActivity(new Intent(BaseActivity.this,ContactUsActivity.class));
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (new PreferencesManager(BaseActivity.this).getStringValue(getString(R.string.facebook_share)).equalsIgnoreCase("yes"))
            menu.findItem(R.id.action_share).setEnabled(false);
        return super.onPrepareOptionsMenu(menu);
    }
}
