package com.pluggdd.burnandearn.view.fragment;

import android.Manifest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.android.gms.plus.Plus;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.pluggdd.burnandearn.BuildConfig;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.FitBitActivity;
import com.pluggdd.burnandearn.activity.OfferDetailActivity;
import com.pluggdd.burnandearn.activity.ShareActivity;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.model.FitnessHistory;
import com.pluggdd.burnandearn.utils.FitBitApi;
import com.pluggdd.burnandearn.utils.FitBitHelper;
import com.pluggdd.burnandearn.utils.FitnessSource;
import com.pluggdd.burnandearn.utils.GoogleFitHelper;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;

import org.joda.time.Days;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Fragment to show activity details and offers
 */
public class PointsFragment extends Fragment implements View.OnClickListener {
    // Initialization of views and variables
    private final int REQUEST_CODE_RESOLUTION = 100, REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 101, REQUEST_GETACCOUNTS_PERMISSIONS_REQUEST_CODE = 102, REQUEST_BOTH_PERMISSION_CODE = 103, REQUEST_FITBIT_API = 104;
    private final static String PAGE_FLAG = "PAGE_FLAG";
    private View mView;
    private DecoView mCircularProgressDecoView;
    private ViewPager mActivitiesViewPager;
    private RelativeLayout mFitnessActivityViewPagerContainer, mAllActivitiesImageContainer, mWalkingImageContainer, mRunningImageContainer, mBikingImageContainer;
    private LinearLayout mFitnessActivityImageContainer;
    private ImageView mViewPagerIndicator1Image, mViewPagerIndicator2Image, mViewPagerIndicator3Image, mViewPagerIndicator4Image;
    private ProgressBar mActivitiesProgressBar;
    private BarChart mBarChart;
    private Context mContext;
    private GoogleApiClient mGoogleAPIClient;
    private OAuthService mOAuthSevice;
    private int mTotalStepCount = 0, mStepsAverage = 0, mPointsAverage = 0;
    private double mTotalCaloriesExpended = 0, mCaloriesAverage = 0, mTotalDistanceTravelled = 0, mDistanceAverage = 0;
    private int mBackIndex, mWalkingActivityIndex, mRunningActivityIndex, mBikingActivityIndex, mTotalPointEarned;
    //private Spinner mDateFilterSpinner;
    private boolean mIsPermissionRequestRaised, mIsGetFitnessDataAsyncRunning;
    private PreferencesManager mPreferenceManager;
    private String mFitnessEmail = "", mCurrentFitnessActivity, mPageFlag;
    private CustomPagerAdapter mActivitiesPagerAdapter;
    private GoogleFitHelper mGoogleFitHelper;
    private FitBitHelper mFitBitHelper;
    private ArrayList<FitnessHistory> mFitnessHistoryList = new ArrayList<>();
    private ArrayList<FitnessHistory> mWeekFitnessHistoryList = new ArrayList<>();
    private ArrayList<Integer> mWeeklyPointsList = new ArrayList<>();
    private AlertDialog mGoalSetUpDialog;
    private float mDecoviewWidth;

    public PointsFragment() {
        // Required empty public constructor
    }

    public static PointsFragment newInstance(String page_flag) {
        PointsFragment fragment = new PointsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PAGE_FLAG, page_flag);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mPageFlag = getArguments().getString(PAGE_FLAG, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_burnt, container, false);
        mCircularProgressDecoView = (DecoView) mView.findViewById(R.id.deco_view);
        mFitnessActivityViewPagerContainer = (RelativeLayout) mView.findViewById(R.id.activities_view_pager_container);
        mActivitiesViewPager = (ViewPager) mView.findViewById(R.id.activities_view_pager);
        mViewPagerIndicator1Image = (ImageView) mView.findViewById(R.id.viewpager_indicator1_image);
        mViewPagerIndicator2Image = (ImageView) mView.findViewById(R.id.viewpager_indicator2_image);
        mViewPagerIndicator3Image = (ImageView) mView.findViewById(R.id.viewpager_indicator3_image);
        mViewPagerIndicator4Image = (ImageView) mView.findViewById(R.id.viewpager_indicator4_image);
        mActivitiesProgressBar = (ProgressBar) mView.findViewById(R.id.activities_progress_bar);
        mFitnessActivityImageContainer = (LinearLayout) mView.findViewById(R.id.activities_fitness_container);
        mAllActivitiesImageContainer = (RelativeLayout) mView.findViewById(R.id.all_activities_image_container);
        mWalkingImageContainer = (RelativeLayout) mView.findViewById(R.id.walking_image_container);
        mRunningImageContainer = (RelativeLayout) mView.findViewById(R.id.running_image_container);
        mBikingImageContainer = (RelativeLayout) mView.findViewById(R.id.cycling_image_container);
        mBarChart = (BarChart) mView.findViewById(R.id.bar_chart);
        mPreferenceManager = new PreferencesManager(mContext);
        mActivitiesViewPager.setOffscreenPageLimit(1);
        if (mPageFlag.equalsIgnoreCase("notification"))
            showGoalSetUpDialog();
        else
            // Check and set selected fitness source ( Google Fit or FitBit)
            setFitnessSource();
        initializeActivitiesBarChart();
        // Initial set up for circular decoview
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.decoview_width,typedValue,true);
        mDecoviewWidth = typedValue.getFloat();
        createBackgroundSeries();
        createBikingActivitySeries();
        createRunningActivitySeries();
        createWalkingActivitySeries();
        // Adding click listener to activity image container
        mAllActivitiesImageContainer.setOnClickListener(this);
        mWalkingImageContainer.setOnClickListener(this);
        mRunningImageContainer.setOnClickListener(this);
        mBikingImageContainer.setOnClickListener(this);

        mActivitiesViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        resetActivitiesViewPager();
                        break;
                    case 1:
                        plotActivityChart(mCurrentFitnessActivity, "distance");
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator4Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        updateFitnessActivities(position);
                        break;
                    case 2:
                        plotActivityChart(mCurrentFitnessActivity, "steps");
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        mViewPagerIndicator4Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        updateFitnessActivities(position);
                        break;
                    case 3:
                        plotActivityChart(mCurrentFitnessActivity, "points");
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator4Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        updateFitnessActivities(position);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return mView;
    }

    private void setFitnessSource() { // Flag - InitialSetUp , Flag - GoalSetUp
        int selected_fitness_source = mPreferenceManager.getIntValue(mContext.getString(R.string.selected_fitness_source));
        switch (selected_fitness_source) {
            case 1: // Google Fit
                checkAndBuildGoogleApiClient();
                break;
            case 2: // Fit Bit

                if (new NetworkCheck().ConnectivityCheck(mContext)) {
                    if (mPreferenceManager.getBooleanValue(mContext.getString(R.string.is_fitbit_authenticated))) {
                        checkAndBuildFitBitApiClient();
                        if (new NetworkCheck().ConnectivityCheck(mContext)) {
                            new FitnessDataAsync().execute();
                        } else {
                            // Hide progress bar
                            mActivitiesProgressBar.setVisibility(View.GONE);
                            if (!isDetached())
                                Toast.makeText(mContext,mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        startActivityForResult(new Intent(mContext, FitBitActivity.class), REQUEST_FITBIT_API);
                    }
                } else {
                    // Hide progress bar
                    mActivitiesProgressBar.setVisibility(View.GONE);
                    if (!isDetached())
                        Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void showGoalSetUpDialog() {
        final AlertDialog.Builder mGoalDialog = new AlertDialog.Builder(mContext);
        View mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_set_goal, null);
        mGoalDialog.setView(mView);
        mGoalDialog.setCancelable(false);
        TextView mUserGoalHeaderText = (TextView) mView.findViewById(R.id.txt_user_goal_header);
        mPreferenceManager = new PreferencesManager(mContext);
        int goal = mPreferenceManager.getIntValue(mContext.getString(R.string.user_goal_detected));
        mUserGoalHeaderText.setText("We recommend " + mPreferenceManager.getIntValue(mContext.getString(R.string.user_goal_detected)) + " calories based on your inputs for maintaining a healthy lifestyle, but you can choose your goal too. Input your goal if you differ from ours");
        final EditText mGoalEdt = (EditText) mView.findViewById(R.id.edt_goal);
        ImageView mProfileImage = (ImageView) mView.findViewById(R.id.img_profile);
        ProgressBar mProfileLoadingProgress = (ProgressBar) mView.findViewById(R.id.loading_progress_bar);
        Button mSetButton = (Button) mView.findViewById(R.id.btn_set);
        Button mCancelButton = (Button) mView.findViewById(R.id.btn_cancel);
        mGoalEdt.setText(String.valueOf(goal));
        new PicassoImageLoaderHelper(getContext(), mProfileImage, mProfileLoadingProgress).loadImage(mPreferenceManager.getStringValue(getString(R.string.profile_image_url)));
        mGoalSetUpDialog = mGoalDialog.show();
        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mGoalEdt.getText())) {
                    mGoalEdt.setError(getString(R.string.enter_goal));
                } else {
                    mGoalSetUpDialog.dismiss();
                    updateUserGoal(Integer.valueOf(mGoalEdt.getText().toString()));
                    mGoalSetUpDialog = null;
                }

            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoalSetUpDialog.dismiss();
                setFitnessSource();
                mGoalSetUpDialog = null;
            }
        });
    }

    private void initializeActivitiesBarChart() {
        mBarChart.setDescription("");    // Hide the description
        mBarChart.getAxisLeft().setDrawLabels(true);
        mBarChart.getAxisLeft().setTextColor(Color.WHITE);
        mBarChart.getAxisRight().setDrawLabels(true);
        mBarChart.getAxisRight().setTextColor(Color.WHITE);
        mBarChart.getXAxis().setDrawLabels(true);
        mBarChart.getLegend().setEnabled(true);
        mBarChart.setDrawGridBackground(false);
        mBarChart.getXAxis().setTextColor(Color.WHITE);
        mBarChart.getLegend().setTextColor(Color.WHITE);
        mBarChart.getLegend().setCustom(getColors(), new String[]{"Walking", "Running", "Biking"});

    }

    // To reset viewpager to initial position
    private void resetActivitiesViewPager() {
        plotActivityChart(mCurrentFitnessActivity, "calories");
        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_selected);
        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_unselected);
        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_unselected);
        mViewPagerIndicator4Image.setImageResource(R.drawable.viewpager_indicator_unselected);
        updateFitnessActivities(0);
    }

    // Initial grey circle series
    private void createBackgroundSeries() {
        TypedValue typedValue = new TypedValue();
        SeriesItem initial_grey_circle_progress = new SeriesItem.Builder(Color.parseColor("#a9a9a9"))
                .setRange(0, 100, 0)
                .setLineWidth(mDecoviewWidth)
                .build();
        mBackIndex = mCircularProgressDecoView.addSeries(initial_grey_circle_progress);
        mCircularProgressDecoView.addEvent(new DecoEvent.Builder(100).setIndex(mBackIndex).build());
    }

    // Walking activity series
    private void createWalkingActivitySeries() {
        SeriesItem walking_activity_series = new SeriesItem.Builder(Color.parseColor("#53B6CD"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .setLineWidth(mDecoviewWidth)
                .build();
        mWalkingActivityIndex = mCircularProgressDecoView.addSeries(walking_activity_series);
    }

    // Running activity series
    private void createRunningActivitySeries() {
        SeriesItem running_activity_series = new SeriesItem.Builder(Color.parseColor("#F8C900"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .setLineWidth(mDecoviewWidth)
                .build();
        mRunningActivityIndex = mCircularProgressDecoView.addSeries(running_activity_series);
    }

    // Biking activity series
    private void createBikingActivitySeries() {
        SeriesItem biking_activity_series = new SeriesItem.Builder(Color.parseColor("#B0C53C"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .setLineWidth(mDecoviewWidth)
                .build();
        mBikingActivityIndex = mCircularProgressDecoView.addSeries(biking_activity_series);
    }

    private void updateFitnessActivities(int position) {
        if (mWeekFitnessHistoryList.size() > 0) {
            ArrayList<FitnessActivity> mTodayFitnessActivityList = mWeekFitnessHistoryList.get(mWeekFitnessHistoryList.size() - 1).getFitnessActivitiesList();
            switch (position) {
                case 0: // Calories
                    Log.i("calories average", mCaloriesAverage + "");
                    double walking_calories_percentage = 0, running_calories_percentage = 0, biking_calories_percentage = 0;
                    double walking_calories_expended = 0;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.WALKING)) {
                            walking_calories_expended = fitnessActivity.getCalories_expended();
                            break;
                        }
                    }
                    //walking_calories_expended = 100;
                    Log.i("walking calories", walking_calories_expended + "");
                    walking_calories_percentage = (mCaloriesAverage == 0) ? walking_calories_expended : (walking_calories_expended / mCaloriesAverage) * 100;
                    // To calculate running calories percentage in total calories
                    double running_calories_expended = 0;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.RUNNING)) {
                            running_calories_expended = fitnessActivity.getCalories_expended();
                            break;
                        }
                    }
                    //running_calories_expended = 60;
                    Log.i("running calories", running_calories_expended + "");
                    running_calories_percentage = (mCaloriesAverage == 0) ? running_calories_expended : (running_calories_expended / mCaloriesAverage) * 100;
                    // To calculate biking calories percentage in total calories
                    double biking_calories_expended = 0;
                    //biking_calories_expended = 200;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.BIKING)) {
                            biking_calories_expended = fitnessActivity.getCalories_expended();
                            break;
                        }
                    }
                    //biking_calories_expended = 10;
                    Log.i("biking calories", biking_calories_expended + "");
                    biking_calories_percentage = (mCaloriesAverage == 0) ? biking_calories_expended : (biking_calories_expended / mCaloriesAverage) * 100;
                    // To update decoview
                    //walking_calories_percentage = 30 ; running_calories_percentage = 0; biking_calories_percentage = 10;
                    updateProgressBar(walking_calories_percentage, running_calories_percentage, biking_calories_percentage);
                    /*walking_calories_expended = 350;
                    double totalCaloriesExpended = walking_calories_expended + running_calories_expended + biking_calories_expended;
                    int calories_goal = mPreferenceManager.getIntValue(mContext.getString(R.string.user_goal));
                    if (totalCaloriesExpended >= calories_goal) {
                        showNotification("goal_complete");
                    } else if (totalCaloriesExpended >= (calories_goal/2)){
                        showNotification("half_way");
                    }*/
                    break;
                case 1: // Distance
                    Log.i("distance average", mDistanceAverage + "");
                    double walking_distance_percentage = 0, running_distance_percentage = 0, biking_distance_percentage = 0;
                    double walking_distance_travelled = 0;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.WALKING)) {
                            walking_distance_travelled = fitnessActivity.getDistance();
                            break;
                        }
                    }
                    //walking_calories_expended = 100;
                    Log.i("walking distance", walking_distance_travelled + "");
                    walking_distance_percentage = (mDistanceAverage == 0) ? walking_distance_travelled : (walking_distance_travelled / mDistanceAverage) * 100;
                    //walking_distance_percentage = ;
                    // To calculate running calories percentage in total calories
                    double running_distance_travelled = 0;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.RUNNING)) {
                            running_distance_travelled = fitnessActivity.getDistance();
                            break;
                        }
                    }
                    //running_calories_expended = 60;
                    Log.i("running distance", running_distance_travelled + "");
                    running_distance_percentage = (mDistanceAverage == 0) ? running_distance_travelled : (running_distance_travelled / mDistanceAverage) * 100;
                    // To calculate biking calories percentage in total calories
                    double biking_distance_travelled = 0;
                    //biking_calories_expended = 200;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.BIKING)) {
                            biking_distance_travelled = fitnessActivity.getDistance();
                            break;
                        }
                    }
                    //biking_calories_expended = 10;
                    Log.i("biking distance", biking_distance_travelled + "");
                    biking_distance_percentage = (mDistanceAverage == 0) ? biking_distance_travelled : (biking_distance_travelled / mDistanceAverage) * 100;
                    // To update decoview
                    //walking_distance_percentage = 0 ; running_distance_percentage = 20; biking_distance_percentage = 10;
                    updateProgressBar(walking_distance_percentage, running_distance_percentage, biking_distance_percentage);
                    break;
                case 2: // Steps
                    Log.i("steps average", mStepsAverage + "");
                    double walking_steps_percentage = 0, running_steps_percentage = 0, biking_steps_percentage = 0;
                    double walking_steps_taken = 0;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.WALKING)) {
                            walking_steps_taken = fitnessActivity.getStep_count();
                            break;
                        }
                    }
                    //walking_calories_expended = 100;
                    Log.i("walking distance", walking_steps_taken + "");
                    walking_steps_percentage = (mStepsAverage == 0) ? walking_steps_taken : (walking_steps_taken / mStepsAverage) * 100;
                    // To calculate running calories percentage in total calories
                    double running_steps_taken = 0;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.RUNNING)) {
                            running_steps_taken = fitnessActivity.getStep_count();
                            break;
                        }
                    }
                    //running_calories_expended = 60;
                    Log.i("running distance", running_steps_taken + "");
                    running_steps_percentage = (mStepsAverage == 0) ? running_steps_taken : (running_steps_taken / mStepsAverage) * 100;
                    // To calculate biking calories percentage in total calories
                    double biking_steps_taken = 0;
                    //biking_calories_expended = 200;
                    for (FitnessActivity fitnessActivity : mTodayFitnessActivityList) {
                        if (fitnessActivity.getName().equalsIgnoreCase(FitnessActivities.BIKING)) {
                            biking_steps_taken = fitnessActivity.getStep_count();
                            break;
                        }
                    }
                    //biking_calories_expended = 10;
                    Log.i("biking distance", biking_steps_taken + "");
                    biking_steps_percentage = (mStepsAverage == 0) ? biking_steps_taken : (biking_steps_taken / mStepsAverage) * 100;
                    //walking_steps_percentage = 10 ; running_steps_percentage = 80; biking_steps_percentage = 0;
                    // To update decoview
                    updateProgressBar(walking_steps_percentage, running_steps_percentage, biking_steps_percentage);
                    break;
                case 3: // Point
                    Log.i("steps average", mPointsAverage + "");
                    double today_points_percentage = 0;
                    double today_points = 0;
                    if (mWeeklyPointsList != null && mWeeklyPointsList.size() > 0) {
                        today_points = mWeeklyPointsList.get(mWeeklyPointsList.size() - 1);
                    }
                    //walking_calories_expended = 100;
                    Log.i("today points", today_points + "");
                    today_points_percentage = (mPointsAverage == 0) ? today_points : (today_points / mPointsAverage) * 100;
                    // To update decoview
                    //today_points_percentage = 20 ;
                    updateProgressBar(today_points_percentage, 0, 0);
                    break;
                default:
                    break;
            }
        }
    }

    private float calculateDistanceAverage() {
        float total_distance_of_week = 0;
        for (int i = 0; i < mWeekFitnessHistoryList.size() - 1; i++) {
            FitnessHistory fitnessHistory = mWeekFitnessHistoryList.get(i);
            total_distance_of_week += (fitnessHistory.getWalkingDistance() + fitnessHistory.getRunningDistance() + fitnessHistory.getCyclingDistance()) / 1000;
        }
        return total_distance_of_week / 7;
    }

    private int calculateStepAverage() {
        int total_steps_of_week = 0;
        for (int i = 0; i < mWeekFitnessHistoryList.size() - 1; i++) {
            FitnessHistory fitnessHistory = mWeekFitnessHistoryList.get(i);
            total_steps_of_week += fitnessHistory.getWalkingSteps() + fitnessHistory.getRunningSteps();
        }
        return total_steps_of_week / 7;
    }

    private void updateProgressBar(double walking_percentage, double running_percentage, double biking_percentage) {
        Log.i("activity percentage", walking_percentage + " " + running_percentage + " " + biking_percentage);
        if (biking_percentage > 0) {
            mCircularProgressDecoView.addEvent(new DecoEvent.Builder((float) (walking_percentage + running_percentage + biking_percentage)).setDuration(2000).setIndex(mBikingActivityIndex).build());
            //mCircularProgressDecoView.moveTo(mBikingActivityIndex, (float) (walking_percentage + running_percentage + biking_percentage));
        } else {
            mCircularProgressDecoView.addEvent(new DecoEvent.Builder(0).setDuration(2000).setIndex(mBikingActivityIndex).setListener(new DecoEvent.ExecuteEventListener() {
                @Override
                public void onEventStart(DecoEvent decoEvent) {

                }

                @Override
                public void onEventEnd(DecoEvent decoEvent) {
                    mCircularProgressDecoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_HIDE, false).setDuration(1000).setIndex(mBikingActivityIndex).build());
                }
            }).build());
        }
        if (running_percentage > 0) {
            //mCircularProgressDecoView.moveTo(mRunningActivityIndex, (float) (running_percentage + walking_percentage));
            mCircularProgressDecoView.addEvent(new DecoEvent.Builder((float) (walking_percentage + running_percentage)).setDuration(2000).setIndex(mRunningActivityIndex).build());
        } else {
            mCircularProgressDecoView.addEvent(new DecoEvent.Builder(0).setIndex(mRunningActivityIndex).setDuration(2000).setListener(new DecoEvent.ExecuteEventListener() {
                @Override
                public void onEventStart(DecoEvent decoEvent) {

                }

                @Override
                public void onEventEnd(DecoEvent decoEvent) {
                    mCircularProgressDecoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_HIDE, false).setDuration(1000).setIndex(mRunningActivityIndex).build());
                }
            }).build());

            //mCircularProgressDecoView.moveTo(mRunningActivityIndex, 0);
        }
        if (walking_percentage > 0) {
            mCircularProgressDecoView.addEvent(new DecoEvent.Builder((float) (walking_percentage)).setDuration(2000).setIndex(mWalkingActivityIndex).build());
            //mCircularProgressDecoView.moveTo(mWalkingActivityIndex, (float) walking_percentage);
        } else {
            //mCircularProgressDecoView.addEvent(new DecoEvent.Builder(0).setIndex(mWalkingActivityIndex).setDuration(2000).build());
            mCircularProgressDecoView.addEvent(new DecoEvent.Builder(0).setDuration(2000).setIndex(mWalkingActivityIndex).setListener(new DecoEvent.ExecuteEventListener() {
                @Override
                public void onEventStart(DecoEvent decoEvent) {

                }

                @Override
                public void onEventEnd(DecoEvent decoEvent) {
                    mCircularProgressDecoView.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_HIDE, false).setDuration(1000).setIndex(mWalkingActivityIndex).build());
                }
            }).build());

            //mCircularProgressDecoView.moveTo(mWalkingActivityIndex, 0);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_RESOLUTION:
                    mGoogleAPIClient.connect();
                    break;
                case REQUEST_FITBIT_API:
                    if (new NetworkCheck().ConnectivityCheck(mContext)) {
                        checkAndBuildFitBitApiClient();
                        String auth_token = data.getExtras().getString(mContext.getString(R.string.auth_token));
                        new FitBitAPIAsync().execute(auth_token);
                    } else {
                        // Hide progress bar
                        mActivitiesProgressBar.setVisibility(View.GONE);
                        if (!isDetached())
                            Snackbar.make(mView, mContext.getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
                    }
                    break;
            }
        } else if (resultCode == getActivity().RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_CODE_RESOLUTION:
                    Snackbar.make(mView, mContext.getString(R.string.google_fit_issue), Snackbar.LENGTH_SHORT).show();
                    break;
                case REQUEST_FITBIT_API:
                    Snackbar.make(mView, mContext.getString(R.string.fit_bit_issue), Snackbar.LENGTH_SHORT).show();
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
    public void onResume() {
        super.onResume();
        if (mGoalSetUpDialog == null) {
            if(mPreferenceManager.getIntValue(mContext.getString(R.string.selected_fitness_source)) == FitnessSource.GOOGLE_FIT.getId()){
                if (mGoogleAPIClient == null ) {
                    if (checkLocationPermissions() && checkGetAccountsPermissions()) {
                        buildGoogleFitnessClient();
                    } else if (mIsPermissionRequestRaised) {
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
                if (mGoogleAPIClient != null && !mGoogleAPIClient.isConnected())
                    mGoogleAPIClient.connect();
            }else{ // FITBIT
                if(!mIsGetFitnessDataAsyncRunning){
                    if (new NetworkCheck().ConnectivityCheck(mContext)) {
                        new FitnessDataAsync().execute();
                    } else {
                        // Hide progress bar
                        mActivitiesProgressBar.setVisibility(View.GONE);
                        if (!isDetached())
                            Toast.makeText(mContext,mContext.getString(R.string.no_network),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleAPIClient != null && mGoogleAPIClient.isConnected())
            mGoogleAPIClient.disconnect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.all_activities_image_container:
                initialActivitiesSetUp();
                break;
            case R.id.walking_image_container:
                mCurrentFitnessActivity = FitnessActivities.WALKING;
                plotActivityChartByActivity();
                mWalkingImageContainer.setBackgroundResource(R.drawable.activities_border);
                mAllActivitiesImageContainer.setBackgroundResource(0);
                mRunningImageContainer.setBackgroundResource(0);
                mBikingImageContainer.setBackgroundResource(0);
                break;
            case R.id.running_image_container:
                mCurrentFitnessActivity = FitnessActivities.RUNNING;
                plotActivityChartByActivity();
                mRunningImageContainer.setBackgroundResource(R.drawable.activities_border);
                mAllActivitiesImageContainer.setBackgroundResource(0);
                mWalkingImageContainer.setBackgroundResource(0);
                mBikingImageContainer.setBackgroundResource(0);
                break;
            case R.id.cycling_image_container:
                mCurrentFitnessActivity = FitnessActivities.BIKING;
                plotActivityChartByActivity();
                mBikingImageContainer.setBackgroundResource(R.drawable.activities_border);
                mWalkingImageContainer.setBackgroundResource(0);
                mRunningImageContainer.setBackgroundResource(0);
                mAllActivitiesImageContainer.setBackgroundResource(0);
                break;
        }
    }

    private void initialActivitiesSetUp() {
        mCurrentFitnessActivity = "all";
        mAllActivitiesImageContainer.setBackgroundResource(R.drawable.activities_border);
        mWalkingImageContainer.setBackgroundResource(0);
        mRunningImageContainer.setBackgroundResource(0);
        mBikingImageContainer.setBackgroundResource(0);
        plotActivityChartByActivity();
    }

    private void plotActivityChartByActivity() {
        switch (mActivitiesViewPager.getCurrentItem()) {
            case 0:
                plotActivityChart(mCurrentFitnessActivity, "calories");
                break;
            case 1:
                plotActivityChart(mCurrentFitnessActivity, "distance");
                break;
            case 2:
                plotActivityChart(mCurrentFitnessActivity, "steps");
                break;
            case 3:
                plotActivityChart(mCurrentFitnessActivity, "points");
                break;

        }
    }

    /**
     * Custom activity detail adapter for viewpager
     */
    private class CustomPagerAdapter extends FragmentStatePagerAdapter {

        public CustomPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TotalCaloriesBurnedFragment.newInstance(mTotalCaloriesExpended, (int) mCaloriesAverage);
                case 1:
                    return TotalDistanceTravelledFragment.newInstance(mTotalDistanceTravelled, mDistanceAverage);
                case 2:
                    return TotalStepsCountFragment.newInstance(mTotalStepCount, mStepsAverage);
                case 3:
                    return TotalPointsEarnedFragment.newInstance(mTotalPointEarned, mPointsAverage);

            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    // To build Google api client
    private void buildGoogleFitnessClient() {
        mGoogleAPIClient = new GoogleApiClient.Builder(mContext)
                .addApi(Fitness.HISTORY_API)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        mFitnessEmail = Plus.AccountApi.getAccountName(mGoogleAPIClient);
                        mGoogleFitHelper = new GoogleFitHelper(mContext, mGoogleAPIClient);
                        if (!mIsGetFitnessDataAsyncRunning) {
                            if (new NetworkCheck().ConnectivityCheck(mContext)) {
                                new FitnessDataAsync().execute();
                            } else {
                                // Hide progress bar
                                mActivitiesProgressBar.setVisibility(View.GONE);
                                if (!isDetached())
                                    Snackbar.make(mView, mContext.getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
                            }
                        }

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


    public class FitnessDataAsync extends AsyncTask<Long, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsGetFitnessDataAsyncRunning = true;
            mTotalCaloriesExpended = 0;
            mTotalDistanceTravelled = 0;
            mTotalStepCount = 0;
            mActivitiesViewPager.setAdapter(new CustomPagerAdapter(getChildFragmentManager()));
            mActivitiesViewPager.setVisibility(View.GONE);
            mActivitiesProgressBar.setVisibility(View.VISIBLE);
            mCircularProgressDecoView.setVisibility(View.GONE);
            mFitnessActivityViewPagerContainer.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Long... params) {
            try {
                if (mPreferenceManager.getIntValue(mContext.getString(R.string.selected_fitness_source)) == FitnessSource.GOOGLE_FIT.getId()) {
                    mWeekFitnessHistoryList = mGoogleFitHelper.getLastWeekData();
                } else {
                    //mWeekFitnessHistoryList = mFitBitHelper.getLastWeekData();
                    mWeekFitnessHistoryList = mFitBitHelper.getLastWeekFitbitData();
                }
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                return "failure";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equalsIgnoreCase("success")) {
                if (new NetworkCheck().ConnectivityCheck(mContext)) {
                    new BusinessListAsync().execute();
                } else {
                    try {
                        mActivitiesProgressBar.setVisibility(View.GONE);
                        if (!isDetached())
                            Snackbar.make(mView, mContext.getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            } else {
                mActivitiesProgressBar.setVisibility(View.GONE);
            }

            //new FitnessHistoryAsync().execute();
        }
    }

    public class BusinessListAsync extends AsyncTask<Long, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Long... params) {
            try {
                if (mPreferenceManager.getIntValue(mContext.getString(R.string.selected_fitness_source)) == FitnessSource.GOOGLE_FIT.getId()) {
                    mFitnessHistoryList = mGoogleFitHelper.getFitnessHistoryData();
                } else {
                    mFitnessHistoryList = mFitBitHelper.getFitbitHistory();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            if (new NetworkCheck().ConnectivityCheck(mContext)) {
                if (mFitnessHistoryList != null && mFitnessHistoryList.size() > 0) {
                    getPointsList();
                } else {
                    try {
                        updateUI();
                        if (mPreferenceManager.getIntValue(mContext.getString(R.string.selected_fitness_source)) == FitnessSource.GOOGLE_FIT.getId()) {
                            if (!isDetached())
                                Snackbar.make(mView, mContext.getString(R.string.no_google_fit_data), Snackbar.LENGTH_SHORT).show();
                        } else {
                            if (!isDetached())
                                Snackbar.make(mView, mContext.getString(R.string.no_fitbit_data), Snackbar.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                           e.printStackTrace();
                    }

                }
            } else {
                try {
                    updateUI();
                    if (!isDetached())
                        Snackbar.make(mView, mContext.getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }
    }


    private void plotActivityChart(String activity, String type) {
        BarData barData;
        if (type.equalsIgnoreCase("points")) {
            barData = new BarData(getPointsXAxisValues(), getPointDataSet());
        } else {
            barData = new BarData(getXAxisValues(), getDataSet(activity, type));
        }
        barData.setGroupSpace(10);
        mBarChart.setData(barData);
        mBarChart.setDescription("");
        mBarChart.animateXY(2000, 2000);
        mBarChart.invalidate();

    }


    private void updateUI() {
        try {
            if (!isDetached()) {
                if (mWeekFitnessHistoryList.size() > 0) {
                    initialActivitiesSetUp();
                    FitnessHistory mTodayFitnessActivityData = mWeekFitnessHistoryList.get(mWeekFitnessHistoryList.size() - 1);
                    mTotalStepCount = mTodayFitnessActivityData.getWalkingSteps() + mTodayFitnessActivityData.getRunningSteps();
                    mTotalCaloriesExpended = mTodayFitnessActivityData.getWalkingCaloriesBurnt() + mTodayFitnessActivityData.getRunningCaloriesBurnt() + mTodayFitnessActivityData.getCyclingCaloriesBurnt();
                    mTotalDistanceTravelled = ((mTodayFitnessActivityData.getWalkingDistance() + mTodayFitnessActivityData.getRunningDistance() + mTodayFitnessActivityData.getCyclingDistance()) / 1000);
                    mCaloriesAverage = mPreferenceManager.getIntValue(mContext.getString(R.string.user_goal));
                    mDistanceAverage = calculateDistanceAverage();
                    mStepsAverage = calculateStepAverage();
                    mActivitiesProgressBar.setVisibility(View.GONE);
                    mFitnessActivityImageContainer.setVisibility(View.VISIBLE);
                    mCircularProgressDecoView.setVisibility(View.VISIBLE);
                    mFitnessActivityViewPagerContainer.setVisibility(View.VISIBLE);
                    mActivitiesViewPager.setVisibility(View.VISIBLE);
                    mActivitiesPagerAdapter = new CustomPagerAdapter(getChildFragmentManager());
                    mActivitiesViewPager.setAdapter(mActivitiesPagerAdapter);
                    Log.i("viewpager position", " " + mActivitiesViewPager.getCurrentItem());
                    mActivitiesViewPager.setCurrentItem(0, true);
                    if (mActivitiesViewPager.getCurrentItem() == 0) {
                        resetActivitiesViewPager();
                    } else {
                        mActivitiesViewPager.setCurrentItem(0, true);
                        mCircularProgressDecoView.executeReset();
                        createBackgroundSeries();
                        createBikingActivitySeries();
                        createRunningActivitySeries();
                        createWalkingActivitySeries();
                        resetActivitiesViewPager();
                    }
                }
            } else {
                if (!isDetached())
                    Toast.makeText(mContext, "Can't able to get fitness data from Google Fit.Please try after sometime", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!isDetached())
                Toast.makeText(mContext, "Unable to connect to our server.Please try after sometime", Toast.LENGTH_SHORT).show();
        } finally {
            mIsGetFitnessDataAsyncRunning = false;
        }
    }

   /* private DataReadRequest getTodayFitnessData(long start_time, long end_time) {
        DataReadRequest mFitnessDataRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByActivityType(1, TimeUnit.MILLISECONDS)
                        //.bucketByTime(1,TimeUnit.DAYS)
                .setTimeRange(start_time, end_time, TimeUnit.MILLISECONDS)
                .build();

        return mFitnessDataRequest;
    }

    private DataReadRequest getFitnessData(long start_time, long end_time) {
        DataReadRequest mFitnessDataRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByActivityType(1, TimeUnit.MILLISECONDS)
                .setTimeRange(start_time, end_time, TimeUnit.MILLISECONDS)
                .build();
        return mFitnessDataRequest;
    }

    private void getFitnessActivityDetails(String source, DataReadResult dataReadResult, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.

        if (dataReadResult.getBuckets().size() > 0) {
            FitnessHistory history = new FitnessHistory();
            ArrayList<FitnessActivity> fitnessActivitiesListofDay = new ArrayList<>();
            //Log.e("Fitness data called: ", startDateTime.toString() + " " + dataReadResult.getBuckets().size());
            double total_walking_calories_of_day = 0, total_running_calories_of_day = 0, total_cycling_calories_of_day = 0,
                    total_walking_distance_of_day = 0, total_running_distance_of_day = 0, total_cycling_distance_of_day = 0,
                    total_walking_steps_of_day = 0, total_running_steps_of_day = 0;
            for (Bucket bucket : dataReadResult.getBuckets()) {
                *//*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
               Log.i("Days : ", bucket.getStartTime(TimeUnit.DAYS) + " " + bucket.getEndTime(TimeUnit.DAYS) + " " );
                Log.i("DAY ", "\tStart: " + dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i("DAY ", "\tEnd: " + dateFormat.format(bucket.getEndTime(TimeUnit.MILLISECONDS)));*//*
                //Log.e("Activity", bucket.getActivity());
                if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.WALKING) || bucket.getActivity().equalsIgnoreCase(FitnessActivities.RUNNING) || bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    int step_count = 0;
                    double calories_expended = 0, distance = 0;
                    for (DataSet dataSet : dataSets) {
                        //dumpDataSet(dataSet);
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            //Log.d("TYPE", "\tType: " + dp.getDataType().getName());
                            for (Field field : dp.getDataType().getFields()) {
                                //Log.i("Fields", "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                                if (field.getName().equalsIgnoreCase("steps") && !bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                                    step_count = dp.getValue(field).asInt();
                                } else if (field.getName().equalsIgnoreCase("calories")) {
                                    calories_expended = dp.getValue(field).asFloat();
                                } else if (field.getName().equalsIgnoreCase("distance")) {
                                    distance = dp.getValue(field).asFloat();
                                }
                            }
                        }
                    }
                    if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.WALKING)) {
                        total_walking_calories_of_day = calories_expended;
                        total_walking_distance_of_day = distance;
                        total_walking_steps_of_day = step_count;
                    } else if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.RUNNING)) {
                        total_running_calories_of_day = calories_expended;
                        total_running_distance_of_day = distance;
                        total_running_steps_of_day = step_count;
                    } else if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                        total_cycling_calories_of_day = calories_expended;
                        total_cycling_distance_of_day = distance;
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
            history.setWalkingCaloriesBurnt(total_walking_calories_of_day);
            history.setCyclingCaloriesBurnt(total_cycling_calories_of_day);
            history.setRunningCaloriesBurnt(total_running_calories_of_day);
            history.setWalkingDistance(total_walking_distance_of_day);
            history.setCyclingDistance(total_cycling_distance_of_day);
            history.setRunningDistance(total_running_distance_of_day);
            history.setWalkingSteps((int) total_walking_steps_of_day);
            history.setRunningSteps((int) total_running_steps_of_day);
            history.setFitnessActivitiesList(fitnessActivitiesListofDay);
            if (source.equalsIgnoreCase("chart")) {
                mWeekFitnessHistoryList.add(history);
            } else // Business Offer webservice
                mFitnessHistoryList.add(history);

        }
        // [END parse_read_data_result]
    }

    private void getTodayFitnessActivityDetails(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        mTotalStepCount = 0;
        mTotalCaloriesExpended = 0;
        mTotalDistanceTravelled = 0;

        if (dataReadResult.getBuckets().size() > 0) {

            *//*Log.i("FITNESS RESULT", "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());*//*
            for (Bucket bucket : dataReadResult.getBuckets()) {
                *//*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
               Log.i("Days : ", bucket.getStartTime(TimeUnit.DAYS) + " " + bucket.getEndTime(TimeUnit.DAYS) + " " );
                Log.i("DAY ", "\tStart: " + dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i("DAY ", "\tEnd: " + dateFormat.format(bucket.getEndTime(TimeUnit.MILLISECONDS)));*//*
                //Log.e("Activity : ", bucket.getActivity());
                if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.WALKING) || bucket.getActivity().equalsIgnoreCase(FitnessActivities.RUNNING) || bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    int step_count = 0;
                    double calories_expended = 0, distance = 0;
                    for (DataSet dataSet : dataSets) {
                        //dumpDataSet(dataSet);
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            //Log.d("TYPE", "\tType: " + dp.getDataType().getName());
                            for (Field field : dp.getDataType().getFields()) {
                                *//*Log.i("Fields", "\tField: " + field.getName() +
                                        " Value: " + dp.getValue(field));*//*
                                if (field.getName().equalsIgnoreCase("steps") && !bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                                    step_count = dp.getValue(field).asInt();
                                    mTotalStepCount += step_count;
                                } else if (field.getName().equalsIgnoreCase("calories")) {
                                    calories_expended = dp.getValue(field).asFloat();
                                    mTotalCaloriesExpended += calories_expended;
                                } else if (field.getName().equalsIgnoreCase("distance")) {
                                    distance = dp.getValue(field).asFloat();
                                    mTotalDistanceTravelled += (distance / 1000);
                                }
                            }
                        }
                    }
                    FitnessActivity activity = new FitnessActivity();
                    activity.setName(bucket.getActivity());
                    activity.setCalories_expended(calories_expended);
                    activity.setDistance(distance / 1000);
                    activity.setStep_count(step_count);
                    if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.WALKING)) {
                        //mFitnessActivityList.set(0, activity);
                    } else if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.RUNNING)) {
                        //mFitnessActivityList.set(1, activity);
                    } else if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                        //mFitnessActivityList.set(2, activity);
                    }
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i("FITNESS RESULT", "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
        }
        // [END parse_read_data_result]
    }*/

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkGetAccountsPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.GET_ACCOUNTS);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestGetAccountsPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.GET_ACCOUNTS);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("Permission Request", "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    mView,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.GET_ACCOUNTS},
                                    REQUEST_GETACCOUNTS_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i("Permission", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestBothPermissions() {
        Log.i("Permission", "Requesting permission");
        // Request permission. It's possible this can be auto answered if device policy
        // sets the permission in a given state or the user denied the permission
        // previously and checked "Never ask again".
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_BOTH_PERMISSION_CODE);

    }


    private void requestLocationPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("Permission Request", "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    mView,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i("Permission", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE);
        }
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
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_BOTH_PERMISSION_CODE);
                return;
            }
            buildGoogleFitnessClient();
        } else {
            buildGoogleFitnessClient();
        }
    }

    private void checkAndBuildFitBitApiClient() {
        mOAuthSevice = new ServiceBuilder()
                .provider(FitBitApi.class)
                .apiKey(mContext.getString(R.string.fit_bit_secret_key))
                .apiSecret(mContext.getString(R.string.fit_bit_api_key))
                .callback(mContext.getString(R.string.fit_bit_call_back_url))
                .scope("activity")
                .debug()
                .build();
        mFitBitHelper = new FitBitHelper((Activity) mContext, mOAuthSevice);

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

    private void getPointsList() {
        if (isAdded()) {
            mWeeklyPointsList = new ArrayList<>();
            VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
            RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
            Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.POINTS_LIST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("response", response);
                    if (response != null) {
                        try {
                            JSONObject responseJson = new JSONObject(response);
                            if (responseJson.optInt("status") == 1) {
                                mTotalPointEarned = responseJson.optInt("grandtotalpoints");
                                mPreferenceManager.setIntValue(mContext.getString(R.string.your_total_points), mTotalPointEarned);
                                if (!responseJson.optString("lastcaloriesupdate").startsWith("0000")) {
                                    LocalDateTime lastUpdateddatetime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseLocalDateTime(responseJson.optString("lastcaloriesupdate"));
                                    mPreferenceManager.setLongValue(mContext.getString(R.string.last_updated_calories_time), lastUpdateddatetime.toDateTime().getMillis());
                                } else
                                    mPreferenceManager.setLongValue(mContext.getString(R.string.last_updated_calories_time), -1);
                                JSONArray points_list = responseJson.optJSONArray("weeklist");
                                int total_points = 0;
                                if (points_list != null && points_list.length() > 0) {
                                    for (int i = 0; i < points_list.length(); i++) {
                                        JSONObject points_object = points_list.optJSONObject(i);
                                        int points = points_object.optInt("TotalCalories");
                                        mWeeklyPointsList.add(points);
                                        total_points += points;
                                    }
                                    mPointsAverage = total_points / 7;
                                } else {
                                    if (!isDetached())
                                        Toast.makeText(mContext, "Burn more calories to avail offers", Toast.LENGTH_SHORT).show();
                                }
                                if (!isDetached())
                                    updateUI();
                            } else {
                                if (!isDetached()) {
                                    updateUI();
                                    Toast.makeText(mContext, "Burn more calories to avail offers", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (JSONException e) {
                            if (!isDetached()) {
                                updateUI();
                                e.printStackTrace();
                                Toast.makeText(mContext, "Failure response from server", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        if (!isDetached()) {
                            updateUI();
                            Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
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
                        root.put("user_id", mPreferenceManager.getStringValue(mContext.getString(R.string.user_id)));
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
                    params.put("fitness_source", String.valueOf(mPreferenceManager.getIntValue(mContext.getString(R.string.selected_fitness_source)))); // 1 - google Fit , 2 - Fitbit
                    return params;
                }
            });
            volleyrequest.setRequestPolicy(request);
            mRequestQueue.add(request);
        }
    }




    /*private void getBusinessList() {
        RequestQueue mRequestQueue = VolleySingleton.getSingletonInstance().getRequestQueue();
        mRequestQueue.add((new StringRequest(Request.Method.POST, WebserviceAPI.BUSINESS_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (!responseJson.optString("msg").trim().equalsIgnoreCase("No Offer Found")) {
                            ArrayList mBusinessOfferList = new ArrayList<BusinessDetails>();
                            mPointsEarnedText.setText(responseJson.optInt("yourpoint") + "!");
                            JSONArray business_list = responseJson.optJSONArray("businesslist");
                            if (business_list != null && business_list.length() > 0) {
                                for (int i = 0; i < business_list.length(); i++) {
                                    JSONObject business_object = business_list.optJSONObject(i);
                                    BusinessDetails businessDetails = new BusinessDetails();
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
                                    businessDetails.setPoints_needed(business_object.optInt(""));
                                    businessDetails.setTerms_and_conditions(business_object.optString("termsandconditions"));
                                    businessDetails.setCoupon(business_object.optString("couponCode"));
                                    mBusinessOfferList.add(businessDetails);
                                }
                            } else {
                                if (!isDetached())
                                    Toast.makeText(mContext, "Burn more calories to avail offers", Toast.LENGTH_SHORT).show();
                            }
                            if (!isDetached())
                             updateUI(mBusinessOfferList);
                        } else {
                            if (!isDetached()){
                                updateUI(null);
                                Toast.makeText(mContext, "Burn more calories to avail offers", Toast.LENGTH_SHORT).show();
                            }

                        }

                    } catch (JSONException e) {
                        if(!isDetached()){
                            updateUI(null);
                            e.printStackTrace();
                            Toast.makeText(mContext, "Failure response from server", Toast.LENGTH_SHORT).show();

                        }

                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if(!isDetached()){
                        updateUI(null);
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
                    root.put("date_time", "");
                    root.put("flag", "android");
                    root.put("fitness_email_id", mFitnessEmail);
                    JSONArray activities_array = new JSONArray();
                    for (int i = 0; i < 3; i++) {
                        JSONObject activity = new JSONObject();
                        activity.put("name", mFitnessActivityList.get(i).getName());
                        activity.put("calories_burnt", mFitnessActivityList.get(i).getCalories_expended() == -1 ? 0 : mFitnessActivityList.get(i).getCalories_expended());
                        activity.put("step_count", mFitnessActivityList.get(i).getStep_count() == -1 ? 0 : mFitnessActivityList.get(i).getStep_count());
                        activity.put("distance", mFitnessActivityList.get(i).getDistance() == -1 ? 0 : mFitnessActivityList.get(i).getDistance());
                        activities_array.put(activity);
                    }
                    root.put("activities", activities_array);
                    json_request = root.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("json request", json_request);
                params.put("jsonrequest", json_request);
                return params;
            }
        }));
    }*/


    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        for (int i = 0; i < mWeekFitnessHistoryList.size(); i++) {
            FitnessHistory history = mWeekFitnessHistoryList.get(i);
            xAxis.add(history.getStartDateTime().toString("E"));
        }
        return xAxis;
    }

    private ArrayList<String> getPointsXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        for (int i = 0; i < mWeeklyPointsList.size(); i++) {
            LocalDateTime dateTime = new LocalDateTime().minusDays(6 - i);
            xAxis.add(dateTime.toString("E"));
        }
        return xAxis;
    }

    private ArrayList<IBarDataSet> getDataSet(String activity, String type) {
        ArrayList<BarEntry> mBarEntry = new ArrayList<>();
        for (int i = 0; i < mWeekFitnessHistoryList.size(); i++) {
            FitnessHistory fitnessHistory = mWeekFitnessHistoryList.get(i);
            BarEntry barEntry = null;
            switch (activity) {
                case "all":
                    if (type.equalsIgnoreCase("calories"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getWalkingCaloriesBurnt(), (float) fitnessHistory.getRunningCaloriesBurnt(), (float) fitnessHistory.getCyclingCaloriesBurnt()}, i);
                    else if (type.equalsIgnoreCase("distance"))
                        barEntry = new BarEntry(new float[]{(float) (fitnessHistory.getWalkingDistance() / 1000), (float) (fitnessHistory.getRunningDistance() / 1000), (float) (fitnessHistory.getCyclingDistance() / 1000)}, i);
                    else  // Steps
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getWalkingSteps(), (float) fitnessHistory.getRunningSteps(), (float) fitnessHistory.getCyclingSteps()}, i);
                    mBarEntry.add(barEntry);
                    break;
                case FitnessActivities.WALKING:
                    if (type.equalsIgnoreCase("calories"))
                        barEntry = new BarEntry((float) fitnessHistory.getWalkingCaloriesBurnt(), i);
                    else if (type.equalsIgnoreCase("distance"))
                        barEntry = new BarEntry((float) fitnessHistory.getWalkingDistance() / 1000, i);
                    else if (type.equalsIgnoreCase("steps")) // Steps
                        barEntry = new BarEntry(fitnessHistory.getWalkingSteps(), i);
                    if (barEntry != null)
                        mBarEntry.add(barEntry);
                    break;
                case FitnessActivities.RUNNING:
                    if (type.equalsIgnoreCase("calories"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getRunningCaloriesBurnt()}, i);
                    else if (type.equalsIgnoreCase("distance"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getRunningDistance() / 1000}, i);
                    else  // Steps
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getRunningSteps()}, i);
                    if (barEntry != null)
                        mBarEntry.add(barEntry);
                    break;
                case FitnessActivities.BIKING:
                    if (type.equalsIgnoreCase("calories"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getCyclingCaloriesBurnt()}, i);
                    else if (type.equalsIgnoreCase("distance"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getCyclingDistance() / 1000}, i);
                    else  // Steps
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getCyclingSteps()}, i);
                    if (barEntry != null)
                        mBarEntry.add(barEntry);
                    break;
            }
        }
        BarDataSet barDataSet = new BarDataSet(mBarEntry, "Calories Burned");
        //barDataSet.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        if (activity.equalsIgnoreCase("all"))
            barDataSet.setColors(getColors());
        else if (activity.equalsIgnoreCase(FitnessActivities.WALKING))
            barDataSet.setColor(ContextCompat.getColor(mContext, R.color.walking_color));
        else if (activity.equalsIgnoreCase(FitnessActivities.RUNNING))
            barDataSet.setColor(ContextCompat.getColor(mContext, R.color.running_color));
        else
            barDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.biking_color));
        barDataSet.setDrawValues(false);
        barDataSet.setBarSpacePercent(70f);
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(barDataSet);
        return dataSets;
    }

    private ArrayList<IBarDataSet> getPointDataSet() {
        ArrayList<BarEntry> mBarEntry = new ArrayList<>();
        for (int i = 0; i < mWeeklyPointsList.size(); i++) {
            BarEntry barEntry = new BarEntry((float) mWeeklyPointsList.get(i), i);
            mBarEntry.add(barEntry);
        }
        BarDataSet barDataSet = new BarDataSet(mBarEntry, "Calories Burned");
        barDataSet.setColor(ContextCompat.getColor(mContext, R.color.walking_color));
        barDataSet.setDrawValues(false);
        barDataSet.setBarSpacePercent(70f);
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(barDataSet);
        return dataSets;
    }

    private int[] getColors() {

        int stacksize = 3;
        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        colors[0] = ContextCompat.getColor(mContext, R.color.walking_color);
        colors[1] = ContextCompat.getColor(mContext, R.color.running_color);
        colors[2] = ContextCompat.getColor(mContext, R.color.biking_color);

        return colors;
    }

    class FitBitAPIAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Verifier verifier = new Verifier(params[0]);
                Token accessToken = mOAuthSevice.getAccessToken(null, verifier);
                if (accessToken != null) {
                    mPreferenceManager.setBooleanValue(mContext.getString(R.string.is_fitbit_authenticated), true);
                    mPreferenceManager.setStringValue(mContext.getString(R.string.access_token), accessToken.getToken());
                    JSONObject accessTokenObject = new JSONObject(accessToken.getRawResponse());
                    mPreferenceManager.setStringValue(mContext.getString(R.string.refresh_token), accessTokenObject.optString("refresh_token"));
                    Log.i("access_token", " Response :" + accessToken.getRawResponse());
                    return "success";
                } else
                    return "failure";

            } catch (Exception e) {
                e.printStackTrace();
                return "failure";
            }

        }

        @Override
        protected void onPostExecute(String status) {
            super.onPostExecute(status);
            if (status.equalsIgnoreCase("success")) {
                if (!mIsGetFitnessDataAsyncRunning) {
                    if (new NetworkCheck().ConnectivityCheck(mContext)) {
                        new FitnessDataAsync().execute();
                    } else {
                        // Hide progress bar
                        mActivitiesProgressBar.setVisibility(View.GONE);
                        if (!isDetached())
                            Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // Failure
                // Hide Progress Bar
                mActivitiesProgressBar.setVisibility(View.GONE);
            }
        }
    }

    private void updateUserGoal(final int goal) {
        final ProgressDialog mLoadingProgress = new ProgressDialog(mContext);
        mLoadingProgress.setMessage("Updating goal please wait!!!");
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.USER_GOAL_UPDATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("user goal response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1) {
                            mPreferenceManager.setIntValue(mContext.getString(R.string.user_goal), goal);
                        } else {
                            Snackbar.make(mView, responseJson.optString("msg"), Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "Failure response from server", Toast.LENGTH_SHORT).show();
                    } finally {
                        mLoadingProgress.dismiss();
                        setFitnessSource();
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoadingProgress.dismiss();
                setFitnessSource();
                try {
                    Snackbar.make(mView, "Unable to connect to our server", Snackbar.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", mPreferenceManager.getStringValue(mContext.getString(R.string.user_id)));
                params.put("usergoal", String.valueOf(goal));
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }

}


