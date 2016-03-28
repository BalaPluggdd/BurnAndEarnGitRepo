package com.pluggdd.burnandearn.model;

/**
 * Model to hold Fitness activity(Walking, running, Biking) details
 */
public class FitnessActivity {

    private String name;
    private int step_count=-1;
    private double distance=-1,calories_expended=-1,calories_percentage,distance_percentage,steps_percentage;

    public FitnessActivity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStep_count() {
        return step_count;
    }

    public void setStep_count(int step_count) {
        this.step_count = step_count;
    }

    public double getCalories_expended() {
        return calories_expended;
    }

    public void setCalories_expended(double calories_expended) {
        this.calories_expended = calories_expended;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getCalories_percentage() {
        return calories_percentage;
    }

    public void setCalories_percentage(double calories_percentage) {
        this.calories_percentage = calories_percentage;
    }

    public double getDistance_percentage() {
        return distance_percentage;
    }

    public void setDistance_percentage(double distance_percentage) {
        this.distance_percentage = distance_percentage;
    }

    public double getSteps_percentage() {
        return steps_percentage;
    }

    public void setSteps_percentage(double steps_percentage) {
        this.steps_percentage = steps_percentage;
    }
}
