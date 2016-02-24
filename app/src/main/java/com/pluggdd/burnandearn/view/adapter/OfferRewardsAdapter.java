package com.pluggdd.burnandearn.view.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.view.fragment.OffersAndRewardsFragment;

/**
 * Created by User on 12-Feb-16.
 */
public class OfferRewardsAdapter extends RecyclerView.Adapter<OfferRewardsAdapter.RestaurentLiveDealViewHolder> {

    private Context mContext;
    private int mLastPosition = -1;
    private FragmentInteraction mListener;
    private OffersAndRewardsFragment mFragment;

    public OfferRewardsAdapter(Context context,FragmentInteraction listener,OffersAndRewardsFragment fragment) {
        mContext = context;
        mListener = listener;
        mFragment = fragment;
    }

    @Override
    public RestaurentLiveDealViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflater_view = LayoutInflater.from(mContext).inflate(R.layout.list_row_offer_rewards, parent, false);
        RestaurentLiveDealViewHolder viewHolder = new RestaurentLiveDealViewHolder(inflater_view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RestaurentLiveDealViewHolder holder, int position) {
        //setAnimation(holder.sContainer, position);
        /*holder.sContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtrasManager extras = new ExtrasManager();
                extras.setStringValue(mContext.getString(R.string.page_flag),"NearMyRestaurentListFragment");
                extras.setParceableValue(mContext.getString(R.string.image),((BitmapDrawable)holder.sRestaurentImage.getDrawable()).getBitmap());
                mListener.changeFragmentWithSharedElements(mFragment,extras.getBundle(), holder.sRestaurentImage);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public static class RestaurentLiveDealViewHolder extends RecyclerView.ViewHolder {

        private CardView sContainer;
        private ImageView sBusinessImage;
        private TextView sOfferRewardsText, sNameText, sPointsNeeded;
        private Button sRedeemButton;


        public RestaurentLiveDealViewHolder(View itemView) {
            super(itemView);
            sContainer = (CardView) itemView.findViewById(R.id.offers_rewards_container);
            sOfferRewardsText = (TextView) itemView.findViewById(R.id.txt_offer_rewards);
            sNameText = (TextView) itemView.findViewById(R.id.name);
            sPointsNeeded = (TextView) itemView.findViewById(R.id.txt_points_needed);
            sRedeemButton = (Button) itemView.findViewById(R.id.btn_redeem);
            sBusinessImage = (ImageView) itemView.findViewById(R.id.business_image);
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