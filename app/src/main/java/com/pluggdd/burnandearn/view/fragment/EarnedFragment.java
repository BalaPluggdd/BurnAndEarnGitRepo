package com.pluggdd.burnandearn.view.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.plus.Plus;
import com.pluggdd.burnandearn.BuildConfig;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.BurnAndEarnMainActivity;
import com.pluggdd.burnandearn.activity.FitBitActivity;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.model.FitnessHistory;
import com.pluggdd.burnandearn.utils.FitBitApi;
import com.pluggdd.burnandearn.utils.FitBitHelper;
import com.pluggdd.burnandearn.utils.FitnessSource;
import com.pluggdd.burnandearn.utils.GoogleFitHelper;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;
import com.pluggdd.burnandearn.view.adapter.BusinessOfferAdapter;
import com.pluggdd.burnandearn.view.adapter.OfferRewardsAdapter;

import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class EarnedFragment extends Fragment {

    public static final int REQUEST_CODE_RESOLUTION = 1000, REQUEST_BOTH_PERMISSION_CODE = 1001 , REQUEST_FITBIT_API = 104;
    private Context mContext;
    private View mView;
    private ProgressBar mLoadingProgressBar;
    private GoogleApiClient mGoogleAPIClient;
    private boolean mIsFitnessOfferListLoaded, mIsPermissionRequestRaised;
    private PreferencesManager mPreferenceManager;
    private String mFitnessEmail;
    private TextView mNoOfferText;
    private ArrayList<FitnessHistory> mFitnessHistoryList = new ArrayList<>();
    private RecyclerView mBusinessOfferRecyclerView;
    private ArrayList<BusinessDetails> mBusinesOfferList;
    private OAuthService mOAuthSevice;
    private FitBitHelper mFitBitHelper;

    public EarnedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_trends, container, false);
        mLoadingProgressBar = (ProgressBar) mView.findViewById(R.id.loading_progress_bar);
        mBusinessOfferRecyclerView = (RecyclerView) mView.findViewById(R.id.offers_and_rewards_recycle_view);
        mNoOfferText = (TextView) mView.findViewById(R.id.txt_no_offers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mBusinessOfferRecyclerView.setLayoutManager(layoutManager);
        mPreferenceManager = new PreferencesManager(getActivity());

        return mView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !mIsFitnessOfferListLoaded){
            int fitness_source = mPreferenceManager.getIntValue(getString(R.string.selected_fitness_source));
            if(fitness_source == 1){
                if (checkLocationPermissions() && checkGetAccountsPermissions()) {
                    checkAndBuildGoogleApiClient();
                    mIsFitnessOfferListLoaded = true;
                } else {
                    Snackbar.make(
                            mView,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.settings, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Build intent that displays the App settings screen.
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
            }else{
                if(mPreferenceManager.getBooleanValue(getString(R.string.is_fitbit_authenticated))){
                    checkAndBuildFitBitApiClient();
                    new FitnessHistoryAsync().execute();
                }else{
                    startActivityForResult(new Intent(mContext, FitBitActivity.class),REQUEST_FITBIT_API);
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_RESOLUTION:
                    mGoogleAPIClient.connect();
                    break;
                case REQUEST_FITBIT_API :
                    if(new NetworkCheck().ConnectivityCheck(mContext)){
                        checkAndBuildFitBitApiClient();
                        String auth_token = data.getExtras().getString(getString(R.string.auth_token));
                        new FitBitAPIAsync().execute(auth_token);
                    }else{
                        // Hide progress bar
                        Snackbar.make(mView,getString(R.string.no_network),Snackbar.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mGoogleAPIClient != null && mGoogleAPIClient.isConnected())
            mGoogleAPIClient.disconnect();
    }

    private void buildGoogleFitnessClient() {
        mGoogleAPIClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Fitness.HISTORY_API)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        mFitnessEmail = Plus.AccountApi.getAccountName(mGoogleAPIClient);
                        new FitnessHistoryAsync().execute();
                        /*if (mDateFilterSpinner.getSelectedItemPosition() == 0) {
                            Log.i("Fitness Async", "called from onconnected callback");
                            if (!mIsGetFitnessDataAsyncRunning)
                                new FitnessDataAsync().execute(start_time, end_time);
                        } else {
                            mDateFilterSpinner.setSelection(0, true);
                        }*/
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                            Log.i("ConnectionSuspended", "Connection lost.  Cause: Network Lost.");
                        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                            Log.i("ConnectionSuspended", "Connection lost.  Reason: Service Disconnected");
                        }
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.i("ConnectionFailed", "GoogleApiClient connection failed: " + connectionResult.toString());
                        if (!connectionResult.hasResolution()) {
                            // show the localized error dialog.
                            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
                            return;
                        }
                        // The failure has a resolution. Resolve it.
                        // Called typically when the app is not yet authorized, and an
                        // authorization
                        // dialog is displayed to the user.
                        try {
                            connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("Exception", "Exception while starting resolution activity", e);
                        }
                    }
                })
                .build();
        mGoogleAPIClient.connect();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkGetAccountsPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.GET_ACCOUNTS);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i("Permission Result", "onRequestPermissionResult");
        switch (requestCode) {
            case REQUEST_BOTH_PERMISSION_CODE: {
                mIsPermissionRequestRaised = true;
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.GET_ACCOUNTS, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleFitnessClient();
                    mGoogleAPIClient.connect();
                } else {
                    Snackbar.make(
                            mView,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.settings, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Build intent that displays the App settings screen.
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .show();

                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

   private void checkAndBuildGoogleApiClient() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<String>();
            final List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.GET_ACCOUNTS))
                permissionsNeeded.add("Get Accounts");
            if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                permissionsNeeded.add("Location");
            if (permissionsList.size() > 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_BOTH_PERMISSION_CODE);
                return;
            }
            buildGoogleFitnessClient();
        } else {
            buildGoogleFitnessClient();
        }
    }

    private void checkAndBuildFitBitApiClient() {
        mOAuthSevice =  new ServiceBuilder()
                .provider(FitBitApi.class)
                .apiKey(getString(R.string.fit_bit_secret_key))
                .apiSecret(getString(R.string.fit_bit_api_key))
                .callback(getString(R.string.fit_bit_call_back_url))
                .scope("activity")
                .debug()
                .build();
        mFitBitHelper = new FitBitHelper((Activity) mContext,mOAuthSevice);

    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    public class FitnessHistoryAsync extends AsyncTask<Long, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Long... params) {
            if(mPreferenceManager.getIntValue(getString(R.string.selected_fitness_source)) == FitnessSource.GOOGLE_FIT.getId()){
                mFitnessHistoryList = new GoogleFitHelper(getActivity(), mGoogleAPIClient).getFitnessHistoryData();
            }else{
                mFitnessHistoryList = mFitBitHelper.getFitbitHistory();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            if(new NetworkCheck().ConnectivityCheck(mContext)){
                getBusinessList();
            }else{
                Snackbar.make(mView,getString(R.string.no_network),Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /*private DataReadRequest getFitnessData(long start_time, long end_time) {
        DataReadRequest mFitnessDataRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByActivityType(1, TimeUnit.MILLISECONDS)
                .setTimeRange(start_time, end_time, TimeUnit.MILLISECONDS)
                .build();
        return mFitnessDataRequest;
    }

    private ArrayList<FitnessHistory> getFitnessActivityDetails(DataReadResult dataReadResult, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.

        if (dataReadResult.getBuckets().size() > 0) {
            FitnessHistory history = new FitnessHistory();
            ArrayList<FitnessActivity> fitnessActivitiesListofDay = new ArrayList<>();
            *//*Log.e("Fitness data called: ", startDateTime.toString() + " " + dataReadResult.getBuckets().size());*//*
            double total_calories = 0;
            for (Bucket bucket : dataReadResult.getBuckets()) {
                *//*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
               Log.i("Days : ", bucket.getStartTime(TimeUnit.DAYS) + " " + bucket.getEndTime(TimeUnit.DAYS) + " " );
                Log.i("DAY ", "\tStart: " + dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i("DAY ", "\tEnd: " + dateFormat.format(bucket.getEndTime(TimeUnit.MILLISECONDS)));*//*
                *//*Log.e("Activity",bucket.getActivity());*//*
                if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.WALKING) || bucket.getActivity().equalsIgnoreCase(FitnessActivities.RUNNING) || bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    int step_count = 0;
                    double calories_expended = 0, distance = 0;
                    for (DataSet dataSet : dataSets) {
                        //dumpDataSet(dataSet);
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            //Log.d("TYPE", "\tType: " + dp.getDataType().getName());
                            for (Field field : dp.getDataType().getFields()) {
                               *//* Log.i("Fields", "\tField: " + field.getName() +
                                        " Value: " + dp.getValue(field));*//*
                                if (field.getName().equalsIgnoreCase("steps") && !bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                                    step_count = dp.getValue(field).asInt();
                                } else if (field.getName().equalsIgnoreCase("calories")) {
                                    calories_expended = dp.getValue(field).asFloat();
                                    total_calories += calories_expended;
                                } else if (field.getName().equalsIgnoreCase("distance")) {
                                    distance = dp.getValue(field).asFloat();
                                }
                            }
                        }
                    }
                    FitnessActivity activity = new FitnessActivity();
                    activity.setName(bucket.getActivity());
                    activity.setCalories_expended(calories_expended);
                    activity.setDistance(distance / 1000);
                    activity.setStep_count(step_count);
                    fitnessActivitiesListofDay.add(activity);
                }
            }
            history.setStartDateTime(startDateTime);
            history.setEndDateTime(endDateTime);
            history.setTotalCaloriesBurnt(total_calories);
            history.setFitnessActivitiesList(fitnessActivitiesListofDay);
            mFitnessHistoryList.add(history);
        }
        return mFitnessHistoryList;
        // [END parse_read_data_result]
    }*/

    private void getBusinessList() {
        mBusinesOfferList = new ArrayList<>();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.BUSINESS_OFFER_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1) {
                            int totalPointEarned = responseJson.optInt("yourpoint");
                            mPreferenceManager.setIntValue(getString(R.string.your_total_points),totalPointEarned);
                            //mPointsEarnedText.setText(responseJson.optInt("yourpoint") + "!");
                            if (!responseJson.optString("lastcaloriesupdate").equalsIgnoreCase("") && !responseJson.optString("lastcaloriesupdate").startsWith("0000") ) {
                                LocalDateTime lastUpdateddatetime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseLocalDateTime(responseJson.optString("lastcaloriesupdate"));
                                mPreferenceManager.setLongValue(getString(R.string.last_updated_calories_time), lastUpdateddatetime.toDateTime().getMillis());
                            } else
                                mPreferenceManager.setLongValue(getString(R.string.last_updated_calories_time), -1);
                            JSONArray business_list = responseJson.optJSONArray("businesslist");
                            if (business_list != null && business_list.length() > 0) {
                                for (int i = 0; i < business_list.length(); i++) {
                                    JSONObject business_object = business_list.optJSONObject(i);
                                    BusinessDetails businessDetails = new BusinessDetails();
                                    businessDetails.setId(Integer.valueOf(business_object.optString("Businessid")));
                                    businessDetails.setName(business_object.optString("Business Name"));
                                    businessDetails.setOffer_name(business_object.optString("offerName"));
                                    businessDetails.setLogo(business_object.optString("Business Image"));
                                    businessDetails.setPromo(business_object.optString("offerText"));
                                    businessDetails.setPoints_needed(business_object.optInt("requiredPoints"));
                                    businessDetails.setCoupon_expiry_date(business_object.optString("endingDate"));
                                    businessDetails.setHow_to_reedem(business_object.optString("How to Redeem"));
                                    businessDetails.setUrl(business_object.optString("site"));
                                    businessDetails.setPhone_number(business_object.optInt("Phone No"));
                                    businessDetails.setAddress(business_object.optString("Address"));
                                    businessDetails.setTerms_and_conditions(business_object.optString("termsandconditions"));
                                    businessDetails.setCoupon(business_object.optString("couponCode"));
                                    businessDetails.setOfferLogo(business_object.optString("offerimage"));
                                    mBusinesOfferList.add(businessDetails);
                                }
                                mLoadingProgressBar.setVisibility(View.GONE);
                                mNoOfferText.setVisibility(View.GONE);
                                mBusinessOfferRecyclerView.setVisibility(View.VISIBLE);
                                mBusinessOfferRecyclerView.setAdapter(new OfferRewardsAdapter(mContext,mBusinesOfferList,"offer_earned"));
                            } else {
                                mLoadingProgressBar.setVisibility(View.GONE);
                                mNoOfferText.setVisibility(View.VISIBLE);
                                mBusinessOfferRecyclerView.setVisibility(View.GONE);
                                if (!isDetached())
                                    Toast.makeText(mContext, "Burn More Calories To Avail Offers", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mLoadingProgressBar.setVisibility(View.GONE);
                            mNoOfferText.setVisibility(View.VISIBLE);
                            mBusinessOfferRecyclerView.setVisibility(View.GONE);
                            if (!isDetached())
                                Toast.makeText(mContext, responseJson.optString("msg"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        mLoadingProgressBar.setVisibility(View.GONE);
                        mNoOfferText.setVisibility(View.VISIBLE);
                        mBusinessOfferRecyclerView.setVisibility(View.GONE);
                        if (!isDetached())
                            Toast.makeText(mContext, "Burn More Calories To Avail Offers", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (!isDetached()) {
                        Toast.makeText(getActivity(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                String json_request = "";
                try {
                    JSONObject root = new JSONObject();
                    root.put("user_id", mPreferenceManager.getStringValue(getString(R.string.user_id)));
                    root.put("flag", "android");
                    root.put("fitness_email_id", mFitnessEmail);
                    JSONArray dates_array = new JSONArray();
                    for (FitnessHistory history : mFitnessHistoryList) {
                        JSONObject fitnessSummaryObj = new JSONObject();
                        fitnessSummaryObj.put("start_datetime", history.getStartDateTime());
                        fitnessSummaryObj.put("end_datetime", history.getEndDateTime());
                        JSONArray activities_array = new JSONArray();
                        for (FitnessActivity activity : history.getFitnessActivitiesList()) {
                            JSONObject activityObj = new JSONObject();
                            activityObj.put("name", activity.getName());
                            activityObj.put("calories_burnt", activity.getCalories_expended() == -1 ? 0 : activity.getCalories_expended());
                            activityObj.put("step_count", activity.getStep_count() == -1 ? 0 : activity.getStep_count());
                            activityObj.put("distance", activity.getDistance() == -1 ? 0 : activity.getDistance());
                            activities_array.put(activityObj);
                        }
                        fitnessSummaryObj.put("activities", activities_array);
                        dates_array.put(fitnessSummaryObj);
                    }
                    root.put("date", dates_array);
                    json_request = root.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("json request", json_request);
                params.put("jsonrequest", json_request);
                params.put("fitness_source",String.valueOf(mPreferenceManager.getIntValue(getString(R.string.selected_fitness_source)))); // 1 - google Fit , 2 - Fitbit
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }

    class FitBitAPIAsync extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                Verifier verifier = new Verifier(params[0]);
                Token accessToken = mOAuthSevice.getAccessToken(null,verifier);
                if(accessToken != null){
                    mPreferenceManager.setBooleanValue(getString(R.string.is_fitbit_authenticated),true);
                    mPreferenceManager.setStringValue(getString(R.string.access_token),accessToken.getToken());
                    JSONObject accessTokenObject = new JSONObject(accessToken.getRawResponse());
                    mPreferenceManager.setStringValue(getString(R.string.refresh_token),accessTokenObject.optString("refresh_token"));
                    Log.i("access_token"," Response :" + accessToken.getRawResponse());
                    return "success";
                }else
                    return "failure";

            }catch (Exception e){
                e.printStackTrace();
                return "failure";
            }
        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);
            if(status.equalsIgnoreCase("success")){
                new FitnessHistoryAsync().execute();
            }else{
                // Failure
                // Hide Progress Bar
            }
        }
    }
}
