package com.pluggdd.burnandearn.activity;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ShareActivity extends BaseActivity {

    private CallbackManager mFBCallBackManager;
    private ShareButton mFBShareButton;
    private ProgressDialog mProgressDialog;
    private View mParentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_share);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFBCallBackManager = CallbackManager.Factory.create();
        mParentView = findViewById(R.id.parent_view);
        mFBShareButton = (ShareButton) findViewById(R.id.btn_fbshare);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Adding points please wait");
        mProgressDialog.setCancelable(false);
        cancelNotification();
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.katana&hl=en"))
                .setContentTitle(getString(R.string.app_name))
                .setContentDescription(getString(R.string.share_and_earn))
                .build();
        mFBShareButton.setShareContent(content);
        mFBShareButton.registerCallback(mFBCallBackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                socialShare("facebook");
            }

            @Override
            public void onCancel() {
                //Toast.makeText(getContext(), "cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(ShareActivity.this, "Error occured please try after sometime", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(R.id.action_share);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFBCallBackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void socialShare(final String social_flag){
        mProgressDialog.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.SOCIAL_SHARE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                mProgressDialog.dismiss();
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1) {
                            new PreferencesManager(ShareActivity.this).setStringValue(getString(R.string.facebook_share), "yes");
                            Snackbar.make(mParentView, "Points added to your profile", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(mParentView, "Points already added, want to restrict this", Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(mParentView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                Snackbar.make(mParentView, "Unable to connect to server", Snackbar.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", new PreferencesManager(ShareActivity.this).getStringValue(getString(R.string.user_id)));
                params.put("social_share", social_flag);
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }
}
