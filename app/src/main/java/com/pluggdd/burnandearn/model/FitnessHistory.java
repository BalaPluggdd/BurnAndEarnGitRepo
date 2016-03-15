package com.pluggdd.burnandearn.model;

import java.util.ArrayList;

/**
 * Created by User on 14-Mar-16.
 */
public class FitnessHistory {

    private String startDateTime,endDateTime;

    private ArrayList<FitnessActivity> fitnessActivitiesList;

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public ArrayList<FitnessActivity> getFitnessActivitiesList() {
        return fitnessActivitiesList;
    }

    public void setFitnessActivitiesList(ArrayList<FitnessActivity> fitnessActivitiesList) {
        this.fitnessActivitiesList = fitnessActivitiesList;
    }
}
