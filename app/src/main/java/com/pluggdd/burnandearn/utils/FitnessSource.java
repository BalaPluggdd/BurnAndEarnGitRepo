package com.pluggdd.burnandearn.utils;

/**
 * Created by User on 16-Apr-16.
 */
public enum FitnessSource {

    GOOGLE_FIT(1),FITBIT(2);

    private int id;

    FitnessSource(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
