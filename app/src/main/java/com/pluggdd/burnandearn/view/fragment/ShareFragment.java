package com.pluggdd.burnandearn.view.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareFragment extends Fragment {

    private View mView;
    private CallbackManager mFBCallBackManager;
    private ShareButton mFBShareButton;
    private ProgressDialog mProgressDialog;

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up Facebook
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mFBCallBackManager = CallbackManager.Factory.create();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_share, container, false);
        mFBShareButton = (ShareButton) mView.findViewById(R.id.btn_fbshare);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Adding points please wait");
        mProgressDialog.setCancelable(false);

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.katana&hl=en"))
                .setContentTitle(getString(R.string.app_name))
                .setContentDescription(getString(R.string.share_and_earn))
                .build();
        mFBShareButton.setShareContent(content);
        mFBShareButton.registerCallback(mFBCallBackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                if (new NetworkCheck().ConnectivityCheck(getActivity())) {
                    socialShare("facebook");
                } else {
                    Snackbar.make(mView,getString(R.string.no_network),Snackbar.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancel() {
                //Toast.makeText(getContext(), "cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getContext(), "Error occured please try after sometime", Toast.LENGTH_SHORT).show();
            }
        });

        /*mFBShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        return mView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFBCallBackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void socialShare(final String social_flag){
        mProgressDialog.show();
        RequestQueue mRequestQueue = VolleySingleton.getSingletonInstance().getRequestQueue();
        mRequestQueue.add((new StringRequest(Request.Method.POST, WebserviceAPI.SOCIAL_SHARE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                mProgressDialog.dismiss();
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optString("msg").equalsIgnoreCase("Points Credited")) {
                            new PreferencesManager(getContext()).setStringValue(getString(R.string.facebook_share),"yes");
                            Snackbar.make(mView, "Points added to your profile", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(mView, "Points already added, want to restrict this", Snackbar.LENGTH_SHORT).show();
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
                mProgressDialog.dismiss();
                Snackbar.make(mView, "Unable to connect to server", Snackbar.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId", new PreferencesManager(getContext()).getStringValue(getString(R.string.user_id)));
                params.put("social_share", social_flag);
                return params;
            }
        }));
    }
}
