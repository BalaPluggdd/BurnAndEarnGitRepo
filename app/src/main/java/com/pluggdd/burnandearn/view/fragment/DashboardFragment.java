package com.pluggdd.burnandearn.view.fragment;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountsException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.print.PageRange;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.pluggdd.burnandearn.BuildConfig;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.OfferDetailActivity;
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

/**
 * Fragment to show acitivity details and offers
 */
public class DashboardFragment extends Fragment {

    // Initialization of views and variables
    private final int REQUEST_CODE_RESOLUTION = 100, REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE = 101, REQUEST_GETACCOUNTS_PERMISSIONS_REQUEST_CODE = 102, REQUEST_BOTH_PERMISSION_CODE = 103;
    private FragmentInteraction mListener;
    private View mView;
    private DecoView mCircularProgressDecoView;
    private ViewPager mActivitiesViewPager;
    private AutoScrollViewPager mBusinessOfferListViewPager;
    private CardView mBusinessOfferListPagerContainer;
    private RelativeLayout mViewPagerIndicatorContainer, mWalkingActivityContainer, mRunningActivityContainer, mBikingActivityContainer,mOfferListProgressBarContainer;
    private ImageView mViewPagerIndicator1Image, mViewPagerIndicator2Image, mViewPagerIndicator3Image;
    private Button mRedeemButton;
    private ProgressBar mActivitiesProgressBar, mCyclingActivityProgressBar, mWalkingActivityProgressBar, mRunningActivityProgressBar,mProfileImageProgressBar;
    private TextView mWalkingActivityValueText, mRunningActivityValueText, mCyclingActivityValueText, mWalkingActivityDimesionText, mRunningActivityDimensionText, mCyclingActivityDimensionText;
    private View mWalkingActivityBorder,mRunningActivityBorder,mCyclingActivityBorder;
    private ImageView mProfileImage;
    private GoogleApiClient mGoogleAPIClient;
    private ArrayList<FitnessActivity> mFitnessActivityList = new ArrayList<>(3);
    private int mTotalStepCount = 0;
    private double mTotalCaloriesExpended = 0, mTotalDistanceTravelled = 0;
    private int mBackIndex;
    private int mWalkingActivityIndex, mRunningActivityIndex, mBikingActivityIndex;
    private Spinner mDateFilterSpinner;
    private boolean mIsPermissionRequestRaised, mIsGetFitnessDataAsyncRunning;
    private PicassoImageLoaderHelper mImageLoaderHelper;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mCircularProgressDecoView = (DecoView) mView.findViewById(R.id.deco_view);
        mActivitiesViewPager = (ViewPager) mView.findViewById(R.id.activities_view_pager);
        mProfileImage = (ImageView) mView.findViewById(R.id.img_profile);
        mProfileImageProgressBar = (ProgressBar) mView.findViewById(R.id.profile_progress_bar);
        mActivitiesProgressBar = (ProgressBar) mView.findViewById(R.id.activities_progress_bar);
        mCyclingActivityProgressBar = (ProgressBar) mView.findViewById(R.id.cycling_progress_bar);
        mWalkingActivityProgressBar = (ProgressBar) mView.findViewById(R.id.walking_progress_bar);
        mRunningActivityProgressBar = (ProgressBar) mView.findViewById(R.id.running_progress_bar);
        mViewPagerIndicatorContainer = (RelativeLayout) mView.findViewById(R.id.viewpager_indicator_container);
        mViewPagerIndicator1Image = (ImageView) mView.findViewById(R.id.viewpager_indicator1_image);
        mViewPagerIndicator2Image = (ImageView) mView.findViewById(R.id.viewpager_indicator2_image);
        mViewPagerIndicator3Image = (ImageView) mView.findViewById(R.id.viewpager_indicator3_image);
        mWalkingActivityContainer = (RelativeLayout) mView.findViewById(R.id.walking_activity_container);
        mWalkingActivityValueText = (TextView) mView.findViewById(R.id.txt_walking_activity_value);
        mWalkingActivityBorder = mView.findViewById(R.id.txt_walking_activity_border);
        mRunningActivityBorder = mView.findViewById(R.id.txt_running_activity_border);
        mCyclingActivityBorder = mView.findViewById(R.id.txt_cycling_activity_border);
        mWalkingActivityDimesionText = (TextView) mView.findViewById(R.id.txt_walking_activity_dimension);
        mRunningActivityContainer = (RelativeLayout) mView.findViewById(R.id.running_activity_container);
        mRunningActivityValueText = (TextView) mView.findViewById(R.id.txt_running_activity_value);
        mRunningActivityDimensionText = (TextView) mView.findViewById(R.id.txt_running_activity_dimension);
        mBikingActivityContainer = (RelativeLayout) mView.findViewById(R.id.biking_activity_container);
        mCyclingActivityValueText = (TextView) mView.findViewById(R.id.txt_cycling_activity_value);
        mCyclingActivityDimensionText = (TextView) mView.findViewById(R.id.txt_cycling_activity_dimension);
        mBusinessOfferListPagerContainer = (CardView) mView.findViewById(R.id.business_offer_list_pager_container);
        mBusinessOfferListViewPager = (AutoScrollViewPager) mView.findViewById(R.id.business_offer_list_pager);
        mOfferListProgressBarContainer = (RelativeLayout) mView.findViewById(R.id.offer_list_progress_bar_container);
        mDateFilterSpinner = (Spinner) getActivity().findViewById(R.id.toolbar).findViewById(R.id.activities_time_spinner);
        mImageLoaderHelper = new PicassoImageLoaderHelper(getContext(),mProfileImage,mProfileImageProgressBar);
        mImageLoaderHelper.loadImage(new PreferencesManager(getContext()).getStringValue(getString(R.string.profile_image_url)));
        initializeActivitiesList();
        checkAndBuildGoogleApiClient();

       /* // Check and request for location and get accounts permission
        if (!checkLocationPermissions() && !checkGetAccountsPermissions()) {
            requestBothPermissions();
        } else if (!checkGetAccountsPermissions()) {
            requestLocationPermissions();
        } else if (!checkLocationPermissions()) {
            requestLocationPermissions();
        } else {
            buildGoogleFitnessClient();
        }*/
        // Initial set up for circular decoview
         createBackgroundSeries();
        createBikingActivitySeries();
        createRunningActivitySeries();
        createWalkingActivitySeries();


       /* mRedeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.page_flag), DashboardFragment.this.getClass().getSimpleName());
                mListener.changeFragment(bundle);
            }
        });*/

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
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        updateFitnessActivities(position);
                        break;
                    case 2:
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        updateFitnessActivities(position);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mDateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar day_start_time = Calendar.getInstance();
                Calendar end_time_calc = Calendar.getInstance();
                end_time_calc.set(Calendar.HOUR_OF_DAY, 0);
                end_time_calc.set(Calendar.MINUTE, 0);
                end_time_calc.set(Calendar.SECOND, 0);
                end_time_calc.set(Calendar.MILLISECOND, 0);
                long start_time;
                long end_time;
                switch (position) {
                    case 0: // Today
                        day_start_time.set(Calendar.HOUR_OF_DAY, 0);
                        day_start_time.set(Calendar.MINUTE, 0);
                        day_start_time.set(Calendar.SECOND, 0);
                        day_start_time.set(Calendar.MILLISECOND, 0);
                        start_time = day_start_time.getTimeInMillis();
                        end_time = Calendar.getInstance().getTimeInMillis();
                        Log.i("Fitness Async", "called from today spinner");
                        if (!mIsGetFitnessDataAsyncRunning)
                            new FitnessDataAsync().execute(start_time, end_time);
                        break;
                    case 1: // Yesterday
                        day_start_time.add(Calendar.DAY_OF_MONTH, -1);
                        day_start_time.set(Calendar.HOUR_OF_DAY, 0);
                        day_start_time.set(Calendar.MINUTE, 0);
                        day_start_time.set(Calendar.SECOND, 0);
                        day_start_time.set(Calendar.MILLISECOND, 0);
                        start_time = day_start_time.getTimeInMillis();
                        end_time = end_time_calc.getTimeInMillis();
                        Log.i("Data Check", day_start_time.getTime().toString() + " " + end_time_calc.getTime().toString());
                        new FitnessDataAsync().execute(start_time, end_time);
                        break;
                    case 2: // Last Week
                        day_start_time.add(Calendar.WEEK_OF_MONTH, -1);
                        day_start_time.set(Calendar.HOUR_OF_DAY, 0);
                        day_start_time.set(Calendar.MINUTE, 0);
                        day_start_time.set(Calendar.SECOND, 0);
                        day_start_time.set(Calendar.MILLISECOND, 0);
                        start_time = day_start_time.getTimeInMillis();
                        end_time = end_time_calc.getTimeInMillis();
                        Log.i("Data Check", day_start_time.getTime().toString() + " " + end_time_calc.getTime().toString());
                        new FitnessDataAsync().execute(start_time, end_time);
                        break;
                    case 3: // Last Month
                        day_start_time.add(Calendar.MONTH, -1);
                        day_start_time.set(Calendar.HOUR_OF_DAY, 0);
                        day_start_time.set(Calendar.MINUTE, 0);
                        day_start_time.set(Calendar.SECOND, 0);
                        day_start_time.set(Calendar.MILLISECOND, 0);
                        start_time = day_start_time.getTimeInMillis();
                        end_time = end_time_calc.getTimeInMillis();
                        Log.i("Data Check", day_start_time.getTime().toString() + " " + end_time_calc.getTime().toString());
                        new FitnessDataAsync().execute(start_time, end_time);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return mView;
    }

    // To reset viewpager to initial position
    private void resetActivitiesViewPager() {
        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_selected);
        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_unselected);
        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_unselected);
        updateFitnessActivities(0);
    }

    // Initial grey circle series
    private void createBackgroundSeries() {
        SeriesItem initial_grey_circle_progress = new SeriesItem.Builder(Color.parseColor("#a9a9a9"))
                .setRange(0, 100, 0)
                .build();
        mBackIndex = mCircularProgressDecoView.addSeries(initial_grey_circle_progress);
        mCircularProgressDecoView.addEvent(new DecoEvent.Builder(100).setIndex(mBackIndex).build());
    }

    // Walking activity series
    private void createWalkingActivitySeries() {
        SeriesItem walking_activity_series = new SeriesItem.Builder(Color.parseColor("#53B6CD"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .build();
        mWalkingActivityIndex = mCircularProgressDecoView.addSeries(walking_activity_series);
    }

    // Running activity series
    private void createRunningActivitySeries() {
        SeriesItem running_activity_series = new SeriesItem.Builder(Color.parseColor("#F8C900"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .build();
        mRunningActivityIndex = mCircularProgressDecoView.addSeries(running_activity_series);
    }

    // Biking activity series
    private void createBikingActivitySeries() {
        SeriesItem biking_activity_series = new SeriesItem.Builder(Color.parseColor("#B0C53C"))
                .setRange(0, 100, 0)
                .setInitialVisibility(false)
                .build();
        mBikingActivityIndex = mCircularProgressDecoView.addSeries(biking_activity_series);
    }

    // Set up activity detail collection list
    private void initializeActivitiesList() {
        for (int i = 0; i < 3; i++) {
            FitnessActivity mFitnessAtivity = new FitnessActivity();
            if (i == 0)
                mFitnessAtivity.setName(FitnessActivities.WALKING);
            else if (i == 1)
                mFitnessAtivity.setName(FitnessActivities.RUNNING);
            else
                mFitnessAtivity.setName(FitnessActivities.BIKING);
            // Default set to -1 for validation purpose
            mFitnessAtivity.setStep_count(-1);
            mFitnessAtivity.setDistance(-1);
            mFitnessAtivity.setCalories_expended(-1);
            mFitnessActivityList.add(i, mFitnessAtivity);
        }
    }

    // To update fitness acitvity details in viewpager and in main view
    private void updateFitnessActivities(int position) {
        mWalkingActivityValueText.setText("");
        mWalkingActivityDimesionText.setText("");
        mRunningActivityValueText.setText("");
        mRunningActivityDimensionText.setText("");
        mCyclingActivityValueText.setText("");
        mCyclingActivityDimensionText.setText("");
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
    }

    // To update decoview with each activity percentage
    private void updateProgressBar(int walking_percentage, int running_percentage, int biking_percentage) {
        if (biking_percentage > 0) {
            mCircularProgressDecoView.moveTo(mBikingActivityIndex, 100);
            if (running_percentage > 0) {
                mCircularProgressDecoView.moveTo(mRunningActivityIndex, running_percentage + walking_percentage);
            }
        } else if (running_percentage > 0) {
            mCircularProgressDecoView.moveTo(mRunningActivityIndex, 100);
        }
        if (walking_percentage > 0)
            mCircularProgressDecoView.moveTo(mWalkingActivityIndex, walking_percentage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE_RESOLUTION) {
            mGoogleAPIClient.connect();
        }
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteraction) {
            mListener = (FragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                    return TotalCaloriesBurnedFragment.newInstance(mTotalCaloriesExpended, mDateFilterSpinner.getSelectedItem().toString());
                case 1:
                    return TotalDistanceTravelledFragment.newInstance(mTotalDistanceTravelled, mDateFilterSpinner.getSelectedItem().toString());
                case 2:
                    return TotalStepsCountFragment.newInstance(mTotalStepCount, mDateFilterSpinner.getSelectedItem().toString());

            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    // To build Google api client
    private void buildGoogleFitnessClient() {
        mGoogleAPIClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Fitness.HISTORY_API)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        String email = Plus.AccountApi.getAccountName(mGoogleAPIClient);
                        //Toast.makeText(getContext(), email,Toast.LENGTH_SHORT).show();
                        long end_time = Calendar.getInstance().getTimeInMillis();
                        Calendar day_start_time = Calendar.getInstance();
                        day_start_time.set(Calendar.HOUR_OF_DAY, 0);
                        day_start_time.set(Calendar.MINUTE, 0);
                        day_start_time.set(Calendar.SECOND, 0);
                        day_start_time.set(Calendar.MILLISECOND, 0);
                        long start_time = day_start_time.getTimeInMillis();
                        if (mDateFilterSpinner.getSelectedItemPosition() == 0) {
                            Log.i("Fitness Async", "called from onconnected callback");
                            if (!mIsGetFitnessDataAsyncRunning)
                                new FitnessDataAsync().execute(start_time, end_time);
                        } else {
                            mDateFilterSpinner.setSelection(0, true);
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
    }


    public class FitnessDataAsync extends AsyncTask<Long, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsGetFitnessDataAsyncRunning = true;
            mTotalCaloriesExpended = 0;
            mTotalDistanceTravelled = 0;
            mTotalStepCount = 0;
            mFitnessActivityList.clear();
            initializeActivitiesList();
            mActivitiesProgressBar.setVisibility(View.VISIBLE);
            mCyclingActivityProgressBar.setVisibility(View.VISIBLE);
            mWalkingActivityProgressBar.setVisibility(View.VISIBLE);
            mRunningActivityProgressBar.setVisibility(View.VISIBLE);
            mActivitiesViewPager.setAdapter(new CustomPagerAdapter(getChildFragmentManager()));
            mActivitiesViewPager.setVisibility(View.GONE);
            mViewPagerIndicatorContainer.setVisibility(View.GONE);
            mWalkingActivityValueText.setVisibility(View.GONE);
            mWalkingActivityDimesionText.setVisibility(View.GONE);
            mRunningActivityValueText.setVisibility(View.GONE);
            mRunningActivityDimensionText.setVisibility(View.GONE);
            mCyclingActivityValueText.setVisibility(View.GONE);
            mCyclingActivityDimensionText.setVisibility(View.GONE);
            mWalkingActivityBorder.setVisibility(View.GONE);
            mRunningActivityBorder.setVisibility(View.GONE);
            mCyclingActivityBorder.setVisibility(View.GONE);
            mOfferListProgressBarContainer.setVisibility(View.VISIBLE);
            mBusinessOfferListPagerContainer.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(Long... params) {
            if (mGoogleAPIClient != null) {
                getFitnessActivityDetails(Fitness.HistoryApi.readData(mGoogleAPIClient, getFitnessData(params[0], params[1])).await(1, TimeUnit.MINUTES));
                return "success";
            } else
                return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mActivitiesProgressBar.setVisibility(View.GONE);
            mCyclingActivityProgressBar.setVisibility(View.GONE);
            mWalkingActivityProgressBar.setVisibility(View.GONE);
            mRunningActivityProgressBar.setVisibility(View.GONE);
            mActivitiesViewPager.setVisibility(View.VISIBLE);
            mViewPagerIndicatorContainer.setVisibility(View.VISIBLE);
            mWalkingActivityValueText.setVisibility(View.VISIBLE);
            mWalkingActivityDimesionText.setVisibility(View.VISIBLE);
            mRunningActivityValueText.setVisibility(View.VISIBLE);
            mRunningActivityDimensionText.setVisibility(View.VISIBLE);
            mCyclingActivityValueText.setVisibility(View.VISIBLE);
            mCyclingActivityDimensionText.setVisibility(View.VISIBLE);
            mWalkingActivityBorder.setVisibility(View.VISIBLE);
            mRunningActivityBorder.setVisibility(View.VISIBLE);
            mCyclingActivityBorder.setVisibility(View.VISIBLE);
            mOfferListProgressBarContainer.setVisibility(View.GONE);
            mBusinessOfferListPagerContainer.setVisibility(View.VISIBLE);
            mActivitiesViewPager.setAdapter(new CustomPagerAdapter(getChildFragmentManager()));
            mBusinessOfferListViewPager.setAdapter(new CustomBusinessOfferListAdapter());
            mBusinessOfferListViewPager.setInterval(5000);
            mBusinessOfferListViewPager.startAutoScroll(5000);
            Log.i("viewpager position", " " + mActivitiesViewPager.getCurrentItem());
            if (mActivitiesViewPager.getCurrentItem() == 0)
                resetActivitiesViewPager();
            else {
                mActivitiesViewPager.setCurrentItem(0, true);
                mCircularProgressDecoView.executeReset();
                createBackgroundSeries();
                createBikingActivitySeries();
                createRunningActivitySeries();
                createWalkingActivitySeries();
                resetActivitiesViewPager();
            }
            mIsGetFitnessDataAsyncRunning = false;
            /*Log.i("viewpager position", " " + mActivitiesViewPager.getCurrentItem());
            for (FitnessActivity activity : mFitnessActivityList) {
                Log.e("Nmae", activity.getName());
                Log.e("calories", activity.getCalories_expended() + "");
                Log.e("distaance", activity.getDistance() + "");
                Log.e("stepcount", activity.getStep_count() + "");
            }
            Log.e("Total calories", mTotalCaloriesExpended + "");
            Log.e("Total distaance", mTotalDistanceTravelled + "");
            Log.e("Total stepcount", mTotalStepCount + "");*/
        }
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

    private void getFitnessActivityDetails(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        mTotalStepCount = 0;
        mTotalCaloriesExpended = 0;
        mTotalDistanceTravelled = 0;

        if (dataReadResult.getBuckets().size() > 0) {

            /*Log.i("FITNESS RESULT", "Number of returned buckets of DataSets is: "
                    + dataReadResult.getBuckets().size());*/
            for (Bucket bucket : dataReadResult.getBuckets()) {
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
                                /*Log.i("Fields", "\tField: " + field.getName() +
                                        " Value: " + dp.getValue(field));*/
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
                        mFitnessActivityList.set(0, activity);
                    } else if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.RUNNING)) {
                        mFitnessActivityList.set(1, activity);
                    } else if (bucket.getActivity().equalsIgnoreCase(FitnessActivities.BIKING)) {
                        mFitnessActivityList.set(2, activity);
                    }


                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i("FITNESS RESULT", "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
        }
        // [END parse_read_data_result]
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
        /*if (requestCode == REQUEST_LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Toast.makeText(getContext(),"Please grant location permission for better usage of app",Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
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
                                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }else if (requestCode == REQUEST_GETACCOUNTS_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Toast.makeText(getContext(),"Please grant account permission for better usage of app",Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
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
        }else if (requestCode == REQUEST_BOTH_PERMISSION_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Toast.makeText(getContext(),"Please grant all permission for better usage of app",Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
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
                                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }*/

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
                /*if (permissionsNeeded.size() > 0) {
                    // Need Rationale
                    String message = "You need to grant access to " + permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++)
                        message = message + ", " + permissionsNeeded.get(i);
                    Snackbar.make(
                            getView(),
                            message,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Request permission
                                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), REQUEST_BOTH_PERMISSION_CODE);
                                }
                            })
                            .show();

                    return;
                }*/
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

    class CustomBusinessOfferListAdapter extends PagerAdapter{

        private LayoutInflater mLayoutInflater;

        CustomBusinessOfferListAdapter(){
            mLayoutInflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mLayoutInflater.inflate(R.layout.pager_business_offer_list,container,false);
            Button mGrabNowButton = (Button) view.findViewById(R.id.btn_grab_now);
            mGrabNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.page_flag), DashboardFragment.this.getClass().getSimpleName());
                    mListener.changeFragment(bundle);*/
                    startActivity(new Intent(getActivity(), OfferDetailActivity.class));
                }
            });
            container.addView(view);
            return view;
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


}
