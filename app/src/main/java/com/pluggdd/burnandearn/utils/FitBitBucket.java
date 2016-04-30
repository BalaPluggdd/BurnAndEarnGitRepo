package com.pluggdd.burnandearn.utils;

import com.pluggdd.burnandearn.model.FitnessActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 29-Apr-16.
 */
public class FitBitBucket {

    public ArrayList<FitnessActivity> dayActivityList = new ArrayList<FitnessActivity>();

    public HashMap<String, FitBitBucket> map = new HashMap<String, FitBitBucket>();

    public void addActivity(FitnessActivity fitnessActivity) {
        String tempDate = fitnessActivity.getStartDate();
        FitBitBucket correspondingBucket = map.get(tempDate);
        if (correspondingBucket == null) {
            correspondingBucket = new FitBitBucket();
            map.put(tempDate, correspondingBucket);
        }
        correspondingBucket.dayActivityList.add(fitnessActivity);
    }
}
