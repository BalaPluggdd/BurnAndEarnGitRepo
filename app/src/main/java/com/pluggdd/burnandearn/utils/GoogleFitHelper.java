package com.pluggdd.burnandearn.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.service.FitnessSensorService;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.model.FitnessHistory;

import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by User on 28-Mar-16.
 */
public class GoogleFitHelper {

    private PreferencesManager mPreferenceManager;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    public GoogleFitHelper(Context context, GoogleApiClient googleApiClient) {
        mPreferenceManager = new PreferencesManager(context);
        mContext = context;
        mGoogleApiClient = googleApiClient;
    }

    public ArrayList<FitnessHistory> getLastWeekData() {
        ArrayList<FitnessHistory> fitnessHistories = new ArrayList<>();
        try {
            List<LocalDateTime> dates = new ArrayList<>();
            LocalDateTime StartDate = new LocalDateTime().minusDays(6).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            int days = Days.daysBetween(StartDate, new LocalDateTime()).getDays();
            Log.i("Days",days + " ");
            for (int i = 0; i < days; i++) {
                LocalDateTime d = StartDate.withFieldAdded(DurationFieldType.days(), i);
                dates.add(d);
            }
            for (LocalDateTime date : dates) {
                LocalDateTime endDayTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                Log.i("date", date.toString());
                FitnessHistory history = getFitnessActivityDetails(Fitness.HistoryApi.readData(mGoogleApiClient, getFitnessData(date.toDateTime().getMillis(), endDayTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), date, endDayTime);
                if(history != null)
                fitnessHistories.add(history);
            }
            Log.i("Fitness History",fitnessHistories.size() + " ");
            FitnessHistory history = getTodayFitnessDetails();
            if(history != null)
                fitnessHistories.add(history);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("Fitness History",fitnessHistories.size() + " ");
        return fitnessHistories;
    }

    public ArrayList<FitnessHistory> getFitnessHistoryData() {
        ArrayList<FitnessHistory> fitnessHistoryList = new ArrayList<>();
        try {
            long last_calories_updated_time = mPreferenceManager.getLongValue(mContext.getString(R.string.last_updated_calories_time));
            //last_calories_updated_time = new LocalDateTime().minusHours(20).toDateTime().getMillis();
            //last_calories_updated_time = 1459098422000L;
            if (last_calories_updated_time == 0) { // App installed today only,so send today fitnessDetails only
                FitnessHistory history = getTodayFitnessDetails();
                if(history != null)
                     fitnessHistoryList.add(history);
            } else { // Send fitness data from last synced date to current time
                List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
                LocalDateTime startDate = new LocalDateTime(last_calories_updated_time);
                LocalDateTime currentDateTime = new LocalDateTime();
                int days = Days.daysBetween(startDate.toDateTime().withTimeAtStartOfDay(), currentDateTime.toDateTime().withTimeAtStartOfDay()).getDays();
                if (days == 0) {
                    //Log.i("start date : ", startDate.toString() + " " + " end date :" + currentDateTime.toString());
                    FitnessHistory history = getFitnessActivityDetails(Fitness.HistoryApi.readData(mGoogleApiClient, getFitnessData(startDate.toDateTime().getMillis(), currentDateTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), startDate, currentDateTime);
                    if(history != null)
                      fitnessHistoryList.add(history);
                } else {
                    for (int i = 0; i < days; i++) {
                        LocalDateTime d = startDate.withFieldAdded(DurationFieldType.days(), i);
                        dates.add(d);
                    }
                    for (int i = 0; i < dates.size(); i++) {
                        LocalDateTime date = dates.get(i);
                        LocalDateTime startDateTime, endDateTime;
                        if (i == 0) {// First position as start date
                            startDateTime = date;
                        } else {
                            startDateTime = date.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        }
                        endDateTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        //Log.i("start date : ", date.toString() + " " + " end date :" + endDateTime.toString());
                        FitnessHistory history = getFitnessActivityDetails(Fitness.HistoryApi.readData(mGoogleApiClient, getFitnessData(startDateTime.toDateTime().getMillis(), endDateTime.toDateTime().getMillis())).await(1, TimeUnit.MINUTES), startDateTime, endDateTime);
                        if(history != null)
                          fitnessHistoryList.add(history);
                    }
                    FitnessHistory history = getTodayFitnessDetails();
                    if(history != null)
                    fitnessHistoryList.add(history); // To add today fitness details as last one
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fitnessHistoryList;
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

    private FitnessHistory getTodayFitnessDetails() {
        LocalDateTime currentDateTime = new LocalDateTime();
        long endTime = currentDateTime.toDateTime().getMillis();
        LocalDateTime startdateTime = currentDateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        long startTime = startdateTime.toDateTime().getMillis();
        Log.i("Time :", startdateTime.toString() + " " + currentDateTime.toDateTime().toString());
        return getFitnessActivityDetails(Fitness.HistoryApi.readData(mGoogleApiClient, getFitnessData(startTime, endTime)).await(1, TimeUnit.MINUTES), startdateTime, currentDateTime);
    }

    private FitnessHistory getFitnessActivityDetails(DataReadResult dataReadResult, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        FitnessHistory history = null;
        if (dataReadResult.getBuckets().size() > 0) {
            history = new FitnessHistory();
            ArrayList<FitnessActivity> fitnessActivitiesListofDay = new ArrayList<>();
            //Log.e("Fitness data called: ", startDateTime.toString() + " " + dataReadResult.getBuckets().size());
            double total_walking_calories_of_day = 0, total_running_calories_of_day = 0, total_cycling_calories_of_day = 0,
                    total_walking_distance_of_day = 0, total_running_distance_of_day = 0, total_cycling_distance_of_day = 0,
                    total_walking_steps_of_day = 0, total_running_steps_of_day = 0;
            for (Bucket bucket : dataReadResult.getBuckets()) {
                /*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
               Log.i("Days : ", bucket.getStartTime(TimeUnit.DAYS) + " " + bucket.getEndTime(TimeUnit.DAYS) + " " );
                Log.i("DAY ", "\tStart: " + dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i("DAY ", "\tEnd: " + dateFormat.format(bucket.getEndTime(TimeUnit.MILLISECONDS)));*/
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
        }
        // [END parse_read_data_result]
        return  history;
    }

}
