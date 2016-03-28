package com.pluggdd.burnandearn.view.adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.OfferDetailActivity;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.view.fragment.OffersAndRewardsFragment;

import java.util.ArrayList;

/**
 * Created by User on 12-Feb-16.
 */
public class OfferRewardsAdapter extends RecyclerView.Adapter<OfferRewardsAdapter.BusinessOfferViewHolder> {

    private Context mContext;
    private int mLastPosition = -1;
    private OffersAndRewardsFragment mFragment;
    private ArrayList<BusinessDetails> mBusinessOfferList;


    public OfferRewardsAdapter(Context context, ArrayList<BusinessDetails> mBusinessList,OffersAndRewardsFragment fragment) {
        mContext = context;
        mFragment = fragment;
        mBusinessOfferList = mBusinessList;

    }

    @Override
    public BusinessOfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflater_view = LayoutInflater.from(mContext).inflate(R.layout.list_row_offer_rewards, parent, false);
        BusinessOfferViewHolder viewHolder = new BusinessOfferViewHolder(inflater_view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final BusinessOfferViewHolder holder, int position) {
        setAnimation(holder.sContainer, position);
        final BusinessDetails businessDetail = mBusinessOfferList.get(position);
        holder.sNameText.setText(businessDetail.getName());
        new PicassoImageLoaderHelper(mContext, holder.sBusinessImage, holder.sLogoProgressBar).loadImage(businessDetail.getLogo());
        holder.sOfferRewardsText.setText(businessDetail.getPromo());
        holder.sPointsNeeded.setText("Points needed : " + businessDetail.getPoints_needed());

        holder.sPhoneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String uri = "tel:" + businessDetail.getPhone_number();
                    Intent mCallIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
                    mContext.startActivity(mCallIntent);
                }catch(Exception e) {
                    Toast.makeText(mContext, "Your call has failed...", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }

            }
        });

        holder.sLocationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               try {
                   Uri locationUri = Uri.parse("geo:0,0?q="+businessDetail.getAddress());
                   Intent mMapIntent = new Intent(Intent.ACTION_VIEW, locationUri);
                   mMapIntent.setPackage("com.google.android.apps.maps");
                   mContext.startActivity(mMapIntent);
               }catch (Exception e){
                   e.printStackTrace();
                   Toast.makeText(mContext, "Maps not available...", Toast.LENGTH_LONG).show();
               }
            }
        });

        holder.sRedeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, OfferDetailActivity.class);
                intent.putExtra(mContext.getString(R.string.logo_url),businessDetail.getLogo());
                intent.putExtra(mContext.getString(R.string.business_name),businessDetail.getName());
                intent.putExtra(mContext.getString(R.string.offer_name),businessDetail.getOffer_name());
                intent.putExtra(mContext.getString(R.string.offer_promo),businessDetail.getPromo());
                intent.putExtra(mContext.getString(R.string.how_to_redeem),businessDetail.getHow_to_reedem());
                intent.putExtra(mContext.getString(R.string.coupon_expiry_date),businessDetail.getCoupon_expiry_date());
                intent.putExtra(mContext.getString(R.string.coupon),businessDetail.getCoupon());
                intent.putExtra(mContext.getString(R.string.redirect_url),businessDetail.getUrl());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBusinessOfferList.size();
    }

    public static class BusinessOfferViewHolder extends RecyclerView.ViewHolder {

        private CardView sContainer;
        private ImageView sBusinessImage,sPhoneImage,sLocationImage;
        private TextView sOfferRewardsText, sNameText, sPointsNeeded;
        private Button sRedeemButton;
        private ProgressBar sLogoProgressBar;

        public BusinessOfferViewHolder(View itemView) {
            super(itemView);
            sContainer = (CardView) itemView.findViewById(R.id.offers_rewards_container);
            sOfferRewardsText = (TextView) itemView.findViewById(R.id.txt_offer_promo);
            sNameText = (TextView) itemView.findViewById(R.id.txt_business_title);
            sPointsNeeded = (TextView) itemView.findViewById(R.id.txt_points_needed);
            sRedeemButton = (Button) itemView.findViewById(R.id.btn_grab_now);
            sBusinessImage = (ImageView) itemView.findViewById(R.id.img_business_logo);
            sLogoProgressBar = (ProgressBar) itemView.findViewById(R.id.logo_progress_bar);
            sPhoneImage = (ImageView) itemView.findViewById(R.id.img_call);
            sLocationImage = (ImageView) itemView.findViewById(R.id.img_location);
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