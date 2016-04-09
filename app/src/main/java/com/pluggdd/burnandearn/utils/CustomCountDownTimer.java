package com.pluggdd.burnandearn.utils;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pluggdd.burnandearn.R;

import org.joda.time.DurationFieldType;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

/**
 * Created by User on 07-Apr-16.
 */
public class CustomCountDownTimer extends CountDownTimer {

    private Context mContext;
    private LinearLayout mDaysEndInContainer;
    private TextView mDaysText,mHoursText,mMinutesText,mExpiredAtText,mCouponCodeText;
    private Button mRedeemButton;
    private String mExpiryDate,mPageFlag;


    public CustomCountDownTimer(Context context,LinearLayout daysEndsInContainer,TextView daysText,TextView hoursText,TextView minutesText,TextView expiredAtText,Button redeemButton,TextView couponCodeText,String expiryDate,String pageFlag,long millisInFuture, long countDownInterval){
        super(millisInFuture, countDownInterval);
        mContext = context;
        mDaysEndInContainer = daysEndsInContainer;
        mDaysText = daysText;
        mHoursText = hoursText;
        mMinutesText = minutesText;
        mExpiredAtText = expiredAtText;
        mExpiryDate = expiryDate;
        mRedeemButton = redeemButton;
        mPageFlag = pageFlag;
        mCouponCodeText = couponCodeText;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        //setText(new Period(millisUntilFinished, PeriodType.dayTime().withMillisRemoved()));
        mDaysText.setText(String.format("%02d", TimeUnit.MILLISECONDS.toDays(millisUntilFinished)));
        mHoursText.setText(String.format("%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished) -
                TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millisUntilFinished))));
        mMinutesText.setText(String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))));
    }

    @Override
    public void onFinish() {

        if(mPageFlag.equalsIgnoreCase("offer_detail")){
            mDaysEndInContainer.setVisibility(View.INVISIBLE);
            mRedeemButton.setVisibility(View.GONE);
        }else{
            mDaysEndInContainer.setVisibility(View.GONE);
            mExpiredAtText.setVisibility(View.VISIBLE);
            LocalDateTime expirationDateTime = new LocalDateTime(mExpiryDate);
            mExpiredAtText.setText("EXPIRED ON : " + expirationDateTime.toString("MMM dd,YYYY"));
            mRedeemButton.setVisibility(View.INVISIBLE);
            mCouponCodeText.setVisibility(View.INVISIBLE);
        }
    }


}
