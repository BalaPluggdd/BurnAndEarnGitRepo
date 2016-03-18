package com.pluggdd.burnandearn.model;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

/**
 * Created by User on 14-Mar-16.
 */
public class FitnessHistory {

    private LocalDateTime startDateTime,endDateTime;

    private ArrayList<FitnessActivity> fitnessActivitiesList;

    private double totalCaloriesBurnt;

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public ArrayList<FitnessActivity> getFitnessActivitiesList() {
        return fitnessActivitiesList;
    }

    public void setFitnessActivitiesList(ArrayList<FitnessActivity> fitnessActivitiesList) {
        this.fitnessActivitiesList = fitnessActivitiesList;
    }

    public double getTotalCaloriesBurnt() {
        return totalCaloriesBurnt;
    }

    public void setTotalCaloriesBurnt(double totalCaloriesBurnt) {
        this.totalCaloriesBurnt = totalCaloriesBurnt;
    }
}
