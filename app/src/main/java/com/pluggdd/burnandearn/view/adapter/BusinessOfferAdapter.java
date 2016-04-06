package com.pluggdd.burnandearn.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
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

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.OfferDetailActivity;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.view.fragment.OffersAndRewardsFragment;

import java.util.ArrayList;

/**
 * Created by User on 12-Feb-16.
 */
public class BusinessOfferAdapter extends RecyclerView.Adapter<BusinessOfferAdapter.BusinessOfferViewHolder> {

    private Context mContext;
    private int mLastPosition = -1;
    private OffersAndRewardsFragment mFragment;
    private ArrayList<BusinessDetails> mBusinessOfferList;


    public BusinessOfferAdapter(Context context, ArrayList<BusinessDetails> mBusinessList) {
        mContext = context;
        mBusinessOfferList = mBusinessList;

    }

    @Override
    public BusinessOfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflater_view = LayoutInflater.from(mContext).inflate(R.layout.list_row_business_offer, parent, false);
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

        holder.sGrabNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, OfferDetailActivity.class);
                intent.putExtra(mContext.getString(R.string.business_id), businessDetail.getId());
                intent.putExtra(mContext.getString(R.string.logo_url), businessDetail.getLogo());
                intent.putExtra(mContext.getString(R.string.business_name), businessDetail.getName());
                intent.putExtra(mContext.getString(R.string.offer_name), businessDetail.getOffer_name());
                intent.putExtra(mContext.getString(R.string.offer_promo), businessDetail.getPromo());
                intent.putExtra(mContext.getString(R.string.how_to_redeem), businessDetail.getHow_to_reedem());
                intent.putExtra(mContext.getString(R.string.coupon_expiry_date), businessDetail.getCoupon_expiry_date());
                intent.putExtra(mContext.getString(R.string.coupon), businessDetail.getCoupon());
                intent.putExtra(mContext.getString(R.string.redirect_url), businessDetail.getUrl() /*"https://play.google.com/store/apps/details?id=com.tingtongapp.android&hl=en"*/);
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
        private TextView sOfferRewardsText,sNameText;
        private ImageView sBusinessImage;
        private ProgressBar sLogoProgressBar;
        private Button sGrabNowButton;

        public BusinessOfferViewHolder(View itemView) {
            super(itemView);
            sContainer = (CardView) itemView.findViewById(R.id.business_offer_container);
            sOfferRewardsText = (TextView) itemView.findViewById(R.id.txt_offer_promo);
            sNameText = (TextView) itemView.findViewById(R.id.txt_business_title);
            sBusinessImage = (ImageView) itemView.findViewById(R.id.img_business_logo);
            sLogoProgressBar = (ProgressBar) itemView.findViewById(R.id.logo_progress_bar);
            sGrabNowButton = (Button) itemView.findViewById(R.id.btn_grab_now);
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