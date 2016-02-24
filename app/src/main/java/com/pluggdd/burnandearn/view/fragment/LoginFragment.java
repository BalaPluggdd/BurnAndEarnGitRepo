package com.pluggdd.burnandearn.view.fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.AlertNotification;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment to display and manage login details
 */
public class LoginFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final int GOOGLE_PLUS_SIGN_REQUEST_CODE = 1;
    public static final int REQUEST_GET_ACCOUNTS_PERMISSION_CODE = 2;
    private FragmentInteraction mListener;
    private CallbackManager mFBCallBackManager;
    private View mView;
    private GoogleApiClient mGoogleAPIClient;
    private GoogleApiAvailability mGooglePlayAvailability;
    private LoginButton mFBLoginButton;
    private PreferencesManager mPreferenceManager;
    private SignInButton mGooglePlusSignInButton;
    private ProgressDialog mProgressDialog;


    public LoginFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_login, container, false);
        // Referencing views
        mFBLoginButton = (LoginButton) mView.findViewById(R.id.fb_login_button);
        mGooglePlusSignInButton = (SignInButton) mView.findViewById(R.id.sign_in_button);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Logging in please wait");
        mProgressDialog.setCancelable(false);
        mPreferenceManager = new PreferencesManager(getActivity());
        // Google API client set up
        mGooglePlayAvailability = GoogleApiAvailability.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();
        if (mGooglePlayAvailability.isGooglePlayServicesAvailable(getContext()) == ConnectionResult.SUCCESS) {
            mGoogleAPIClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage(getActivity(), this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addApi(Plus.API)
                    .build();
        }
        mGooglePlusSignInButton.setSize(SignInButton.SIZE_WIDE);
        mGooglePlusSignInButton.setScopes(gso.getScopeArray());
        setGooglePlusButtonText(mGooglePlusSignInButton,getString(R.string.google_plus_login));
        // Facebook sign in set up
        mFBLoginButton.setFragment(this);
        mFBLoginButton.setReadPermissions(Arrays.asList("public_profile,email"));
        mFBLoginButton.registerCallback(mFBCallBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Snackbar.make(view, "Successs", Snackbar.LENGTH_SHORT).show();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                // Application code
                                Log.v("LoginActivity", response.toString());
                                if (response.getError() != null) {
                                    Snackbar.make(mView, "Unable to connect with facebook please try after sometime", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    String first_name = object.optString("first_name");
                                    String last_name = object.optString("last_name");
                                    String email_id = object.optString("email");
                                    String facebook_id = object.optString("id");
                                    mPreferenceManager.setStringValue(getString(R.string.first_name), first_name);
                                    mPreferenceManager.setStringValue(getString(R.string.last_name), last_name);
                                    mPreferenceManager.setStringValue(getString(R.string.email), email_id);
                                    //836913896406276
                                    mPreferenceManager.setStringValue(getString(R.string.profile_image_url), "https://graph.facebook.com/" + facebook_id + "/picture?type=large&width=400&height=400");
                                    if (new NetworkCheck().ConnectivityCheck(getContext())) {
                                        navigateToDashboard();
                                    } else {
                                        Snackbar.make(mView, getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
                                    }

                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Snackbar.make(mView, "Please login to facebook to use app", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Snackbar.make(mView, "Unable to connect with facebook please try after sometime", Snackbar.LENGTH_SHORT).show();
            }
        });


        mGooglePlusSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  googlePlusSignIn();
            }
        });

        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(mGoogleAPIClient != null)
            mGoogleAPIClient.connect();
        if (context instanceof FragmentInteraction) {
            mListener = (FragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPackageInstalled("com.google.android.apps.fitness")) {

        }else{
            AlertDialog.Builder mConfirmDialog = new AlertDialog.Builder(getActivity());
            mConfirmDialog.setTitle("Confirm");
            mConfirmDialog.setMessage("Thank you for installing BurnAndEarn.To start using, Please connect to Google FIT");
            mConfirmDialog.setCancelable(false);
            mConfirmDialog.setPositiveButton("Connect to FIT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.fitness")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.fitness")));
                    }
                }
            });

            mConfirmDialog.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.page_flag), LoginFragment.this.getClass().getSimpleName());
                    bundle.putString("button_pressed", "quit_app");
                    mListener.changeFragment(bundle);
                }
            });

            mConfirmDialog.show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mGoogleAPIClient !=null && mGoogleAPIClient.isConnected())
            mGoogleAPIClient.disconnect();
    }

    private void googlePlusSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleAPIClient);
        startActivityForResult(signInIntent, GOOGLE_PLUS_SIGN_REQUEST_CODE);
        mProgressDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_PLUS_SIGN_REQUEST_CODE) {
            if (resultCode != getActivity().RESULT_OK) {
                mProgressDialog.dismiss();
            }
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            getSignInResult(result);
        }
    }

    private void getSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            mPreferenceManager.setStringValue(getString(R.string.first_name), acct.getDisplayName());
            mPreferenceManager.setStringValue(getString(R.string.last_name), "");
            mPreferenceManager.setStringValue(getString(R.string.email), acct.getEmail());
            mPreferenceManager.setStringValue(getString(R.string.profile_image_url),acct.getPhotoUrl() != null ? acct.getPhotoUrl().toString() : "");
            navigateToDashboard();
            //Snackbar.make(mView, "success", Snackbar.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
        } else {
            // Signed out, show unauthenticated UI.
            Snackbar.make(mView, "Google sign in error please try after sometime", Snackbar.LENGTH_SHORT).show();
        }
    }

    /*
     get user's information name, email, profile pic,Date of birth,tag line and about me
     */

    private void getProfileInfo() {

        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleAPIClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleAPIClient);
                mPreferenceManager.setStringValue(getString(R.string.first_name), currentPerson.getDisplayName());
                mPreferenceManager.setStringValue(getString(R.string.last_name), "");
                mPreferenceManager.setStringValue(getString(R.string.email), Plus.AccountApi.getAccountName(mGoogleAPIClient));
                mPreferenceManager.setStringValue(getString(R.string.profile_image_url), currentPerson.getImage().getUrl());
                navigateToDashboard();
               // Snackbar.make(mView, "success", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(mView, "Personal information not found", Snackbar.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void navigateToDashboard() {
        Bundle extras = new Bundle();
        extras.putString(getString(R.string.page_flag), LoginFragment.class.getSimpleName());
        extras.putString(getString(R.string.button_pressed),getString(R.string.social_login));
        mListener.changeFragment(extras);
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GET_ACCOUNTS_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Handle permission granted
                    getProfileInfo();
                } else {
                    // Handle permission denied
                    Snackbar.make(mView, "Permission not granted", Snackbar.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                //tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
                tv.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.google_plus_button_padding),0,getResources().getDimensionPixelOffset(R.dimen.google_plus_button_padding));
                tv.setTypeface(null, Typeface.BOLD);
                //tv.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                tv.setText(buttonText);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
               // tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_google_plus,0,0,0);
                return;
            }
        }
    }

    private boolean isPackageInstalled(String packagename) {
        try {
            getContext().getPackageManager().getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
