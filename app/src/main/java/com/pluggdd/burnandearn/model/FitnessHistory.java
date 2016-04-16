package com.pluggdd.burnandearn.model;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

/**
 * Created by User on 14-Mar-16.
 */
public class FitnessHistory {

    private LocalDateTime startDateTime,endDateTime;

    private ArrayList<FitnessActivity> fitnessActivitiesList;

    private double totalCaloriesBurnt,walkingCaloriesBurnt,cyclingCaloriesBurnt,runningCaloriesBurnt,walkingDistance,cyclingDistance,runningDistance;

    private int walkingSteps,cyclingSteps,runningSteps;

    private String source;

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

    public double getWalkingCaloriesBurnt() {
        return walkingCaloriesBurnt;
    }

    public void setWalkingCaloriesBurnt(double walkingCaloriesBurnt) {
        this.walkingCaloriesBurnt = walkingCaloriesBurnt;
    }

    public double getCyclingCaloriesBurnt() {
        return cyclingCaloriesBurnt;
    }

    public void setCyclingCaloriesBurnt(double cyclingCaloriesBurnt) {
        this.cyclingCaloriesBurnt = cyclingCaloriesBurnt;
    }

    public double getRunningCaloriesBurnt() {
        return runningCaloriesBurnt;
    }

    public void setRunningCaloriesBurnt(double runningCaloriesBurnt) {
        this.runningCaloriesBurnt = runningCaloriesBurnt;
    }

    public int getWalkingSteps() {
        return walkingSteps;
    }

    public void setWalkingSteps(int walkingSteps) {
        this.walkingSteps = walkingSteps;
    }

    public int getCyclingSteps() {
        return cyclingSteps;
    }

    public void setCyclingSteps(int cyclingSteps) {
        this.cyclingSteps = cyclingSteps;
    }

    public int getRunningSteps() {
        return runningSteps;
    }

    public void setRunningSteps(int runningSteps) {
        this.runningSteps = runningSteps;
    }

    public double getRunningDistance() {
        return runningDistance;
    }

    public void setRunningDistance(double runningDistance) {
        this.runningDistance = runningDistance;
    }

    public double getCyclingDistance() {
        return cyclingDistance;
    }

    public void setCyclingDistance(double cyclingDistance) {
        this.cyclingDistance = cyclingDistance;
    }

    public double getWalkingDistance() {
        return walkingDistance;
    }

    public void setWalkingDistance(double walkingDistance) {
        this.walkingDistance = walkingDistance;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
