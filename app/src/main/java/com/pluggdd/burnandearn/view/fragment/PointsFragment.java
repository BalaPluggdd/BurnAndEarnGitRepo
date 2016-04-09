package com.pluggdd.burnandearn.view.fragment;

import android.Manifest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.pluggdd.burnandearn.activity.OfferDetailActivity;
import com.pluggdd.burnandearn.activity.ShareActivity;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.model.FitnessHistory;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment to show activity details and offers
 */
public class PointsFragment extends Fragment implements View.OnClickListener {
    // Initialization of views and variables
    private final int REQUEST_CODE_RESOLUTION = 100, REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 101, REQUEST_GETACCOUNTS_PERMISSIONS_REQUEST_CODE = 102, REQUEST_BOTH_PERMISSION_CODE = 103;
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
    private int mTotalStepCount = 0, mStepsAverage = 0, mPointsAverage = 0;
    private double mTotalCaloriesExpended = 0, mCaloriesAverage = 0, mTotalDistanceTravelled = 0, mDistanceAverage = 0;
    private int mBackIndex, mWalkingActivityIndex, mRunningActivityIndex, mBikingActivityIndex, mTotalPointEarned;
    //private Spinner mDateFilterSpinner;
    private boolean mIsPermissionRequestRaised, mIsGetFitnessDataAsyncRunning;
    private PreferencesManager mPreferenceManager;
    private String mFitnessEmail = "", mCurrentFitnessActivity;
    private CustomPagerAdapter mActivitiesPagerAdapter;
    private GoogleFitHelper mGoogleFitHelper;
    private ArrayList<FitnessHistory> mFitnessHistoryList = new ArrayList<>();
    private ArrayList<FitnessHistory> mWeekFitnessHistoryList = new ArrayList<>();
    private ArrayList<Integer> mWeeklyPointsList = new ArrayList<>();

    public PointsFragment() {
        // Required empty public constructor
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
        checkAndBuildGoogleApiClient();
        initializeActivitiesBarChart();
        // Initial set up for circular decoview
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
        SeriesItem initial_grey_circle_progress = new SeriesItem.Builder(Color.parseColor("#a9a9a9"))
                .setRange(0, 100, 0)
                .setLineWidth(50f)
                .build();
        mBackIndex = mCircularProgressDecoView.addSeries(initial_grey_circle_progress);
        mCircularProgressDecoView.addEvent(new DecoEvent.Builder(100).setIndex(mBackIndex).build());
    }

    // Walking activity series
    private void createWalkingActivitySeries() {
        SeriesItem walking_activity_series = new SeriesItem.Builder(Color.parseColor("#53B6CD"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .setLineWidth(50f)
                .build();
        mWalkingActivityIndex = mCircularProgressDecoView.addSeries(walking_activity_series);
    }

    // Running activity series
    private void createRunningActivitySeries() {
        SeriesItem running_activity_series = new SeriesItem.Builder(Color.parseColor("#F8C900"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .setLineWidth(50f)
                .build();
        mRunningActivityIndex = mCircularProgressDecoView.addSeries(running_activity_series);
    }

    // Biking activity series
    private void createBikingActivitySeries() {
        SeriesItem biking_activity_series = new SeriesItem.Builder(Color.parseColor("#B0C53C"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .setLineWidth(50f)
                .build();
        mBikingActivityIndex = mCircularProgressDecoView.addSeries(biking_activity_series);
    }

    private void updateFitnessActivities(int position) {
        if(mWeekFitnessHistoryList.size() > 0){
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
                    walking_calories_percentage = (walking_calories_expended / mCaloriesAverage) * 100;
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
                    running_calories_percentage = (running_calories_expended / mCaloriesAverage) * 100;
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
                    biking_calories_percentage = (biking_calories_expended / mCaloriesAverage) * 100;
                    // To update decoview
                    //walking_calories_percentage = 30 ; running_calories_percentage = 0; biking_calories_percentage = 10;
                    updateProgressBar(walking_calories_percentage, running_calories_percentage, biking_calories_percentage);
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
                    walking_distance_percentage = (walking_distance_travelled / mDistanceAverage) * 100;
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
                    running_distance_percentage = (running_distance_travelled / mDistanceAverage) * 100;
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
                    biking_distance_percentage = (biking_distance_travelled / mDistanceAverage) * 100;
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
                    walking_steps_percentage = (walking_steps_taken / mStepsAverage) * 100;
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
                    running_steps_percentage = (running_steps_taken / mStepsAverage) * 100;
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
                    biking_steps_percentage = (biking_steps_taken / mStepsAverage) * 100;
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
                    today_points_percentage = (today_points / mPointsAverage) * 100;
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

    // To update fitness acitvity details in viewpager and in main view
   /* private void updateFitnessActivities(int position) {
        switch (position) {
            case 0:
                // To calculate walking calories percentage in total calories
                int walking_calories_percentage = 0, running_calories_percentage = 0, biking_calories_percentage = 0;
                double walking_calories_expended = mFitnessActivityList.get(0).getCalories_expended();
                //walking_calories_expended = 1500;
                if (walking_calories_expended != -1) {
                    walking_calories_percentage = (int) Math.round((walking_calories_expended / mTotalCaloriesExpended) * 100);
                    mWalkingActivityContainer.setVisibility(View.VISIBLE);
                    mWalkingActivityValueText.setText(String.valueOf(Math.round(walking_calories_expended)));
                    mWalkingActivityDimesionText.setText(getString(R.string.calories_unit));
                } else {
                    mWalkingActivityContainer.setVisibility(View.GONE);
                }
                // To calculate running calories percentage in total calories
                double running_calories_expended = mFitnessActivityList.get(1).getCalories_expended();
                //running_calories_expended = 2000;
                if (running_calories_expended != -1) {
                    running_calories_percentage = (int) Math.round((running_calories_expended / mTotalCaloriesExpended) * 100);
                    mRunningActivityContainer.setVisibility(View.VISIBLE);
                    mRunningActivityValueText.setText(String.valueOf(Math.round(running_calories_expended)));
                    mRunningActivityDimensionText.setText(getString(R.string.calories_unit));
                } else {
                    mRunningActivityContainer.setVisibility(View.GONE);
                }
                // To calculate biking calories percentage in total calories
                double biking_calories_expended = mFitnessActivityList.get(2).getCalories_expended();
                //biking_calories_expended = 200;
                if (biking_calories_expended != -1) {
                    biking_calories_percentage = (int) Math.round((biking_calories_expended / mTotalCaloriesExpended) * 100);
                    mBikingActivityContainer.setVisibility(View.VISIBLE);
                    mCyclingActivityValueText.setText(String.valueOf(Math.round(biking_calories_expended)));
                    mCyclingActivityDimensionText.setText(getString(R.string.calories_unit));
                } else {
                    mBikingActivityContainer.setVisibility(View.GONE);
                }
                // To update decoview
                updateProgressBar(walking_calories_percentage, running_calories_percentage, biking_calories_percentage);
                break;
            case 1:
                // To calculate walking distance percentage in total distance travelled
                int walking_distance_percentage = 0, running_distance_percentage = 0, biking_distance_percentage = 0;
                double walking_distance = mFitnessActivityList.get(0).getDistance();
                //walking_distance = 5.0;
                if (walking_distance != -1) {
                    walking_distance_percentage = (int) Math.round((walking_distance / mTotalDistanceTravelled) * 100);
                    mWalkingActivityContainer.setVisibility(View.VISIBLE);
                    mWalkingActivityValueText.setText(String.format("%.2f", walking_distance));
                    mWalkingActivityDimesionText.setText(getString(R.string.distance_dimension));
                } else {
                    mWalkingActivityContainer.setVisibility(View.GONE);
                }
                // To calculate running distance percentage in total distance travelled
                double running_distance = mFitnessActivityList.get(1).getDistance();
                //running_distance = 5.0;
                if (running_distance != -1) {
                    running_distance_percentage = (int) Math.round((running_distance / mTotalDistanceTravelled) * 100);
                    mRunningActivityContainer.setVisibility(View.VISIBLE);
                    mRunningActivityValueText.setText(String.format("%.2f", running_distance));
                    mRunningActivityDimensionText.setText(getString(R.string.distance_dimension));
                } else {
                    mRunningActivityContainer.setVisibility(View.GONE);
                }
                // To calculate biking distance percentage in total distance travelled
                double biking_distance = mFitnessActivityList.get(2).getDistance();
                // biking_distance = 6.0;
                if (biking_distance != -1) {
                    biking_distance_percentage = (int) Math.round((biking_distance / mTotalDistanceTravelled) * 100);
                    mBikingActivityContainer.setVisibility(View.VISIBLE);
                    mCyclingActivityValueText.setText((String.format("%.2f", biking_distance)));
                    mCyclingActivityDimensionText.setText(getString(R.string.distance_dimension));
                } else {
                    mBikingActivityContainer.setVisibility(View.GONE);
                }
                // To update decoview
                updateProgressBar(walking_distance_percentage, running_distance_percentage, biking_distance_percentage);
                break;
            case 2:
                // To calculate walking steps percentage in total steps taken
                int walking_step_percentage = 0, running_step_percentage = 0, biking_step_percentage = 0;
                double walking_step = mFitnessActivityList.get(0).getStep_count();
                //walking_step = 1000;
                if (walking_step != -1) {
                    walking_step_percentage = (int) Math.round((walking_step / mTotalStepCount) * 100);
                    mWalkingActivityContainer.setVisibility(View.VISIBLE);
                    mWalkingActivityValueText.setText(String.valueOf(Math.round(walking_step)));
                    mWalkingActivityDimesionText.setText(getString(R.string.steps_text));
                } else {
                    mWalkingActivityContainer.setVisibility(View.GONE);
                }
                // To calculate running steps percentage in total steps taken
                double running_step = mFitnessActivityList.get(1).getStep_count();
                //running_step = 20000;
                if (running_step != -1) {
                    running_step_percentage = (int) Math.round((running_step / mTotalStepCount) * 100);
                    mRunningActivityContainer.setVisibility(View.VISIBLE);
                    mRunningActivityValueText.setText(String.valueOf(running_step));
                    mRunningActivityDimensionText.setText(getString(R.string.steps_text));
                } else {
                    mRunningActivityContainer.setVisibility(View.GONE);
                }
                // To calculate biking steps percentage in total steps taken
                double biking_step = mFitnessActivityList.get(2).getStep_count();
                //biking_step = 4000;
                if (biking_step != -1) {
                    biking_step_percentage = (int) Math.round((biking_step / mTotalStepCount) * 100);
                    mBikingActivityContainer.setVisibility(View.VISIBLE);
                    mCyclingActivityValueText.setText(String.valueOf(Math.round(biking_step)));
                    mCyclingActivityDimensionText.setText(getString(R.string.steps_text));
                } else {
                    mBikingActivityContainer.setVisibility(View.GONE);
                }
                // To update decoview
                updateProgressBar(walking_step_percentage, running_step_percentage, biking_step_percentage);
                break;
            case 3:
                break;
            default:
                break;
        }
    }*/

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
        /*if (biking_percentage > 0) {
            mCircularProgressDecoView.moveTo(mBikingActivityIndex, 100);
            if (running_percentage > 0) {
                mCircularProgressDecoView.moveTo(mRunningActivityIndex, running_percentage + walking_percentage);
            }
        } else if (running_percentage > 0) {
            mCircularProgressDecoView.moveTo(mRunningActivityIndex, 100);
        }
        if (walking_percentage > 0)
            mCircularProgressDecoView.moveTo(mWalkingActivityIndex, walking_percentage);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE_RESOLUTION) {
            mGoogleAPIClient.connect();
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
        if (mGoogleAPIClient == null) {
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
                        if (!mIsGetFitnessDataAsyncRunning)
                            new FitnessDataAsync().execute();
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
            mWeekFitnessHistoryList = mGoogleFitHelper.getLastWeekData();
            /*List<LocalDateTime> dates = new ArrayList<>();
            LocalDateTime StartDate = new LocalDateTime().minusDays(7).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            int days = Days.daysBetween(StartDate, new LocalDateTime()).getDays();
            for (int i = 0; i < days; i++) {
                LocalDateTime d = StartDate.withFieldAdded(DurationFieldType.days(), i);
                dates.add(d);
            }
            mWeekFitnessHistoryList = new ArrayList<>();
            for (LocalDateTime date : dates) {
                LocalDateTime endDayTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                getFitnessActivityDetails("chart", Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(date.toDateTime().getMillis(), endDayTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), date, endDayTime);
            }
            getTodayFitnessDetails("chart");*/
            return "success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new BusinessListAsync().execute();
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
                mFitnessHistoryList = mGoogleFitHelper.getFitnessHistoryData();
                /*long last_calories_updated_time = mPreferenceManager.getLongValue(getString(R.string.last_updated_calories_time));
                //last_calories_updated_time = new LocalDateTime().minusHours(20).toDateTime().getMillis();
                //last_calories_updated_time = 1459098422000L;
                if (last_calories_updated_time == 0) { // App installed today only,so send today fitnessDetails only
                    mFitnessHistoryList = new ArrayList<>();
                    getTodayFitnessDetails("business_offer");
                } else { // Send fitness data from last synced date to current time
                    List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
                    LocalDateTime startDate = new LocalDateTime(last_calories_updated_time);
                    LocalDateTime currentDateTime = new LocalDateTime();
                    int days = Days.daysBetween(startDate.toDateTime().withTimeAtStartOfDay(), currentDateTime.toDateTime().withTimeAtStartOfDay()).getDays();
                    if(days == 0){
                        Log.i("start date : ", startDate.toString() + " " + " end date :" + currentDateTime.toString());
                        getFitnessActivityDetails("business_offer", Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(startDate.toDateTime().getMillis(), currentDateTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), startDate, currentDateTime);
                    }else{
                        for (int i = 0; i < days; i++) {
                            LocalDateTime d = startDate.withFieldAdded(DurationFieldType.days(), i);
                            dates.add(d);
                        }
                        for(int i=0 ; i< dates.size() ; i++){
                            LocalDateTime date = dates.get(i);
                            LocalDateTime startDateTime,endDateTime;
                            if(i == 0 ){// First position as start date
                                startDateTime = date;
                            }else{
                                startDateTime = date.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            }
                            endDateTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            Log.i("start date : ", date.toString() + " " + " end date :" + endDateTime.toString());
                            getFitnessActivityDetails("business_offer", Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(startDateTime.toDateTime().getMillis(), endDateTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), startDateTime, endDateTime);
                        }
                        getTodayFitnessDetails("business_offer"); // To add today fitness details as last one
                    }

                    *//*if (days == 0) {
                        Log.i("start date : ", startDate.toString() + " " + " end date :" + currentDateTime.toString());
                        mFitnessHistoryList = new ArrayList<>();
                        if (startDate.get(DateTimeFieldType.dayOfMonth()) == currentDateTime.get(DateTimeFieldType.dayOfMonth())) { //// Calories already sent  for current date
                            getFitnessActivityDetails("business_offer", Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(startDate.toDateTime().getMillis(), currentDateTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), startDate, currentDateTime);
                        } else { // Calories already sent yesterday but date difference is not 1...
                            LocalDateTime startDateMidnight = startDate.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            getFitnessActivityDetails("business_offer", Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(startDate.toDateTime().getMillis(), startDateMidnight.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), startDate, startDateMidnight);
                            getTodayFitnessDetails("business_offer");
                        }
                    } else {
                        for (int i = 0; i < days; i++) {
                            LocalDateTime d = startDate.withFieldAdded(DurationFieldType.days(), i);
                            dates.add(d);
                        }
                        mFitnessHistoryList = new ArrayList<>();
                        for (LocalDateTime date : dates) {
                            LocalDateTime endDayTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                            Log.i("start date : ", date.toString() + " " + date.toDateTime().getMillis() + " end date :" + endDayTime.toString());
                            //getFitnessActivityDetails("business_offer", Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(date.toDateTime().getMillis(), endDayTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), date, endDayTime);
                        }
                        getTodayFitnessDetails("business_offer"); // To add today fitness details as last one
                    }*//*
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            if(new NetworkCheck().ConnectivityCheck(mContext)){
                if(mFitnessHistoryList != null && mFitnessHistoryList.size() >0){
                    getPointsList();
                }else{
                    updateUI();
                    Snackbar.make(mView,getString(R.string.no_google_fit_data),Snackbar.LENGTH_SHORT).show();
                }
            }else{
                updateUI();
                Snackbar.make(mView,getString(R.string.no_network),Snackbar.LENGTH_SHORT).show();
            }

        }
    }

    /*public class FitnessHistoryAsync extends AsyncTask<Long, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Long... params) {
            List<LocalDateTime> dates = new ArrayList<>();
            LocalDateTime StartDate = new LocalDateTime().minusDays(7).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            int days = Days.daysBetween(StartDate, new LocalDateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)).getDays();
            for (int i = 0; i < days; i++) {
                LocalDateTime d = StartDate.withFieldAdded(DurationFieldType.days(), i);
                dates.add(d);
            }
            mWeekFitnessHistoryList = new ArrayList<>();
            for (LocalDateTime date : dates) {
                LocalDateTime endDayTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                //Log.i("start date : ", date.toString() + " " + date.toDateTime().getMillis() + " end date :" + endDayTime.toString());
                getFitnessActivityDetails("chart", Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(date.toDateTime().getMillis(), endDayTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), date, endDayTime);
            }
            *//*if (mGoogleAPIClient != null) {
                return getFitnessActivityDetails(Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(params[0], params[1])).await(1, TimeUnit.MINUTES));

            } else
                return null;*//*
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            mBarChart.setVisibility(View.VISIBLE);
            for (FitnessHistory history : mWeekFitnessHistoryList) {
                Log.i("Time", history.getStartDateTime() + " " + history.getEndDateTime() + " " + history.getTotalCaloriesBurnt());
                for (FitnessActivity activity : history.getFitnessActivitiesList()) {
                    Log.i("Name", activity.getName());
                    Log.i("calories", activity.getCalories_expended() + "");
                    Log.i("distance", activity.getDistance() + "");
                    Log.i("stepcount", activity.getStep_count() + "");
                }
            }

        }
    }*/

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

   /* private void getTodayFitnessDetails(String flag) {
        LocalDateTime currentDateTime = new LocalDateTime();
        long endTime = currentDateTime.toDateTime().getMillis();
        LocalDateTime startdateTime = currentDateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        long startTime = startdateTime.toDateTime().getMillis();
        Log.i("Time :", startdateTime.toString() + " " + currentDateTime.toDateTime().toString());
        getFitnessActivityDetails(flag, Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(startTime, endTime)).await(1, TimeUnit.MINUTES), startdateTime, currentDateTime);
    }*/

    private void updateUI() {
        if (!isDetached()) {
            if (mWeekFitnessHistoryList.size() > 0) {
                initialActivitiesSetUp();
                FitnessHistory mTodayFitnessActivityData = mWeekFitnessHistoryList.get(mWeekFitnessHistoryList.size() - 1);
                mTotalStepCount = mTodayFitnessActivityData.getWalkingSteps() + mTodayFitnessActivityData.getRunningSteps();
                mTotalCaloriesExpended = mTodayFitnessActivityData.getWalkingCaloriesBurnt() + mTodayFitnessActivityData.getRunningCaloriesBurnt() + mTodayFitnessActivityData.getCyclingCaloriesBurnt();
                mTotalDistanceTravelled = ((mTodayFitnessActivityData.getWalkingDistance() + mTodayFitnessActivityData.getRunningDistance() + mTodayFitnessActivityData.getCyclingDistance()) / 1000);
                mCaloriesAverage = mPreferenceManager.getIntValue(getString(R.string.calories_goal));
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
            Snackbar.make(mView,"Can't able to get fitness data from Google Fit.Please try after sometime",Snackbar.LENGTH_SHORT).show();

        }
        mIsGetFitnessDataAsyncRunning = false;


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
                                mPreferenceManager.setIntValue(getString(R.string.your_total_points), mTotalPointEarned);
                                if (!responseJson.optString("lastcaloriesupdate").startsWith("0000")) {
                                    LocalDateTime lastUpdateddatetime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseLocalDateTime(responseJson.optString("lastcaloriesupdate"));
                                    mPreferenceManager.setLongValue(getString(R.string.last_updated_calories_time), lastUpdateddatetime.toDateTime().getMillis());
                                } else
                                    mPreferenceManager.setLongValue(getString(R.string.last_updated_calories_time), -1);

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
                            // To show notification when user achieves his goal
                            if (mTotalCaloriesExpended >= mPreferenceManager.getIntValue(getString(R.string.calories_goal))) {
                                long last_notification_time = mPreferenceManager.getLongValue(getString(R.string.notification_time));
                                if (last_notification_time == 0)
                                    showNotification();
                                else {
                                    LocalDateTime startDate = new LocalDateTime(last_notification_time);
                                    LocalDateTime currentDateTime = new LocalDateTime();
                                    int days = Days.daysBetween(startDate.toDateTime().withTimeAtStartOfDay(), currentDateTime.toDateTime().withTimeAtStartOfDay()).getDays();
                                    if (days > 0) {
                                        showNotification();
                                    }
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
                    return params;
                }
            });
            volleyrequest.setRequestPolicy(request);
            mRequestQueue.add(request);
        }

    }

    private void showNotification() {
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(mContext);
        mNotificationBuilder.setSmallIcon(R.drawable.ic_logo);
        mNotificationBuilder.setContentTitle(getString(R.string.goal_achieved_notification_header));
        mNotificationBuilder.setAutoCancel(true);
        String content = getString(R.string.goal_achieved_notification_content) + " " + String.valueOf(mPreferenceManager.getIntValue(getString(R.string.calories_goal))) + " " + getString(R.string.calories_unit);
        mNotificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        mNotificationBuilder.setContentText(content);
        //Intent shareIntent = new Intent(mContext, ShareActivity.class);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        shareIntent.setType("text/plain");
        /*shareIntent.putExtra(getString(R.string.from_notification), true);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
        taskStackBuilder.addParentStack(ShareActivity.class);
        taskStackBuilder.addNextIntent(shareIntent);
        // To add pending intent
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);*/
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, Intent.createChooser(shareIntent, "Share"), PendingIntent.FLAG_UPDATE_CURRENT);
        // To add Share button
        NotificationCompat.Action mShareAction = new NotificationCompat.Action.Builder(R.drawable.ic_share, "Share", pendingIntent).build();
        mNotificationBuilder.addAction(mShareAction);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mNotificationBuilder.build());
        mPreferenceManager.setLongValue(getString(R.string.notification_time), new LocalDateTime().toDateTime().getMillis());
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

    class CustomBusinessOfferListAdapter extends PagerAdapter {

        private LayoutInflater mLayoutInflater;
        private ArrayList<BusinessDetails> mBusinessList;

        CustomBusinessOfferListAdapter(ArrayList<BusinessDetails> businessList) {
            if (!isDetached()) {
                mLayoutInflater = LayoutInflater.from(mContext);
                mBusinessList = businessList;
            }
        }

        @Override
        public int getCount() {
            return mBusinessList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final BusinessDetails businessDetail = mBusinessList.get(position);
            View itemView = mLayoutInflater.inflate(R.layout.list_row_business_offer, container, false);
            TextView offerRewardsText = (TextView) itemView.findViewById(R.id.txt_offer_promo);
            TextView nameText = (TextView) itemView.findViewById(R.id.txt_business_title);
            ImageView businessImage = (ImageView) itemView.findViewById(R.id.img_business_logo);
            ProgressBar logoProgressBar = (ProgressBar) itemView.findViewById(R.id.logo_progress_bar);
            nameText.setText(businessDetail.getName());
            nameText.setSelected(true);
            if (!isDetached())
                new PicassoImageLoaderHelper(mContext, businessImage, logoProgressBar).loadImage(businessDetail.getLogo());
            offerRewardsText.setText(businessDetail.getPromo());
            Button mGrabNowButton = (Button) itemView.findViewById(R.id.btn_grab_now);
            mGrabNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.page_flag), PointsFragment.this.getClass().getSimpleName());
                    mListener.changeFragment(bundle);*/
                    Intent intent = new Intent(mContext, OfferDetailActivity.class);
                    intent.putExtra(getString(R.string.logo_url), businessDetail.getLogo());
                    intent.putExtra(getString(R.string.business_name), businessDetail.getName());
                    intent.putExtra(getString(R.string.offer_name), businessDetail.getOffer_name());
                    intent.putExtra(getString(R.string.offer_promo), businessDetail.getPromo());
                    intent.putExtra(getString(R.string.how_to_redeem), businessDetail.getHow_to_reedem());
                    intent.putExtra(getString(R.string.coupon_expiry_date), businessDetail.getCoupon_expiry_date());
                    intent.putExtra(getString(R.string.coupon), businessDetail.getCoupon());
                    intent.putExtra(getString(R.string.redirect_url), businessDetail.getUrl() /*"https://play.google.com/store/apps/details?id=com.tingtongapp.android&hl=en"*/);
                    startActivity(intent);
                }
            });
            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

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
            LocalDateTime dateTime = new LocalDateTime().minusDays(7-i);
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
                        barEntry = new BarEntry(new float[]{(float) (fitnessHistory.getWalkingDistance()/1000), (float) (fitnessHistory.getRunningDistance()/1000), (float) (fitnessHistory.getCyclingDistance()/1000)}, i);
                    else  // Steps
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getWalkingSteps(), (float) fitnessHistory.getRunningSteps(), (float) fitnessHistory.getCyclingSteps()}, i);
                    mBarEntry.add(barEntry);
                    break;
                case FitnessActivities.WALKING:
                    if (type.equalsIgnoreCase("calories"))
                        barEntry = new BarEntry((float) fitnessHistory.getWalkingCaloriesBurnt(), i);
                    else if (type.equalsIgnoreCase("distance"))
                        barEntry = new BarEntry((float) fitnessHistory.getWalkingDistance()/1000, i);
                    else if (type.equalsIgnoreCase("steps")) // Steps
                        barEntry = new BarEntry(fitnessHistory.getWalkingSteps(), i);
                    if (barEntry != null)
                        mBarEntry.add(barEntry);
                    break;
                case FitnessActivities.RUNNING:
                    if (type.equalsIgnoreCase("calories"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getRunningCaloriesBurnt()}, i);
                    else if (type.equalsIgnoreCase("distance"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getRunningDistance()/1000}, i);
                    else  // Steps
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getRunningSteps()}, i);
                    if (barEntry != null)
                        mBarEntry.add(barEntry);
                    break;
                case FitnessActivities.BIKING:
                    if (type.equalsIgnoreCase("calories"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getCyclingCaloriesBurnt()}, i);
                    else if (type.equalsIgnoreCase("distance"))
                        barEntry = new BarEntry(new float[]{(float) fitnessHistory.getCyclingDistance()/1000}, i);
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
}
