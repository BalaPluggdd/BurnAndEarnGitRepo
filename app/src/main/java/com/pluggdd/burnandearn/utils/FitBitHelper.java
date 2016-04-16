package com.pluggdd.burnandearn.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.data.SqliteDatabaseHelper;
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.model.FitnessHistory;

import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by User on 16-Apr-16.
 */
public class FitBitHelper {

    private Context mContext;
    private PreferencesManager mPreferenceManager;
    private SqliteDatabaseHelper mDatabaseHelper;
    private OAuthService mOAuthSevice;
    private Gson mGson;

    public FitBitHelper(Context context, OAuthService service) {
        mContext = context;
        mPreferenceManager = new PreferencesManager(context);
        mOAuthSevice = service;
        mDatabaseHelper = SqliteDatabaseHelper.getInstance(context);
        mDatabaseHelper.deleteLastWeekData(FitnessSource.FITBIT.getId());
        mGson = Converters.registerLocalDateTime(new GsonBuilder()).create();
    }

    public ArrayList<FitnessHistory> getLastWeekData() {
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
            Log.i("inserted data", mGson.toJson(activity,FitnessHistory.class));
        }
        return fitnessHistories;
    }

    public ArrayList<FitnessHistory> getFitnessHistoryData() {
        ArrayList<FitnessHistory> fitnessHistoryList = new ArrayList<>();
        try {
            long last_calories_updated_time = mPreferenceManager.getLongValue(mContext.getString(R.string.last_updated_fitbit_calories_time));
            LocalDateTime currentDateTime = new LocalDateTime();
            LocalDateTime startDateTime = currentDateTime.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            //last_calories_updated_time = new LocalDateTime().minusHours(20).toDateTime().getMillis();
            //last_calories_updated_time = 1459098422000L;
            if (last_calories_updated_time == 0) { // App installed today only,so send today fitnessDetails only
                FitnessHistory history = getFitBitActivityData(currentDateTime.toString("yyyy-MM-dd"),startDateTime,currentDateTime);
                if(history != null)
                    fitnessHistoryList.add(history);
            } else if(last_calories_updated_time != -1){ // Send fitness data from last synced date to current time
                List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
                LocalDateTime startDate = new LocalDateTime(last_calories_updated_time);
                int days = Days.daysBetween(startDate.toDateTime().withTimeAtStartOfDay(), currentDateTime.toDateTime().withTimeAtStartOfDay()).getDays();
                if (days == 0) {
                    //Log.i("start date : ", startDate.toString() + " " + " end date :" + currentDateTime.toString());
                    FitnessHistory history = getFitBitActivityData(currentDateTime.toString("yyyy-MM-dd"),startDateTime,currentDateTime);
                    if(history != null)
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
                        FitnessHistory history = getFitBitActivityData(currentDateTime.toString("yyyy-MM-dd"),startDateTime,endDateTime);
                        if(history != null)
                            fitnessHistoryList.add(history);
                    }
                    FitnessHistory history = getFitBitActivityData(currentDateTime.toString("yyyy-MM-dd"),startDateTime,currentDateTime);
                    if(history != null)
                        fitnessHistoryList.add(history); // To add today fitness details as last one
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fitnessHistoryList;
    }

    public FitnessHistory getFitBitActivityData(String start_date, LocalDateTime start_datetime, LocalDateTime end_datetime) {
        FitnessHistory history;
        String accessToken = mPreferenceManager.getStringValue(mContext.getString(R.string.access_token));
        Response response = callFitBitAPI(accessToken, start_date);
        Log.i("response_activity", response.getBody());
        if (response != null) {
            try {
                JSONObject responseObject = new JSONObject(response.getBody());
                if (checkifAccessTokenExpired(responseObject)) {
                    String refresh_token_status = refreshAccessToken();
                    if (refresh_token_status.equalsIgnoreCase("success")) {
                        accessToken = mPreferenceManager.getStringValue(mContext.getString(R.string.access_token));
                        response = callFitBitAPI(accessToken, start_date);
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

    private Response callFitBitAPI(String accessToken, String date) {
        Token token = new Token(accessToken, accessToken);
        //OAuthRequest request = new OAuthRequest(Verb.GET,"https://api.fitbit.com/1/user/-/profile.json");
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.fitbit.com/1/user/-/activities/date/" + date + ".json");
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
                getLastWeekData();
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                return "failure";
            }
        } else {
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
