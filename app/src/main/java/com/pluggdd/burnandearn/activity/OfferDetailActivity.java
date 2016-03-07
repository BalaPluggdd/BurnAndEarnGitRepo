package com.pluggdd.burnandearn.activity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.HeaderView;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;


public class OfferDetailActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener{


    private Toolbar mToolBar;
    AppBarLayout mAppBarLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    HeaderView toolbarHeaderView;
    HeaderView floatHeaderView;
    TextView mHeaderTitle,mHeaderSubTitle;
    boolean isHideToolbarView = false;
    private ImageView mBusinessLogo;
    private TextView mOfferPromoText,mHowToRedeemText;
    private ProgressBar mLogoProgressBar;
    private Button mRedeemButton;
    private Bundle mExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_container);
        toolbarHeaderView = (HeaderView) findViewById(R.id.toolbar_header_view);
        floatHeaderView = (HeaderView) findViewById(R.id.float_header_view);
        mHeaderTitle = (TextView) toolbarHeaderView.findViewById(R.id.header_view_title);
        mHeaderSubTitle = (TextView) toolbarHeaderView.findViewById(R.id.header_view_sub_title);
        mBusinessLogo = (ImageView) findViewById(R.id.img_business_logo);
        mOfferPromoText = (TextView) findViewById(R.id.txt_offer_promo);
        mHowToRedeemText = (TextView) findViewById(R.id.txt_how_to_redeem);
        mLogoProgressBar = (ProgressBar) findViewById(R.id.logo_progress_bar);
        mRedeemButton = (Button) findViewById(R.id.btn_redeem_now);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(" ");
        mAppBarLayout.addOnOffsetChangedListener(this);
        mExtra = getIntent().getExtras();
        new PicassoImageLoaderHelper(this,mBusinessLogo,mLogoProgressBar).loadImage(mExtra.getString(getString(R.string.logo_url), ""));
        toolbarHeaderView.bindTo(mExtra.getString(getString(R.string.business_name), ""), mExtra.getString(getString(R.string.offer_name), ""));
        floatHeaderView.bindTo(mExtra.getString(getString(R.string.business_name), ""), mExtra.getString(getString(R.string.offer_name), ""));
        mOfferPromoText.setText(mExtra.getString(getString(R.string.offer_promo), ""));
        mHowToRedeemText.setText(mExtra.getString(getString(R.string.how_to_redeem), ""));

        mRedeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mCouponDialog = new AlertDialog.Builder(OfferDetailActivity.this);
                View mView = LayoutInflater.from(OfferDetailActivity.this).inflate(R.layout.dialog_coupon, null);
                mCouponDialog.setView(mView);
                ImageView mCouponBusinessLogo = (ImageView) mView.findViewById(R.id.img_business_logo);
                ProgressBar mLogoProgress = (ProgressBar) mView.findViewById(R.id.logo_progress_bar);
                TextView mCouponCodeText = (TextView) mView.findViewById(R.id.txt_coupon_code);
                TextView mCouponExpiryText = (TextView) mView.findViewById(R.id.txt_coupon_expiry);
                Button mRedirectButton = (Button) mView.findViewById(R.id.btn_redirect);
                final Button mCancelButton = (Button) mView.findViewById(R.id.btn_cancel);
                new PicassoImageLoaderHelper(OfferDetailActivity.this,mCouponBusinessLogo,mLogoProgress).loadImage(mExtra.getString(getString(R.string.logo_url), ""));
                mCouponCodeText.setText("Coupon Code : " + mExtra.getString(getString(R.string.coupon), ""));
                mCouponExpiryText.setText("Expiry Date : "+ mExtra.getString(getString(R.string.coupon_expiry_date), ""));
                mCouponDialog.setCancelable(true);
                final AlertDialog dialog =  mCouponDialog.show();
                mRedirectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                           try{
                               Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(mExtra.getString(getString(R.string.redirect_url), "")));
                               startActivity(intent);
                           }catch (ActivityNotFoundException e){
                               Toast.makeText(OfferDetailActivity.this,"Cannot open this link",Toast.LENGTH_SHORT).show();
                           }

                    }
                });

                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        if (percentage == 1f && isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.VISIBLE);
            mHeaderTitle.setTextColor(Color.WHITE);
            mHeaderSubTitle.setTextColor(Color.WHITE);
            isHideToolbarView = !isHideToolbarView;

        } else if (percentage < 1f && !isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.GONE);
            isHideToolbarView = !isHideToolbarView;
        }
    }
}
