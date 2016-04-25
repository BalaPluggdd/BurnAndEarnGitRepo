package com.pluggdd.burnandearn.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 25-Apr-16.
 */
public class WebserviceHelper {

    private Context mContext;
    private String mPageFlag;
    private ChildFragmentInteraction mChildFragmentInteraction;
    private View mView;
    private PreferencesManager mPreferenceManager;
    private Bundle mBundle;

    public WebserviceHelper(Context context,View view,String pageFlag,Bundle bundle,ChildFragmentInteraction childFragmentInteraction){
        mContext = context;
        mView = view;
        mPageFlag = pageFlag;
        mChildFragmentInteraction = childFragmentInteraction;
        mBundle = bundle;
        mPreferenceManager = new PreferencesManager(mContext);
    }

    private void updateProfile(final ProgressDialog mLoadingProgress) {
        mLoadingProgress.show();
        VolleySingleton volleyRequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyRequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.LOGIN_AND_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(" login response", response);
                mLoadingProgress.dismiss();
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1 || responseJson.optInt("status") == 2) {

                            mPreferenceManager.setStringValue(mContext.getString(R.string.user_id), responseJson.optString("userid"));
                            mPreferenceManager.setStringValue(mContext.getString(R.string.facebook_share), responseJson.optString("facebook_share"));
                            mPreferenceManager.setStringValue(mContext.getString(R.string.first_name), responseJson.optString("firstname"));
                            mPreferenceManager.setStringValue(mContext.getString(R.string.last_name), responseJson.optString("lastname"));
                            mPreferenceManager.setStringValue(mContext.getString(R.string.email), responseJson.optString("emailid"));
                            mPreferenceManager.setStringValue(mContext.getString(R.string.profile_image_url), responseJson.optString("profilepicture"));
                            mPreferenceManager.setIntValue(mContext.getString(R.string.gender), responseJson.optInt("gender"));
                            mPreferenceManager.setStringValue(mContext.getString(R.string.dob), responseJson.optString("dob").split("-")[2] + "-" + responseJson.optString("dob").split("-")[1] + "-" + responseJson.optString("dob").split("-")[0]);
                            mPreferenceManager.setStringValue(mContext.getString(R.string.email), responseJson.optString("emailid"));
                            //mPreferenceManager.setIntValue(getString(R.string.user_goal), mGoalSetUpSpinner.getSelectedItemPosition());
                            mPreferenceManager.setStringValue(mContext.getString(R.string.company), responseJson.optString("companyname"));
                            if (responseJson.optString("lastcaloriesupdate") != null && !responseJson.optString("lastcaloriesupdate").equalsIgnoreCase("") && !responseJson.optString("lastcaloriesupdate").startsWith("0000")) {
                                LocalDateTime lastUpdateddatetime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseLocalDateTime(responseJson.optString("lastcaloriesupdate"));
                                mPreferenceManager.setLongValue(mContext.getString(R.string.last_updated_calories_time), lastUpdateddatetime.toDateTime().getMillis());
                            } else
                                mPreferenceManager.setLongValue(mContext.getString(R.string.last_updated_calories_time), 0);

                            /*if (mSourcePageFlag.equalsIgnoreCase("login")) { // From login fragment
                                mPreferenceManager.setBooleanValue(getString(R.string.is_goal_set), true);
                                Bundle bundle = new Bundle();
                                bundle.putString(getString(R.string.page_flag), ProfileFragment.class.getSimpleName());
                                bundle.putString(getString(R.string.occupation), mOccupationSpinner.getSelectedItem().toString());
                                mChildFragmentInteraction.changeFragment(bundle);
                            } else {
                                Snackbar.make(mView, "Profile Updated Successfully", Snackbar.LENGTH_SHORT).show();
                            }*/
                            Bundle bundle = new Bundle();
                            bundle.putString(mContext.getString(R.string.page_flag), mPageFlag);
                            bundle.putInt(mContext.getString(R.string.user_goal),0);
                            mChildFragmentInteraction.changeChildFragment(bundle);

                        } else {
                            Snackbar.make(mView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(mView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoadingProgress.dismiss();
                Snackbar.make(mView, "Unable to connect to server", Snackbar.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("fbId", mPreferenceManager.getStringValue(mContext.getString(R.string.facebookId)));
                params.put("firstName",mBundle.getString(mContext.getString(R.string.name)));
                params.put("emailId", mBundle.getString(mContext.getString(R.string.email)));
                params.put("image", mBundle.getString(mContext.getString(R.string.profile_base64)));
                params.put("dob", mBundle.getString(mContext.getString(R.string.dob)));
                params.put("gender", String.valueOf(mBundle.getInt(mContext.getString(R.string.gender))));
                params.put("city", String.valueOf(mPreferenceManager.getIntValue(mContext.getString(R.string.selected_city))));
                params.put("occupation", String.valueOf(mPreferenceManager.getIntValue(mContext.getString(R.string.occupation))));
                params.put("parameter1",mBundle.getString(mContext.getString(R.string.paramter1)));
                params.put("parameter2",mBundle.getString(mContext.getString(R.string.paramter2)));
                params.put("parameter3",mBundle.getString(mContext.getString(R.string.paramter3)));
                params.put("parameter4",mBundle.getString(mContext.getString(R.string.paramter4)));
                params.put("deviceId", mPreferenceManager.getStringValue(mContext.getString(R.string.gcm_reg_id)));
                params.put("deviceType", String.valueOf(0));
                params.put("bloodgroup",mBundle.getString(mContext.getString(R.string.blood_group)));
                params.put("heighttype",mBundle.getString(mContext.getString(R.string.height_dimen)));
                params.put("height",mBundle.getString(mContext.getString(R.string.height)));
                params.put("weighttype",mBundle.getString(mContext.getString(R.string.weight_unit)));
                params.put("weight",mBundle.getString(mContext.getString(R.string.weight)));
                return params;
            }
        });
        volleyRequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }
}
