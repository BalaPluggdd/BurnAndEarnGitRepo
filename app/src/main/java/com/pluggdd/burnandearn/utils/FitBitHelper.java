package com.pluggdd.burnandearn.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.FitBitActivity;
import com.pluggdd.burnandearn.data.SqliteDatabaseHelper;
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.model.FitnessHistory;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by User on 16-Apr-16.
 */
public class FitBitHelper {

    private Activity mContext;
    private PreferencesManager mPreferenceManager;
    private SqliteDatabaseHelper mDatabaseHelper;
    private OAuthService mOAuthSevice;
    private Gson mGson;
    public static final String ACTIVITY_URL = "https://api.fitbit.com/1/user/-/activities/list.json?afterDate=";
    boolean forCheck = true;
    private FitBitBucket mFitBitBucket;

    public FitBitHelper(Activity context, OAuthService service) {
        mContext = context;
        mPreferenceManager = new PreferencesManager(context);
        mOAuthSevice = service;
        mDatabaseHelper = SqliteDatabaseHelper.getInstance(context);
        mDatabaseHelper.deleteLastWeekData(FitnessSource.FITBIT.getId());
        ///////////////////////////////////////////////////////
        //mDatabaseHelper.deleteAllData(FitnessSource.FITBIT.getId());
        /////////////////////////////////////////////////////////
        mGson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        mFitBitBucket = new FitBitBucket();
    }

    public ArrayList<FitnessHistory> getLastWeekFitbitData() {
        ArrayList<FitnessHistory> fitnessHistories = new ArrayList<>();
        List<LocalDateTime> dates = new ArrayList<>();
        LocalDateTime StartDate = new LocalDateTime().minusDays(6).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        String startDate;
        if (mDatabaseHelper.checkIfFitbitDataAvailable()) {
            startDate = mDatabaseHelper.getLastUpdatedFitbitEntry().replace(" ", "T");
        } else {
            startDate = StartDate.toString("yyyy-MM-dd");
        }
        boolean fitBitRequestStatus = getFitbitActivityListData(ACTIVITY_URL + startDate + "&sort=asc&offset=0&limit=20", "for_graph");
        if (fitBitRequestStatus == true) {
            for (FitnessActivity activity : mDatabaseHelper.getFitbitFitnessData()) {
                Log.i("inserted data", mGson.toJson(activity, FitnessActivity.class));
            }
            int days = Days.daysBetween(StartDate, new LocalDateTime()).getDays();
            Log.i("Days", days + " ");
            for (int i = 0; i < days; i++) {
                LocalDateTime d = StartDate.withFieldAdded(DurationFieldType.days(), i);
                dates.add(d);
            }
            dates.add(new LocalDateTime());
            for (int i = 0; i < dates.size(); i++) {
                LocalDateTime date = dates.get(i);
                LocalDateTime startDateTime = date.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                LocalDateTime endDateTime = startDateTime.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                fitnessHistories.add(mDatabaseHelper.getFitnessHistory(startDateTime.toString("yyyy-MM-dd"), startDateTime, endDateTime));
            }
        } else {
            // Want to delete all entries
            mDatabaseHelper.deleteAllData(FitnessSource.FITBIT.getId());
        }
        for (FitnessHistory activity : fitnessHistories) {
            Log.i("Result data", mGson.toJson(activity, FitnessHistory.class));
        }
        if (mDatabaseHelper.getLastUpdatedFitbitEntry() != null)
            Log.i("Last Updated", mDatabaseHelper.getLastUpdatedFitbitEntry());
        else
            Log.i("Last Updated", "Empty");
        return fitnessHistories;
    }

    public ArrayList<FitnessHistory> getFitbitHistory() {
        ArrayList<FitnessHistory> fitbitFitnessHistory = new ArrayList<>();
        long last_calories_updated_time = mPreferenceManager.getLongValue(mContext.getString(R.string.last_updated_calories_time));
        LocalDateTime currentDateTime = new LocalDateTime();
        LocalDateTime startDateTime = currentDateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        //last_calories_updated_time = new LocalDateTime().minusHours(20).toDateTime().getMillis();
        //last_calories_updated_time = 1459098422000L;
        boolean fitBitRequestStatus;
        if (last_calories_updated_time == 0) { // App installed today only,so send today fitnessDetails only
            fitBitRequestStatus = getFitbitActivityListData(ACTIVITY_URL + currentDateTime.toString("yyyy-MM-dd") + "&sort=asc&offset=0&limit=20", "for_api");
            if (fitBitRequestStatus) {
                if (mFitBitBucket.map != null) {
                    fitbitFitnessHistory.add(groupDailyActivity(mFitBitBucket.map.get(startDateTime.toString("yyyy-MM-dd")).dayActivityList, startDateTime, currentDateTime));
                } else {
                    fitbitFitnessHistory.add(groupDailyActivity(new ArrayList<FitnessActivity>(), startDateTime, currentDateTime));
                }
            } else {
                return null;
            }
        } else if (last_calories_updated_time != -1) { // Send fitness data from last synced date to current time
            LocalDateTime startDate = new LocalDateTime(last_calories_updated_time);
            fitBitRequestStatus = getFitbitActivityListData(ACTIVITY_URL + startDate.toString("yyyy-MM-dd'T'HH:mm:ss") + "&sort=asc&offset=0&limit=20", "for_api");
            if (fitBitRequestStatus) {
                List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
                int days = Days.daysBetween(startDate.toDateTime().withTimeAtStartOfDay(), currentDateTime.toDateTime().withTimeAtStartOfDay()).getDays();
                if (days == 0) {
                    if (mFitBitBucket.map != null && mFitBitBucket.map.containsKey(startDate.toString("yyyy-MM-dd"))) {
                        fitbitFitnessHistory.add(groupDailyActivity(mFitBitBucket.map.get(startDate.toString("yyyy-MM-dd")).dayActivityList, startDate, currentDateTime));
                    } else {
                        fitbitFitnessHistory.add(groupDailyActivity(new ArrayList<FitnessActivity>(), startDate, currentDateTime));
                    }
                } else if (last_calories_updated_time != -1) {
                    for (int i = 0; i < days; i++) {
                        LocalDateTime d = startDate.withFieldAdded(DurationFieldType.days(), i);
                        dates.add(d);
                    }
                    for (int i = 0; i < dates.size(); i++) {
                        LocalDateTime date = dates.get(i);
                        LocalDateTime endDateTime;
                        if (i == 0) {// First position as start date
                            startDateTime = date;
                        } else { // Else from midnight as start date
                            startDateTime = date.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        }
                        endDateTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        //Log.i("start date : ", date.toString() + " " + " end date :" + endDateTime.toString());
                        if (mFitBitBucket.map != null && mFitBitBucket.map.containsKey(startDateTime.toString("yyyy-MM-dd"))) {
                            fitbitFitnessHistory.add(groupDailyActivity(mFitBitBucket.map.get(startDateTime.toString("yyyy-MM-dd")).dayActivityList, startDateTime, endDateTime));
                        } else {
                            fitbitFitnessHistory.add(groupDailyActivity(new ArrayList<FitnessActivity>(), startDateTime, endDateTime));
                        }
                    }
                    startDateTime = currentDateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0); // Current date
                    if (mFitBitBucket.map != null && mFitBitBucket.map.containsKey(currentDateTime.toString("yyyy-MM-dd"))) {
                        fitbitFitnessHistory.add(groupDailyActivity(mFitBitBucket.map.get(currentDateTime.toString("yyyy-MM-dd")).dayActivityList, startDateTime, currentDateTime));
                    } else {
                        fitbitFitnessHistory.add(groupDailyActivity(new ArrayList<FitnessActivity>(), startDateTime, currentDateTime));
                    }
                }
            } else {
                return null;
            }
        }
        for (FitnessHistory activity : fitbitFitnessHistory) {
            Log.i("History", mGson.toJson(activity, FitnessHistory.class));
        }
        return fitbitFitnessHistory;
    }

    private FitnessHistory groupDailyActivity(ArrayList<FitnessActivity> fitnessActivitiesList, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        double walking_calories_of_day = 0, walking_distance_of_day = 0, running_calories_of_day = 0,
                running_distance_of_day = 0, cycling_calories_of_day = 0, cycling_distance_of_day = 0;
        int walking_steps_of_day = 0, running_steps_of_day = 0;
        for (FitnessActivity activity : fitnessActivitiesList) {
            switch (activity.getName()) {
                case FitnessActivities.WALKING:
                    walking_calories_of_day += activity.getCalories_expended();
                    walking_distance_of_day += activity.getDistance();
                    walking_steps_of_day += activity.getStep_count();
                    break;
                case FitnessActivities.RUNNING:
                    running_calories_of_day += activity.getCalories_expended();
                    running_distance_of_day += activity.getDistance();
                    running_steps_of_day += activity.getStep_count();
                    break;
                case FitnessActivities.BIKING:
                    cycling_calories_of_day += activity.getCalories_expended();
                    cycling_distance_of_day += activity.getDistance();
                    break;
            }
        }
        FitnessHistory fitnessHistory = new FitnessHistory();
        fitnessHistory.setWalkingCaloriesBurnt(walking_calories_of_day);
        fitnessHistory.setWalkingDistance(walking_distance_of_day);
        fitnessHistory.setWalkingSteps(walking_steps_of_day);
        fitnessHistory.setRunningCaloriesBurnt(running_calories_of_day);
        fitnessHistory.setRunningDistance(running_distance_of_day);
        fitnessHistory.setRunningSteps(running_steps_of_day);
        fitnessHistory.setCyclingCaloriesBurnt(cycling_calories_of_day);
        fitnessHistory.setCyclingDistance(cycling_distance_of_day);
        fitnessHistory.setStartDateTime(startDateTime);
        fitnessHistory.setEndDateTime(endDateTime);
        ArrayList<FitnessActivity> fitnessActivityList = new ArrayList<>();
        // Walking
        FitnessActivity walkingActivity = new FitnessActivity();
        walkingActivity.setName(FitnessActivities.WALKING);
        walkingActivity.setCalories_expended(walking_calories_of_day);
        walkingActivity.setDistance(walking_distance_of_day);
        walkingActivity.setStep_count(walking_steps_of_day);
        // Running
        FitnessActivity runningActivity = new FitnessActivity();
        runningActivity.setName(FitnessActivities.RUNNING);
        runningActivity.setCalories_expended(running_calories_of_day);
        runningActivity.setDistance(running_distance_of_day);
        runningActivity.setStep_count(running_steps_of_day);
        // Biking
        FitnessActivity bikingActivity = new FitnessActivity();
        bikingActivity.setName(FitnessActivities.BIKING);
        bikingActivity.setCalories_expended(cycling_calories_of_day);
        bikingActivity.setDistance(cycling_distance_of_day);
        fitnessActivityList.add(walkingActivity);
        fitnessActivityList.add(runningActivity);
        fitnessActivityList.add(bikingActivity);
        fitnessHistory.setFitnessActivitiesList(fitnessActivityList);
        return fitnessHistory;
    }



    /*public ArrayList<FitnessHistory> getLastWeekData() {
        ArrayList<FitnessHistory> fitnessHistories = new ArrayList<>();
        List<LocalDateTime> dates = new ArrayList<>();
        LocalDateTime StartDate = new LocalDateTime().minusDays(6).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        int days = Days.daysBetween(StartDate, new LocalDateTime()).getDays();
        Log.i("Days", days + " ");
        for (int i = 0; i < days; i++) {
            LocalDateTime d = StartDate.withFieldAdded(DurationFieldType.days(), i);
            dates.add(d);
        }
        dates.add(new LocalDateTime());
        Log.i("Days", dates.size() + " ");
        for (int i = 0; i < dates.size(); i++) {
            LocalDateTime date = dates.get(i);
            LocalDateTime startDateTime = date.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            String start_date = startDateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toString("yyyy-MM-dd'T'hh:mm:ss");
            String endDate;
            FitnessHistory history;
            if (i == dates.size() - 1) { // Current date
                endDate = date.toString("yyyy-MM-dd'T'hh:mm:ss");
                history = getFitBitActivityData(date.toString("yyyy-MM-dd"), startDateTime, date);
                if (history != null) {
                    if (mDatabaseHelper.checkifCurrentDateActivityLogAvailable(FitnessSource.FITBIT.getId(), start_date)) {
                        Log.i("status", "update");
                        mDatabaseHelper.updateActivityLog(FitnessSource.FITBIT.getId(), mGson.toJson(history, FitnessHistory.class), start_date, endDate);
                    } else {
                        Log.i("status", "insert");
                        mDatabaseHelper.insertFitnessActivity(FitnessSource.FITBIT.getId(), mGson.toJson(history, FitnessHistory.class), start_date, endDate);
                    }
                }
            } else { // Previous date
                LocalDateTime endDateTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                endDate = endDateTime.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toString("yyyy-MM-dd'T'hh:mm:ss");
                if (mDatabaseHelper.checkifPreviousDateActivityLogAvailable(FitnessSource.FITBIT.getId(), endDate)) {
                    Log.i("Date: " + endDate, "Available already");
                } else {
                    Log.i("date", date.toString());
                    history = getFitBitActivityData(date.toString("yyyy-MM-dd"), startDateTime, endDateTime);
                    if (history != null) {
                        if (mDatabaseHelper.checkifCurrentDateActivityLogAvailable(FitnessSource.FITBIT.getId(), start_date)) {
                            Log.i("status", "update");
                            mDatabaseHelper.updateActivityLog(FitnessSource.FITBIT.getId(), mGson.toJson(history, FitnessHistory.class), start_date, endDate);
                        } else {
                            Log.i("status", "insert");
                            mDatabaseHelper.insertFitnessActivity(FitnessSource.FITBIT.getId(), mGson.toJson(history, FitnessHistory.class), start_date, endDate);
                        }
                    }
                }
            }
        }
        fitnessHistories = mDatabaseHelper.getFitnessData();
        for (FitnessHistory activity : fitnessHistories) {
            Log.i("inserted data", mGson.toJson(activity, FitnessHistory.class));
        }
        return fitnessHistories;
    }*/

   /* public ArrayList<FitnessHistory> getFitnessHistoryData() {
        ArrayList<FitnessHistory> fitnessHistoryList = new ArrayList<>();
        try {
            long last_calories_updated_time = mPreferenceManager.getLongValue(mContext.getString(R.string.last_updated_calories_time));
            LocalDateTime currentDateTime = new LocalDateTime();
            LocalDateTime startDateTime = currentDateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            //last_calories_updated_time = new LocalDateTime().minusHours(20).toDateTime().getMillis();
            //last_calories_updated_time = 1459098422000L;
            if (last_calories_updated_time == 0) { // App installed today only,so send today fitnessDetails only
                FitnessHistory history = getFitBitActivityData(currentDateTime.toString("yyyy-MM-dd"), startDateTime, currentDateTime);
                if (history != null)
                    fitnessHistoryList.add(history);
            } else if (last_calories_updated_time != -1) { // Send fitness data from last synced date to current time
                List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
                LocalDateTime startDate = new LocalDateTime(last_calories_updated_time);
                int days = Days.daysBetween(startDate.toDateTime().withTimeAtStartOfDay(), currentDateTime.toDateTime().withTimeAtStartOfDay()).getDays();
                if (days == 0) {
                    //Log.i("start date : ", startDate.toString() + " " + " end date :" + currentDateTime.toString());
                    FitnessHistory history = getFitBitActivityData(currentDateTime.toString("yyyy-MM-dd"), startDateTime, currentDateTime);
                    if (history != null)
                        fitnessHistoryList.add(history);
                } else {
                    for (int i = 0; i < days; i++) {
                        LocalDateTime d = startDate.withFieldAdded(DurationFieldType.days(), i);
                        dates.add(d);
                    }
                    for (int i = 0; i < dates.size(); i++) {
                        LocalDateTime date = dates.get(i);
                        LocalDateTime endDateTime;
                        if (i == 0) {// First position as start date
                            startDateTime = date;
                        } else {
                            startDateTime = date.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        }
                        endDateTime = date.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                        //Log.i("start date : ", date.toString() + " " + " end date :" + endDateTime.toString());
                        FitnessHistory history = getFitBitActivityData(currentDateTime.toString("yyyy-MM-dd"), startDateTime, endDateTime);
                        if (history != null)
                            fitnessHistoryList.add(history);
                    }
                    startDateTime = currentDateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0); // Current date
                    FitnessHistory history = getFitBitActivityData(currentDateTime.toString("yyyy-MM-dd"), startDateTime, currentDateTime);
                    if (history != null)
                        fitnessHistoryList.add(history); // To add today fitness details as last one
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fitnessHistoryList;
    }*/

    private boolean getFitbitActivityListData(String activityUrl, String parseFlag) { // ParseFlag - "for_graph" or "for_api"
        String accessToken = mPreferenceManager.getStringValue(mContext.getString(R.string.access_token));
        Response response = callFitBitAPI(activityUrl, accessToken);
        Log.i("response_activity", "Response : " + response.getBody());
        if (response != null) {
            try {
                JSONObject responseObject = new JSONObject(response.getBody());
                if (checkifAccessTokenExpired(responseObject)/*forCheck*/) {
                    //forCheck = false;
                    String refresh_token_status = refreshAccessToken();
                    if (refresh_token_status.equalsIgnoreCase("success")) {
                        accessToken = mPreferenceManager.getStringValue(mContext.getString(R.string.access_token));
                        response = callFitBitAPI(activityUrl, accessToken);
                    } else {
                        return false;
                    }
                }
                Log.i("response_activity", "Response : " + response.getBody());
                responseObject = new JSONObject(response.getBody());
                String nextPaginationUrl = parseFitbitData(responseObject, parseFlag);
                if (TextUtils.isEmpty(nextPaginationUrl)) {
                    return true;
                } else if (nextPaginationUrl.equalsIgnoreCase("failure")) {
                    return false;
                } else {
                    return getFitbitActivityListData(nextPaginationUrl, parseFlag);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    private String parseFitbitData(JSONObject response, String parseFlag) {
        try {
            ArrayList<FitnessHistory> fitnessHistoriesList = new ArrayList<>();
            JSONArray activities_array = response.optJSONArray("activities");
            if (activities_array != null && activities_array.length() > 0) {
                for (int i = 0; i < activities_array.length(); i++) {
                    JSONObject activities_obj = activities_array.getJSONObject(i);
                    String logType = activities_obj.optString("logType");
                    if (logType.equalsIgnoreCase("auto_detected") /*|| logType.equalsIgnoreCase("manual")*/) {
                        FitnessActivity fitnessActivity = new FitnessActivity();
                        int activityTypeID = activities_obj.optInt("activityTypeId");
                        String startDateTimeString = activities_obj.optString("startTime");
                        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        DateTime startDateTime = dateTimeFormatter.withOffsetParsed().parseDateTime(startDateTimeString);
                        int duration = activities_obj.optInt("duration", 0);
                        DateTime endDateTime = startDateTime.plusMillis(duration);
                        switch (activityTypeID) {
                            case 90013: // Walk
                                fitnessActivity.setName(FitnessActivities.WALKING);
                                fitnessActivity.setCalories_expended(activities_obj.optDouble("calories"));
                                fitnessActivity.setDistance(activities_obj.optDouble("distance"));
                                fitnessActivity.setStep_count(activities_obj.optInt("steps"));
                                break;
                            case 90009: // Run
                                fitnessActivity.setName(FitnessActivities.RUNNING);
                                fitnessActivity.setCalories_expended(activities_obj.optDouble("calories"));
                                fitnessActivity.setDistance(activities_obj.optDouble("distance"));
                                fitnessActivity.setStep_count(activities_obj.optInt("steps"));
                                break;
                            case 90001: // Cycling
                                fitnessActivity.setName(FitnessActivities.BIKING);
                                fitnessActivity.setCalories_expended(activities_obj.optDouble("calories"));
                                fitnessActivity.setDistance(activities_obj.optDouble("distance"));
                                fitnessActivity.setStep_count(activities_obj.optInt("steps"));
                                break;
                        }
                        fitnessActivity.setStartDate(startDateTime.toString("yyyy-MM-dd"));
                        if (parseFlag.equalsIgnoreCase("for_graph"))
                            mDatabaseHelper.insertFitnessActivity(FitnessSource.FITBIT.getId(), mGson.toJson(fitnessActivity, FitnessActivity.class), startDateTime.toString("yyyy-MM-dd'T'hh:mm:ss"), endDateTime.toString("yyyy-MM-dd'T'hh:mm:ss"));
                        else
                            mFitBitBucket.addActivity(fitnessActivity);
                    }
                }
            } else { // Update last updated datetime
                mDatabaseHelper.updateFitnessActivity(FitnessSource.FITBIT.getId());
            }
            JSONObject paginationObject = response.optJSONObject("pagination");
            return paginationObject.optString("next");
        } catch (JSONException e) {
            e.printStackTrace();
            return "failure";
        }
    }


    /*public FitnessHistory getFitBitActivityData(String start_date, LocalDateTime start_datetime, LocalDateTime end_datetime) {
        FitnessHistory history;
        String accessToken = mPreferenceManager.getStringValue(mContext.getString(R.string.access_token));
        Response response = callFitBitAPI(ACTIVITY_URL+"2016-04-14&sort=asc&offset=0&limit=20",accessToken);
        Log.i("response_activity", "Response : " + response.getBody());
        if (response != null) {
            try {
                JSONObject responseObject = new JSONObject(response.getBody());
                if (*//*checkifAccessTokenExpired(responseObject)*//*forCheck) {
                    forCheck = false;
                    String refresh_token_status = refreshAccessToken();
                    if (refresh_token_status.equalsIgnoreCase("success")) {
                        accessToken = mPreferenceManager.getStringValue(mContext.getString(R.string.access_token));
                        response = callFitBitAPI(ACTIVITY_URL+"2016-04-14&sort=asc&offset=0&limit=20",accessToken);
                    } else {
                        return null;
                    }
                }
                responseObject = new JSONObject(response.getBody());
                JSONArray activities_array = responseObject.optJSONArray("activities");
                history = new FitnessHistory();
                ArrayList<FitnessActivity> fitnessActivitiesListofDay = new ArrayList<>();
                double total_walking_calories_of_day = 0, total_walking_distance_of_day = 0, total_running_calories_of_day = 0,
                        total_running_distance_of_day = 0, total_cycling_calories_of_day = 0, total_cycling_distance_of_day = 0;
                int total_walking_steps_of_day = 0, total_running_steps_of_day = 0;
                boolean isWalkingDone = false, isRunningDone = false, isCyclingDone = false;
                for (int j = 0; j < activities_array.length(); j++) {
                    JSONObject activities_obj = activities_array.getJSONObject(j);
                    int activity_parentID = activities_obj.optInt("activityParentId");
                    if (activity_parentID == 90013 || activity_parentID == 90009 || activity_parentID == 90001) {
                        FitnessActivity activity = new FitnessActivity();
                        switch (activity_parentID) {
                            case 90013:
                                isWalkingDone = true;
                                activity.setName(FitnessActivities.WALKING);
                                total_walking_calories_of_day = activities_obj.optDouble("calories");
                                total_walking_distance_of_day = activities_obj.optDouble("distance");
                                total_walking_steps_of_day = activities_obj.optInt("steps");
                                activity.setCalories_expended(total_walking_calories_of_day);
                                activity.setDistance(total_walking_distance_of_day);
                                activity.setStep_count(total_walking_steps_of_day);
                                fitnessActivitiesListofDay.add(activity);
                                break;
                            case 90009:
                                isRunningDone = true;
                                activity.setName(FitnessActivities.RUNNING);
                                total_running_calories_of_day = activities_obj.optDouble("calories");
                                total_running_distance_of_day = activities_obj.optDouble("distance");
                                total_running_steps_of_day = activities_obj.optInt("steps");
                                activity.setCalories_expended(total_running_calories_of_day);
                                activity.setDistance(total_running_distance_of_day);
                                activity.setStep_count(total_running_steps_of_day);
                                fitnessActivitiesListofDay.add(activity);
                                break;
                            case 90001:
                                isCyclingDone = true;
                                activity.setName(FitnessActivities.BIKING);
                                total_cycling_calories_of_day = activities_obj.optDouble("calories");
                                total_cycling_distance_of_day = activities_obj.optDouble("distance");
                                activity.setCalories_expended(total_cycling_calories_of_day);
                                activity.setDistance(total_cycling_distance_of_day);
                                activity.setStep_count(0);
                                fitnessActivitiesListofDay.add(activity);
                                break;
                        }
                    }
                }
                addNonWorkoutActivities(isWalkingDone, isRunningDone, isCyclingDone, fitnessActivitiesListofDay);
                history.setStartDateTime(start_datetime);
                history.setEndDateTime(end_datetime);
                history.setWalkingCaloriesBurnt(total_walking_calories_of_day);
                history.setCyclingCaloriesBurnt(total_cycling_calories_of_day);
                history.setRunningCaloriesBurnt(total_running_calories_of_day);
                history.setWalkingDistance(total_walking_distance_of_day);
                history.setCyclingDistance(total_cycling_distance_of_day);
                history.setRunningDistance(total_running_distance_of_day);
                history.setWalkingSteps(total_walking_steps_of_day);
                history.setRunningSteps(total_running_steps_of_day);
                history.setFitnessActivitiesList(fitnessActivitiesListofDay);
                return history;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
*/
    private Response callFitBitAPI(String activityUrl, String accessToken) {
        Log.i("utl", activityUrl);
        Token token = new Token(accessToken, accessToken);
        //OAuthRequest request = new OAuthRequest(Verb.GET,"https://api.fitbit.com/1/user/-/profile.json");
        // OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.fitbit.com/1/user/-/activities/date/" + date + ".json");
        //OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.fitbit.com/1/user/-/activities/tracker/activityCalories/date/today/7d.json");
        OAuthRequest request = new OAuthRequest(Verb.GET, activityUrl);
        //OAuthRequest request = new OAuthRequest(Verb.GET,"https://api.fitbit.com/1/activities.json");
        request.addHeader("Authorization", "Bearer " + accessToken);
        mOAuthSevice.signRequest(token, request); // the access token from step
        return request.send();
    }

    private String refreshAccessToken() {
        String accessToken = mPreferenceManager.getStringValue(mContext.getString(R.string.access_token));
        Token token = new Token(accessToken, accessToken);
        OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.fitbit.com/oauth2/token");
        //OAuthRequest request = new OAuthRequest(Verb.GET,"https://api.fitbit.com/1/activities.json");
        String oauth2Credentials = mContext.getString(R.string.fit_bit_api_key) + ":" + mContext.getString(R.string.fit_bit_secret_key);
        request.addHeader("Authorization", "Basic " + new String(Base64.encode(oauth2Credentials.getBytes(), Base64.NO_WRAP)));
        request.addBodyParameter("grant_type", "refresh_token");
        request.addBodyParameter("refresh_token", mPreferenceManager.getStringValue(mContext.getString(R.string.refresh_token)));
        mOAuthSevice.signRequest(token, request); // the access token from step
        Response response = request.send();
        Log.i("access_token", "Refresh token :" + response.getBody());
        if (response != null && response.isSuccessful()) {
            try {
                JSONObject jsonObject = new JSONObject(response.getBody());
                mPreferenceManager.setStringValue(mContext.getString(R.string.access_token), jsonObject.optString("access_token"));
                mPreferenceManager.setStringValue(mContext.getString(R.string.refresh_token), jsonObject.optString("refresh_token"));
                return "success";
                //getLastWeekFitbitData();
            } catch (Exception e) {
                e.printStackTrace();
                return "failure";
            }
        } else {
            mDatabaseHelper.deleteAllData(FitnessSource.FITBIT.getId());
            mContext.startActivityForResult(new Intent(mContext, FitBitActivity.class), 104);
            return "failure";
        }
    }

    private boolean checkifAccessTokenExpired(JSONObject responseObject) {
        try {
            if (responseObject.has("errors")) {
                JSONArray errorArray = responseObject.getJSONArray("errors");
                for (int i = 0; i < errorArray.length(); i++) {
                    JSONObject errorObject = errorArray.getJSONObject(i);
                    String errorType = errorObject.optString("errorType");
                    if (errorType != null && errorType.equalsIgnoreCase("expired_token")) {
                        return true;
                    }
                }
                return false;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addNonWorkoutActivities(boolean isWalkingDone, boolean isRunningDone, boolean isCyclingDone, ArrayList<FitnessActivity> fitnessActivitiesListofDay) {
        // To add not existing activity
        if (!isWalkingDone) {
            FitnessActivity activity = new FitnessActivity();
            activity.setName(FitnessActivities.WALKING);
            activity.setCalories_expended(0);
            activity.setDistance(0);
            activity.setStep_count(0);
            fitnessActivitiesListofDay.add(activity);
        }
        if (!isRunningDone) {
            FitnessActivity activity = new FitnessActivity();
            activity.setName(FitnessActivities.RUNNING);
            activity.setCalories_expended(0);
            activity.setDistance(0);
            activity.setStep_count(0);
            fitnessActivitiesListofDay.add(activity);
        }
        if (!isCyclingDone) {
            FitnessActivity activity = new FitnessActivity();
            activity.setName(FitnessActivities.BIKING);
            activity.setCalories_expended(0);
            activity.setDistance(0);
            activity.setStep_count(0);
            fitnessActivitiesListofDay.add(activity);
        }
    }
}
