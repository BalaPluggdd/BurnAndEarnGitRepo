package com.pluggdd.burnandearn.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FitBitApi;

import org.scribe.builder.ServiceBuilder;
import org.scribe.oauth.OAuthService;

public class FitBitActivity extends AppCompatActivity {

    private static final String EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION";
    private static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";
    private OAuthService mOAuthSevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit_bit);

        mOAuthSevice =  new ServiceBuilder()
                .provider(FitBitApi.class)
                .apiKey(getString(R.string.fit_bit_secret_key))
                .apiSecret(getString(R.string.fit_bit_api_key))
                .callback(getString(R.string.fit_bit_call_back_url))
                .scope("profile activity")
                .debug()
                .build();
        Log.i("OAuth version",mOAuthSevice.getVersion()+"");
        String authUrl = mOAuthSevice.getAuthorizationUrl(null);
        try {
            Intent customChromeTabIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
            Bundle extras = new Bundle();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                extras.putBinder(EXTRA_CUSTOM_TABS_SESSION,null);
            }
            customChromeTabIntent.putExtra(EXTRA_CUSTOM_TABS_TOOLBAR_COLOR, ContextCompat.getColor(FitBitActivity.this,R.color.colorPrimary));
            customChromeTabIntent.putExtras(extras);
            startActivity(customChromeTabIntent);
        }catch (Exception e){
            e.printStackTrace();
            // Please update google chrome to authenticate
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null && intent.getExtras()!=null){
            Uri uri = intent.getData();
            if(uri != null){
                String auth_cocde = uri.getQueryParameter("code");
                Intent mIntent = new Intent();
                mIntent.putExtra(getString(R.string.auth_token),auth_cocde);
                setResult(RESULT_OK,mIntent);
                finish();
            }else{
                Toast.makeText(this,"Relaunch App",Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
            }

        }
    }
}
