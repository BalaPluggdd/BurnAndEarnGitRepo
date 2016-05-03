package com.pluggdd.burnandearn.view.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.OfferDetailActivity;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.utils.CustomCountDownTimer;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.view.fragment.OffersAndRewardsFragment;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

/**
 * Created by User on 12-Feb-16.
 */
public class OfferRewardsAdapter extends RecyclerView.Adapter<OfferRewardsAdapter.BusinessOfferViewHolder> {

    private Activity mContext;
    private int mLastPosition = -1;
    private ArrayList<BusinessDetails> mBusinessOfferList;
    private PreferencesManager mPreferenceManager;
    private String mPageFlag;


    public OfferRewardsAdapter(Context context, ArrayList<BusinessDetails> mBusinessList, String pageFlag) {
        mContext = (Activity)context;
        mBusinessOfferList = mBusinessList;
        mPreferenceManager = new PreferencesManager(mContext);
        mPageFlag = pageFlag;
    }

    @Override
    public BusinessOfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflater_view = LayoutInflater.from(mContext).inflate(R.layout.list_row_offer_and_rewards, parent, false);
        BusinessOfferViewHolder viewHolder = new BusinessOfferViewHolder(inflater_view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BusinessOfferViewHolder holder, int position) {
        setAnimation(holder.sContainer, position);
        final BusinessDetails businessDetail = mBusinessOfferList.get(position);
        //holder.sNameText.setText(businessDetail.getName());
        new PicassoImageLoaderHelper(mContext, holder.sBusinessImage, holder.sLogoProgressBar).loadImage(businessDetail.getLogo());
        new PicassoImageLoaderHelper(mContext, holder.sBusinessOfferImage, holder.sOfferImageProgressBar).loadImage(businessDetail.getOfferLogo());
        holder.sOfferRewardsText.setText(businessDetail.getOffer_name());
        holder.sPointsNeededText.setText(String.valueOf(businessDetail.getPoints_needed()));
        if(mPageFlag.equalsIgnoreCase(mContext.getString(R.string.my_offers))) { // Active My Offers
            holder.sCouponCodeText.setVisibility(View.VISIBLE);
            holder.sRedeemButton.setText(businessDetail.getCoupon());
        }else if(mPageFlag.equalsIgnoreCase(mContext.getString(R.string.my_offers_inactive))) { //In Active My Offers
            holder.sDaysEndsInContainer.setVisibility(View.GONE);
            holder.sExpiredAtText.setVisibility(View.VISIBLE);
            LocalDateTime expirationDateTime = new LocalDateTime(businessDetail.getCoupon_expiry_date());
            holder.sExpiredAtText.setText("EXPIRED ON : " + expirationDateTime.toString("MMM dd,YYYY"));
            holder.sRedeemButton.setVisibility(View.GONE);
            //holder.sPhoneImage.setVisibility(View.GONE);
            //holder.sLocationImage.setVisibility(View.GONE);
        }
        if(!mPageFlag.equalsIgnoreCase(mContext.getString(R.string.my_offers_inactive))) { // Active My Offers
            // To calculate time left
            long date_difference = new LocalDateTime(businessDetail.getCoupon_expiry_date()).plusDays(1).toDateTime().getMillis() - new LocalDateTime().toDateTime().getMillis();
            //long date_difference = new LocalDateTime().plusMinutes(1).toDateTime().getMillis() - new LocalDateTime().toDateTime().getMillis();
            new CustomCountDownTimer(mContext,holder.sDaysEndsInContainer,holder.sDaysText,holder.sHourText,holder.sMinuteText,holder.sExpiredAtText,holder.sRedeemButton,holder.sCouponCodeText,businessDetail.getCoupon_expiry_date(),mPageFlag,date_difference,1000).start();
        }


        holder.sRedeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.sRedeemButton.getText().toString().equalsIgnoreCase(mContext.getString(R.string.redeem))){
                    int points_earned = mPreferenceManager.getIntValue(mContext.getString(R.string.your_total_points));
                    if(points_earned >= businessDetail.getPoints_needed()){
                        Intent intent = new Intent(mContext, OfferDetailActivity.class);
                        intent.putExtra(mContext.getString(R.string.business_id),businessDetail.getId());
                        intent.putExtra(mContext.getString(R.string.logo_url),businessDetail.getLogo());
                        intent.putExtra(mContext.getString(R.string.offer_image_url),businessDetail.getOfferLogo());
                        intent.putExtra(mContext.getString(R.string.business_name),businessDetail.getName());
                        intent.putExtra(mContext.getString(R.string.offer_name),businessDetail.getOffer_name());
                        intent.putExtra(mContext.getString(R.string.offer_promo),businessDetail.getPromo());
                        intent.putExtra(mContext.getString(R.string.points_needed),businessDetail.getPoints_needed());
                        intent.putExtra(mContext.getString(R.string.expire_date),businessDetail.getCoupon_expiry_date());
                        intent.putExtra(mContext.getString(R.string.coupon),businessDetail.getCoupon());
                        intent.putExtra(mContext.getString(R.string.website),businessDetail.getUrl());
                        intent.putExtra(mContext.getString(R.string.address), businessDetail.getAddress());
                        intent.putExtra(mContext.getString(R.string.offer_type), businessDetail.getOffer_type());
                        intent.putExtra(mContext.getString(R.string.terms_and_conditions),businessDetail.getTerms_and_conditions());
                        intent.putExtra(mContext.getString(R.string.phone_number),businessDetail.getPhone_number());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(mContext,
                                            new Pair<View, String>(holder.sBusinessImage,mContext.getString(R.string.activity_logo_transition)),
                                            new Pair<View, String>(holder.sBusinessOfferImage,mContext.getString(R.string.activity_image_transition)),
                                            new Pair<View, String>(holder.sPointsNeededContainer,mContext.getString(R.string.activity_points_needed_transition)));

                            mContext.startActivity(intent, options.toBundle());
                        }
                        else {
                            mContext.startActivity(intent);
                        }
                    }else{
                        int points_required = businessDetail.getPoints_needed() - points_earned;
                        AlertDialog.Builder mDialog = new AlertDialog.Builder(mContext,R.style.AlertDialogStyle);
                        mDialog.setMessage("EARN " + points_required + " MORE POINTS TO AVAIL THIS OFFER");
                        mDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        mDialog.show();
                    }
                }
          }
        });
    }

    @Override
    public int getItemCount() {
        return mBusinessOfferList.size();
    }

    public static class BusinessOfferViewHolder extends RecyclerView.ViewHolder {

        private CardView sContainer;
        private ImageView sBusinessImage,sBusinessOfferImage;
        private LinearLayout sPointsNeededContainer,sDaysEndsInContainer;
        private TextView sOfferRewardsText, sPointsNeededText,sDaysText,sHourText,sMinuteText,sExpiredAtText,sCouponCodeText;
        private Button sRedeemButton;
        private ProgressBar sLogoProgressBar,sOfferImageProgressBar;

        public BusinessOfferViewHolder(View itemView) {
            super(itemView);
            sContainer = (CardView) itemView.findViewById(R.id.offers_rewards_container);
            sOfferRewardsText = (TextView) itemView.findViewById(R.id.txt_offer_promo);
            //sNameText = (TextView) itemView.findViewById(R.id.txt_business_title);
            sPointsNeededContainer = (LinearLayout) itemView.findViewById(R.id.points_needed_container);
            sDaysEndsInContainer = (LinearLayout) itemView.findViewById(R.id.days_ends_in_container);
            sPointsNeededText = (TextView) itemView.findViewById(R.id.txt_points_needed);
            sCouponCodeText = (TextView) itemView.findViewById(R.id.txt_coupon_code);
            sExpiredAtText = (TextView) itemView.findViewById(R.id.txt_coupon_expired);
            sRedeemButton = (Button) itemView.findViewById(R.id.btn_redeem);
            sBusinessImage = (ImageView) itemView.findViewById(R.id.img_business_logo);
            sBusinessOfferImage = (ImageView) itemView.findViewById(R.id.img_offer_image);
            sLogoProgressBar = (ProgressBar) itemView.findViewById(R.id.logo_progress_bar);
            sOfferImageProgressBar = (ProgressBar) itemView.findViewById(R.id.offer_image_progress_bar);
            sDaysText = (TextView) itemView.findViewById(R.id.txt_days);
            sHourText = (TextView) itemView.findViewById(R.id.txt_hours);
            sMinuteText = (TextView) itemView.findViewById(R.id.txt_minutes);
            //sPhoneImage = (ImageView) itemView.findViewById(R.id.img_call);
            //sLocationImage = (ImageView) itemView.findViewById(R.id.img_location);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > mLastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
            viewToAnimate.startAnimation(animation);
            mLastPosition = position;
        }
    }
}