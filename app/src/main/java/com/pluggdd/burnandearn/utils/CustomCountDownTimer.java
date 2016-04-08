package com.pluggdd.burnandearn.utils;

import android.os.CountDownTimer;
import android.widget.TextView;

import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

/**
 * Created by User on 07-Apr-16.
 */
public class CustomCountDownTimer extends CountDownTimer {

    private TextView mDaysText,mHoursText,mMinutesText;

    public CustomCountDownTimer(TextView daysText,TextView hoursText,TextView minutesText,long millisInFuture, long countDownInterval){
        super(millisInFuture, countDownInterval);
        mDaysText = daysText;
        mHoursText = hoursText;
        mMinutesText = minutesText;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //setText(new Period(millisUntilFinished, PeriodType.dayTime().withMillisRemoved()));
        mDaysText.setText(String.format("%02d",TimeUnit.MILLISECONDS.toDays(millisUntilFinished)));
        mHoursText.setText(String.format("%02d",TimeUnit.MILLISECONDS.toHours(millisUntilFinished) -
                TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millisUntilFinished))));
        mMinutesText.setText(String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))));
    }

    @Override
    public void onFinish() {

    }

    private void setText(Period period) {
        long days = period.toStandardDuration().getStandardDays();
        int hours = period.getHours();
        int minutes = period.getMinutes();
        int seconds = period.getSeconds();

    }
}
