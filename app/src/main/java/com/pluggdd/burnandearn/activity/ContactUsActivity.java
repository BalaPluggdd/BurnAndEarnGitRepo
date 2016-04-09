package com.pluggdd.burnandearn.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ContactUsActivity extends BaseActivity {

    private ProgressDialog mProgressDialog;
    private View mView;
    private EditText mSubjectEdt,mFeedbackEdt;
    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mView = findViewById(R.id.container);
        mSubjectEdt = (EditText) findViewById(R.id.edt_subject);
        mFeedbackEdt = (EditText) findViewById(R.id.edt_feedback_request);
        mSendButton = (Button) findViewById(R.id.btn_send);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Sending Feedback !!!");
        mProgressDialog.setCancelable(false);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = mSubjectEdt.getText().toString();
                String feedback = mFeedbackEdt.getText().toString();
                if(TextUtils.isEmpty(subject)){
                    Snackbar.make(mView,"Enter subject",Snackbar.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(feedback)){
                    Snackbar.make(mView,"Enter feedback",Snackbar.LENGTH_SHORT).show();
                }else{
                    if(new NetworkCheck().ConnectivityCheck(ContactUsActivity.this)){
                        sendFeedback(subject,feedback);
                    }else{
                        Snackbar.make(mView,getString(R.string.no_network),Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(R.id.action_contact_us);
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

    private void sendFeedback(final String subject, final String feedback){
        mProgressDialog.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.SEND_FEEDBACK, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                mProgressDialog.dismiss();
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        Snackbar.make(mView, responseJson.optString("msg"), Snackbar.LENGTH_SHORT).show();
                        /*if (responseJson.optInt("status") == 1) {
                            Snackbar.make(mView, responseJson.optString("msg"), Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(mView, responseJson.optString("msg"), Snackbar.LENGTH_SHORT).show();
                        }*/
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(mView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                Snackbar.make(mView, "Unable to connect to server", Snackbar.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", new PreferencesManager(ContactUsActivity.this).getStringValue(getString(R.string.first_name)));
                params.put("subject", subject);
                params.put("feedback", subject);
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }
}
