package com.pluggdd.burnandearn.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.maps.internal.StreetViewLifecycleDelegate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.model.FitnessHistory;
import com.pluggdd.burnandearn.utils.FitnessSource;

import org.joda.time.LocalDateTime;
import org.joda.time.convert.Converter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

/**
 * Database helper class to manage fitness activities log data
 */
public class SqliteDatabaseHelper extends SQLiteOpenHelper {

    public static SqliteDatabaseHelper sInstance;
    private static final String FITNESS_HISTORY_DB = "fitness_history.db";
    private static final String TABLE_FITNESS_ACTIVITY = "fitness_activity_log";
    private static final String TABLE_FITNESS_SOURCE = "fitness_source";
    private static final int DATABASE_VERSION = 1;
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SOURCE = "fitness_source";
    public static final String COLUMN_FITNESS_ACTIVITY_LOG = "activity_log";
    public static final String COLUMN_START_DATETIME = "start_datetime";
    public static final String COLUMN_END_DATETIME = "end_datetime";
    public static final String COLUMN_UPDATED_DATETIME = "updated_datetime";

    public static final String CREATE_TABLE_FITNESS_SOURCE = "CREATE TABLE " + TABLE_FITNESS_SOURCE + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_SOURCE + " TEXT)";

    public static final String CREATE_TABLE_FITNESS_ACTIVITY = "CREATE TABLE " +
            TABLE_FITNESS_ACTIVITY + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_SOURCE + " INTEGER," + COLUMN_START_DATETIME + " DATETIME,"
            + COLUMN_END_DATETIME + " DATETIME," + COLUMN_FITNESS_ACTIVITY_LOG + " TEXT," + COLUMN_UPDATED_DATETIME + " DATETIME DEFAULT (datetime('now', 'localtime')),FOREIGN KEY(" + COLUMN_SOURCE + ") REFERENCES " + TABLE_FITNESS_SOURCE + "(" + COLUMN_ID + "))";

    public static synchronized SqliteDatabaseHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new SqliteDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private SqliteDatabaseHelper(Context context) {
        super(context, FITNESS_HISTORY_DB, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FITNESS_SOURCE);
        db.execSQL(CREATE_TABLE_FITNESS_ACTIVITY);
        insertFitnessSource(db, FitnessSource.GOOGLE_FIT.getId(), FitnessSource.GOOGLE_FIT.name());
        insertFitnessSource(db, FitnessSource.FITBIT.getId(), FitnessSource.FITBIT.name());
    }

    private void insertFitnessSource(SQLiteDatabase database, int id, String source) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, id);
        contentValues.put(COLUMN_SOURCE, source);
        database.insert(TABLE_FITNESS_SOURCE, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertFitnessActivity(int fitness_source, String fitness_activity_json, String startDateTime, String endDateTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_SOURCE, fitness_source);
        contentValues.put(COLUMN_FITNESS_ACTIVITY_LOG, fitness_activity_json);
        contentValues.put(COLUMN_START_DATETIME, startDateTime);
        contentValues.put(COLUMN_END_DATETIME, endDateTime);
        long insert_status = -1;
        SQLiteDatabase database = getWritableDatabase();
        insert_status = database.insert(TABLE_FITNESS_ACTIVITY, null, contentValues);
        database.close();
        return insert_status;
    }

    public ArrayList<FitnessHistory> getFitnessData() {
        ArrayList<FitnessHistory> fitnessActivityList = new ArrayList<>();
        Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        Cursor cursor = getReadableDatabase().query(true, TABLE_FITNESS_ACTIVITY, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                FitnessHistory activity = gson.fromJson(cursor.getString(cursor.getColumnIndex(COLUMN_FITNESS_ACTIVITY_LOG)), FitnessHistory.class);
                fitnessActivityList.add(activity);
            } while (cursor.moveToNext());
        }
        return fitnessActivityList;
    }

    public boolean checkIfFitbitDataAvailable() {
        Cursor cursor = getReadableDatabase().query(true, TABLE_FITNESS_ACTIVITY, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            if (cursor.getCount() > 0)
                return true;
            else
                return false;
        }
        return false;
    }

    public String getLastUpdatedFitbitEntry() {
        Cursor cursor = getReadableDatabase().query(true, TABLE_FITNESS_ACTIVITY, null, null, null, null, null, null, null);
        if (cursor.moveToLast()) {
            return cursor.getString(cursor.getColumnIndex(COLUMN_UPDATED_DATETIME));
        }
        return null;
    }

    public void updateFitnessActivity(int fitness_source) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("UPDATE " + TABLE_FITNESS_ACTIVITY + " SET " + COLUMN_UPDATED_DATETIME + "=datetime('now', 'localtime') WHERE " + COLUMN_SOURCE + "=?", new String[]{String.valueOf(fitness_source)});
        database.close();
    }

    public ArrayList<FitnessActivity> getFitbitFitnessData() {
        ArrayList<FitnessActivity> fitnessActivityList = new ArrayList<>();
        Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        Cursor cursor = getReadableDatabase().query(true, TABLE_FITNESS_ACTIVITY, null, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                FitnessActivity activity = gson.fromJson(cursor.getString(cursor.getColumnIndex(COLUMN_FITNESS_ACTIVITY_LOG)), FitnessActivity.class);
                fitnessActivityList.add(activity);
            } while (cursor.moveToNext());
        }
        return fitnessActivityList;
    }

    public FitnessHistory getFitnessHistory(String date, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        FitnessHistory fitnessHistory = new FitnessHistory();
        Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        Cursor cursor = getReadableDatabase().query(true, TABLE_FITNESS_ACTIVITY, null, COLUMN_SOURCE + "=? AND date(" + COLUMN_START_DATETIME + ") = ?", new String[]{String.valueOf(FitnessSource.FITBIT.getId()), date}, null, null, null, null);
        double walking_calories_of_day = 0, walking_distance_of_day = 0, running_calories_of_day = 0,
                running_distance_of_day = 0, cycling_calories_of_day = 0, cycling_distance_of_day = 0;
        int walking_steps_of_day = 0, running_steps_of_day = 0;
        if (cursor.moveToFirst()) {
            do {
                FitnessActivity activity = gson.fromJson(cursor.getString(cursor.getColumnIndex(COLUMN_FITNESS_ACTIVITY_LOG)), FitnessActivity.class);
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
            } while (cursor.moveToNext());
        }
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

    public boolean checkifPreviousDateActivityLogAvailable(int source_id, String end_date) {
        Cursor cursor = getReadableDatabase().query(TABLE_FITNESS_ACTIVITY, null, COLUMN_SOURCE + " = ? AND " + COLUMN_END_DATETIME + " LIKE ?", new String[]{String.valueOf(source_id), end_date}, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getCount() > 0 ? true : false;
        } else {
            return false;
        }
    }

    public boolean checkifCurrentDateActivityLogAvailable(int source_id, String start_date) {
        Cursor cursor = getReadableDatabase().query(TABLE_FITNESS_ACTIVITY, null, COLUMN_SOURCE + " = ? AND " + COLUMN_START_DATETIME + " LIKE ?", new String[]{String.valueOf(source_id), start_date}, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getCount() > 0 ? true : false;
        } else {
            return false;
        }
    }

    public long updateActivityLog(int fitness_source, String fitness_activity_json, String startDateTime, String endDateTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_SOURCE, fitness_source);
        contentValues.put(COLUMN_FITNESS_ACTIVITY_LOG, fitness_activity_json);
        contentValues.put(COLUMN_START_DATETIME, startDateTime);
        contentValues.put(COLUMN_END_DATETIME, endDateTime);
        long update_status = -1;
        SQLiteDatabase database = getWritableDatabase();
        update_status = database.update(TABLE_FITNESS_ACTIVITY, contentValues, COLUMN_SOURCE + " = ? AND " + COLUMN_START_DATETIME + " LIKE ?", new String[]{String.valueOf(fitness_source), startDateTime});
        database.close();
        return update_status;
    }

    public long deleteLastWeekData(int fitness_source_id) {
        long delete_status;
        SQLiteDatabase database = getWritableDatabase();
        delete_status = database.delete(TABLE_FITNESS_ACTIVITY, COLUMN_SOURCE + " = ? AND " + COLUMN_START_DATETIME + " < datetime('now', 'localtime','-6 days')", new String[]{String.valueOf(fitness_source_id)});
        Log.i("delete status", delete_status + "");
        return delete_status;
    }

    public long deleteAllData(int fitness_source_id) {
        long delete_status;
        SQLiteDatabase database = getWritableDatabase();
        delete_status = database.delete(TABLE_FITNESS_ACTIVITY, COLUMN_SOURCE + " = ?", new String[]{String.valueOf(fitness_source_id)});
        Log.i("delete status", delete_status + "");
        return delete_status;
    }
}
