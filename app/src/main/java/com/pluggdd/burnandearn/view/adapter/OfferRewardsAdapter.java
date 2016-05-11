package com.pluggdd.burnandearn.view.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.OfferDetailActivity;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.utils.CustomCountDownTimer;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;
import com.pluggdd.burnandearn.view.fragment.OffersAndRewardsFragment;

import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by User on 12-Feb-16.
 */
public class OfferRewardsAdapter extends RecyclerView.Adapter<OfferRewardsAdapter.BusinessOfferViewHolder> {

    private Activity mContext;
    private int mLastPosition = -1, mPosition;
    private ArrayList<BusinessDetails> mBusinessOfferList;
    private PreferencesManager mPreferenceManager;
    private String mPageFlag;
    private ArrayList<BusinessOfferViewHolder> mViewHoldersList;
    private Handler handler = new Handler();


    public OfferRewardsAdapter(Context context, ArrayList<BusinessDetails> mBusinessList, String pageFlag) {
        mContext = (Activity) context;
        mBusinessOfferList = mBusinessList;
        mPreferenceManager = new PreferencesManager(mContext);
        mPageFlag = pageFlag;
        mViewHoldersList = new ArrayList<>();
        startUpdateTimer();
    }

    @Override
    public BusinessOfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflater_view = LayoutInflater.from(mContext).inflate(R.layout.list_row_offer_and_rewards, parent, false);
        BusinessOfferViewHolder viewHolder = new BusinessOfferViewHolder(inflater_view,mPageFlag);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BusinessOfferViewHolder holder, final int position) {
        setAnimation(holder.sContainer, position);
        final BusinessDetails businessDetail = mBusinessOfferList.get(position);
        synchronized (mViewHoldersList) {
            holder.setData(businessDetail);
            if(!mViewHoldersList.contains(holder))
                mViewHoldersList.add(holder);
        }

       //holder.sNameText.setText(businessDetail.getName());
        new PicassoImageLoaderHelper(mContext, holder.sBusinessImage, holder.sLogoProgressBar).loadImage(businessDetail.getLogo());
        new PicassoImageLoaderHelper(mContext, holder.sBusinessOfferImage, holder.sOfferImageProgressBar).loadImage(businessDetail.getOfferLogo());
        holder.sOfferRewardsText.setText(businessDetail.getOffer_name());
        holder.sPointsNeededText.setText(String.valueOf(businessDetail.getPoints_needed()));
        if (mPageFlag.equalsIgnoreCase(mContext.getString(R.string.my_offers))) { // Active My Offers
            holder.sCouponCodeText.setVisibility(View.VISIBLE);
            holder.sPointsNeededContainer.setVisibility(View.GONE);
            holder.sUseNowButton.setVisibility(View.VISIBLE);
            holder.sRedeemButton.setText(businessDetail.getCoupon());
        } else if (mPageFlag.equalsIgnoreCase(mContext.getString(R.string.my_offers_inactive))) { //In Active My Offers
            holder.sDaysEndsInContainer.setVisibility(View.GONE);
            holder.sExpiredAtText.setVisibility(View.VISIBLE);
            LocalDateTime expirationDateTime = new LocalDateTime(businessDetail.getCoupon_expiry_date());
            holder.sExpiredAtText.setText("EXPIRED ON : " + expirationDateTime.toString("MMM dd,YYYY"));
            holder.sRedeemButton.setVisibility(View.GONE);
            //holder.sPhoneImage.setVisibility(View.GONE);
            //holder.sLocationImage.setVisibility(View.GONE);
        }

        holder.sUseNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
                mDialog.setMessage("Are you sure you want to use this offer right now?");
                mDialog.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(new NetworkCheck().ConnectivityCheck(mContext)){
                            redeemOffer(businessDetail.getId(),position);
                        }else{
                            Toast.makeText(mContext,mContext.getString(R.string.no_network),Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
                mDialog.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mDialog.show();

            }
        });


        holder.sRedeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.sRedeemButton.getText().toString().equalsIgnoreCase(mContext.getString(R.string.redeem))) {
                    int points_earned = mPreferenceManager.getIntValue(mContext.getString(R.string.your_total_points));
                    if (points_earned >= businessDetail.getPoints_needed()) {
                        Intent intent = new Intent(mContext, OfferDetailActivity.class);
                        intent.putExtra(mContext.getString(R.string.business_id), businessDetail.getId());
                        intent.putExtra(mContext.getString(R.string.logo_url), businessDetail.getLogo());
                        intent.putExtra(mContext.getString(R.string.offer_image_url), businessDetail.getOfferLogo());
                        intent.putExtra(mContext.getString(R.string.business_name), businessDetail.getName());
                        intent.putExtra(mContext.getString(R.string.offer_name), businessDetail.getOffer_name());
                        intent.putExtra(mContext.getString(R.string.offer_promo), businessDetail.getPromo());
                        intent.putExtra(mContext.getString(R.string.points_needed), businessDetail.getPoints_needed());
                        intent.putExtra(mContext.getString(R.string.expire_date), businessDetail.getCoupon_expiry_date());
                        intent.putExtra(mContext.getString(R.string.coupon), businessDetail.getCoupon());
                        intent.putExtra(mContext.getString(R.string.website), businessDetail.getUrl());
                        intent.putExtra(mContext.getString(R.string.address), businessDetail.getAddress());
                        intent.putExtra(mContext.getString(R.string.offer_type), businessDetail.getOffer_type());
                        intent.putExtra(mContext.getString(R.string.terms_and_conditions), businessDetail.getTerms_and_conditions());
                        intent.putExtra(mContext.getString(R.string.phone_number), businessDetail.getPhone_number());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(mContext,
                                            new Pair<View, String>(holder.sBusinessImage, mContext.getString(R.string.activity_logo_transition)),
                                            new Pair<View, String>(holder.sBusinessOfferImage, mContext.getString(R.string.activity_image_transition)),
                                            new Pair<View, String>(holder.sPointsNeededContainer, mContext.getString(R.string.activity_points_needed_transition)));

                            mContext.startActivity(intent, options.toBundle());
                        } else {
                            mContext.startActivity(intent);
                        }
                    } else {
                        int points_required = businessDetail.getPoints_needed() - points_earned;
                        AlertDialog.Builder mDialog = new AlertDialog.Builder(mContext, R.style.AlertDialogStyle);
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
        private ImageView sBusinessImage, sBusinessOfferImage;
        private LinearLayout sPointsNeededContainer, sDaysEndsInContainer;
        private TextView sOfferRewardsText, sPointsNeededText, sDaysText, sHourText, sMinuteText, sExpiredAtText, sCouponCodeText;
        private Button sRedeemButton,sUseNowButton;
        private ProgressBar sLogoProgressBar, sOfferImageProgressBar;
        private BusinessDetails mBusinessDetail;
        private String mPageFlag;

        public BusinessOfferViewHolder(View itemView,String pageFlag) {
            super(itemView);
            this.mPageFlag = pageFlag;
            sContainer = (CardView) itemView.findViewById(R.id.offers_rewards_container);
            sOfferRewardsText = (TextView) itemView.findViewById(R.id.txt_offer_promo);
            //sNameText = (TextView) itemView.findViewById(R.id.txt_business_title);
            sPointsNeededContainer = (LinearLayout) itemView.findViewById(R.id.points_needed_container);
            sDaysEndsInContainer = (LinearLayout) itemView.findViewById(R.id.days_ends_in_container);
            sPointsNeededText = (TextView) itemView.findViewById(R.id.txt_points_needed);
            sCouponCodeText = (TextView) itemView.findViewById(R.id.txt_coupon_code);
            sExpiredAtText = (TextView) itemView.findViewById(R.id.txt_coupon_expired);
            sRedeemButton = (Button) itemView.findViewById(R.id.btn_redeem);
            sUseNowButton = (Button) itemView.findViewById(R.id.btn_user_now);
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

        public void setData(BusinessDetails businessDetails) {
            this.mBusinessDetail = businessDetails;
        }

        public void updateTimeRemaining() {
            try {
                long timeDiff = new LocalDateTime(mBusinessDetail.getCoupon_expiry_date()).plusDays(1).toDateTime().getMillis() - new LocalDateTime().toDateTime().getMillis();
                //timeDiff = new LocalDateTime().plusMinutes(1).toDateTime().getMillis() - new LocalDateTime().toDateTime().getMillis();
                if (timeDiff > 0) {
                    sDaysText.setText(String.format("%02d", TimeUnit.MILLISECONDS.toDays(timeDiff)));
                    sHourText.setText(String.format("%02d", TimeUnit.MILLISECONDS.toHours(timeDiff) -
                            TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(timeDiff))));
                    sMinuteText.setText(String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(timeDiff) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff))));
                } else {
                    //txt_timeleft.setText("Time Out !!");
                    if(mPageFlag.equalsIgnoreCase("offer_detail")){
                        sDaysEndsInContainer.setVisibility(View.INVISIBLE);
                        sRedeemButton.setVisibility(View.GONE);
                        sUseNowButton.setVisibility(View.INVISIBLE);
                    }else{
                        sDaysEndsInContainer.setVisibility(View.GONE);
                        sExpiredAtText.setVisibility(View.VISIBLE);
                        LocalDateTime expirationDateTime = new LocalDateTime(mBusinessDetail.getCoupon_expiry_date());
                        sExpiredAtText.setText("EXPIRED ON : " + expirationDateTime.toString("MMM dd,YYYY"));
                        sRedeemButton.setVisibility(View.INVISIBLE);
                        sUseNowButton.setVisibility(View.INVISIBLE);
                        sCouponCodeText.setVisibility(View.INVISIBLE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

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

    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mViewHoldersList) {
                for (BusinessOfferViewHolder holder : mViewHoldersList) {
                    holder.updateTimeRemaining();
                }
            }
        }
    };

    private void startUpdateTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(updateRemainingTimeRunnable);
            }
        }, 1000, 1000);
    }

    private void redeemOffer(final int businessId, final int position){
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Redeem offer please wait !!!");
        progressDialog.setCancelable(false);
        progressDialog.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.REDEEM_OFFER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                if (response != null) {
                    try {
                        progressDialog.dismiss();
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1) {
                                /*mBurnAndLogoContainer.setVisibility(View.VISIBLE);
                                mPointsNeededContainer.setVisibility(View.GONE);
                                mRedeemButton.setText("Coupon code : " + mExtra.getString(getString(R.string.coupon)));*/
                            Toast.makeText(mContext,"Successfully redeemed your offer",Toast.LENGTH_SHORT).show();
                            Log.i("Position",position+"");
                            mBusinessOfferList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, getItemCount());
                            //showCouponDialog();
                        }else{
                            Toast.makeText(mContext,responseJson.optString("msg"),Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    progressDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userId",mPreferenceManager.getStringValue(mContext.getString(R.string.user_id)));
                params.put("businessId",String.valueOf(businessId));
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }

}