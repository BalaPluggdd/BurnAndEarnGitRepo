package com.pluggdd.burnandearn.service;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RegistrationIntentService extends IntentService {

    // abbreviated tag name
    private static final String TAG = "RegIntentService";
    private PreferencesManager mPreferenceManager;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Make a call to Instance API
        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_SenderId);
        try {
            // request token that will be used by the server to send push notifications
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Log.d(TAG, "GCM Registration Token: " + token);
            mPreferenceManager = new PreferencesManager(RegistrationIntentService.this);
            mPreferenceManager.setStringValue(getString(R.string.gcm_reg_id), token);
            sendRegistrationToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRegistrationToServer() {
        // Add custom implementation, as needed.
        if(!mPreferenceManager.getBooleanValue(getString(R.string.is_gcm_token_updated)) && !mPreferenceManager.getStringValue(getString(R.string.user_id)).equalsIgnoreCase("")){
            updateGCMRegToken();
        }
        // Have to send updated code to server and add whether its sent or not in preference
    }


    private void updateGCMRegToken() {
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.UPDATE_GCM_REG_TOKEN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("update gcm response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1) {
                            mPreferenceManager.setBooleanValue(getString(R.string.is_gcm_token_updated), true);
                        } else {
                            mPreferenceManager.setBooleanValue(getString(R.string.is_gcm_token_updated), false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mPreferenceManager.setBooleanValue(getString(R.string.is_gcm_token_updated), false);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mPreferenceManager.setBooleanValue(getString(R.string.is_gcm_token_updated), false);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", mPreferenceManager.getStringValue(getString(R.string.user_id)));
                params.put("deviceId", mPreferenceManager.getStringValue(getString(R.string.gcm_reg_id)));
                params.put("deviceType", String.valueOf(0));
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }
}
