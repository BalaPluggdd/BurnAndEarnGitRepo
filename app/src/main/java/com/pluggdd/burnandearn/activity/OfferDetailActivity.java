package com.pluggdd.burnandearn.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.utils.CustomCountDownTimer;
import com.pluggdd.burnandearn.utils.HeaderView;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;
import com.pluggdd.burnandearn.view.adapter.OfferRewardsAdapter;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OfferDetailActivity extends AppCompatActivity{

    private Toolbar mToolBar;
    private ImageView mBusinessLogo,mOfferImage;
    private LinearLayout mBurnAndLogoContainer,mPointsNeededContainer,mDaysEndsInContainer;
    private TextView mOfferPromoText,mPointsNeededText,mBusinessTitleText,mBusinessAddressText,mWebsiteText,mExpirationDateText,mDaysText,mHoursText,mMinutesText,mTermsAndConditionsText,mPhoneImage,mLocationImage;
    private ProgressBar mLogoProgressBar,mOfferImageProgressBar;
    private Button mRedeemButton;
    private Bundle mExtra;
    private int mBusinesId,mPhoneNumber;
    private String mAddress;
    private PreferencesManager mPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBusinessLogo = (ImageView) findViewById(R.id.img_business_logo);
        mOfferImage = (ImageView) findViewById(R.id.img_offer_image);
        mOfferPromoText = (TextView) findViewById(R.id.txt_offer_promo);
        mBurnAndLogoContainer = (LinearLayout) findViewById(R.id.burn_and_logo_container);
        mPointsNeededContainer = (LinearLayout) findViewById(R.id.points_needed_container);
        mPointsNeededText = (TextView) findViewById(R.id.txt_points_needed);
        mBusinessTitleText = (TextView) findViewById(R.id.txt_business_title);
        mBusinessAddressText = (TextView) findViewById(R.id.txt_business_address);
        mWebsiteText = (TextView) findViewById(R.id.txt_business_website);
        mExpirationDateText = (TextView) findViewById(R.id.txt_valid_till);
        mPhoneImage = (TextView) findViewById(R.id.img_phone);
        mLocationImage = (TextView) findViewById(R.id.img_location);
        mDaysEndsInContainer = (LinearLayout) findViewById(R.id.txt_countdown_container);
        mDaysText = (TextView) findViewById(R.id.txt_days);
        mHoursText = (TextView) findViewById(R.id.txt_hours);
        mMinutesText = (TextView) findViewById(R.id.txt_minutes);
        mTermsAndConditionsText = (TextView) findViewById(R.id.txt_terms_and_conditions);
        mLogoProgressBar = (ProgressBar) findViewById(R.id.logo_progress_bar);
        mOfferImageProgressBar = (ProgressBar) findViewById(R.id.offer_image_progress_bar);
        mRedeemButton = (Button) findViewById(R.id.btn_redeem);
        mPreferenceManager = new PreferencesManager(this);
        mExtra = getIntent().getExtras();
        new PicassoImageLoaderHelper(this,mBusinessLogo,mLogoProgressBar).loadImage(mExtra.getString(getString(R.string.logo_url), ""));
        new PicassoImageLoaderHelper(this,mOfferImage,mOfferImageProgressBar).loadImage(mExtra.getString(getString(R.string.offer_image_url), ""));
        mBusinesId = mExtra.getInt(getString(R.string.business_id));
        getSupportActionBar().setTitle(mExtra.getString(getString(R.string.offer_name), "Burn And Offer"));
        mBusinessTitleText.setText(mExtra.getString(getString(R.string.business_name), ""));
        mOfferPromoText.setText(mExtra.getString(getString(R.string.offer_name), ""));
        mPointsNeededText.setText(String.valueOf(mExtra.getInt(getString(R.string.points_needed), 0)));
        mAddress = mExtra.getString(getString(R.string.address), "");
        mBusinessAddressText.setText(mAddress);
        mWebsiteText.setText(mExtra.getString(getString(R.string.website,"")));
        String expiration_date = mExtra.getString(getString(R.string.expire_date), "");
        LocalDateTime expirationDateTime = new LocalDateTime(expiration_date);
        mExpirationDateText.setText(expirationDateTime.toString("MMM dd,YYYY"));
        mPhoneNumber = mExtra.getInt(getString(R.string.phone_number));
        mTermsAndConditionsText.setText(mExtra.getString(getString(R.string.terms_and_conditions)));
        long date_difference = expirationDateTime.plusDays(1).toDateTime().getMillis() - new LocalDateTime().toDateTime().getMillis();
        //date_difference = new LocalDateTime().plusMinutes(1).toDateTime().getMillis() - new LocalDateTime().toDateTime().getMillis();
        new CustomCountDownTimer(OfferDetailActivity.this,mDaysEndsInContainer,mDaysText,mHoursText,mMinutesText,null,mRedeemButton,null,expiration_date,"offer_detail",date_difference,1000).start();

        mPhoneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String uri = "tel:" + mPhoneNumber;
                    Intent mCallIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
                    startActivity(mCallIntent);
                } catch (Exception e) {
                    Toast.makeText(OfferDetailActivity.this, "Your call has failed...", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        mLocationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri locationUri = Uri.parse("geo:0,0?q=" +mAddress);
                    Intent mMapIntent = new Intent(Intent.ACTION_VIEW, locationUri);
                    mMapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mMapIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(OfferDetailActivity.this, "Maps not available...", Toast.LENGTH_LONG).show();
                }
            }
        });


        mRedeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //addMyOffer();
                if(mRedeemButton.getText().toString().equalsIgnoreCase(getString(R.string.redeem))){
                    AlertDialog.Builder mConfirmDialog = new AlertDialog.Builder(OfferDetailActivity.this,R.style.AlertDialogStyle);
                    String message = getString(R.string.confirm_redeem_dialog_content)+" '"+mOfferPromoText.getText().toString().toUpperCase() +"' AT " + mBusinessTitleText.getText().toString().toUpperCase();
                    message += "\n\n YOUR ACCOUNT SHALL BE DEDUCTED OF "+mPointsNeededText.getText().toString()+" SWEAT POINTS";
                    mConfirmDialog.setMessage((message));
                    mConfirmDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(new NetworkCheck().ConnectivityCheck(OfferDetailActivity.this)){
                                addMyOffer();
                            }else {
                                Toast.makeText(OfferDetailActivity.this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    mConfirmDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    mConfirmDialog.show();

                }

            }
        });
    }

    private void showCouponDialog() {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private void addMyOffer(){
        final ProgressDialog progressDialog = new ProgressDialog(OfferDetailActivity.this);
        progressDialog.setMessage("Adding offer please wait !!!");
        progressDialog.setCancelable(false);
        progressDialog.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.ADD_MY_OFFER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                if (response != null) {
                    try {
                        progressDialog.dismiss();
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1) {
                                mBurnAndLogoContainer.setVisibility(View.VISIBLE);
                                mPointsNeededContainer.setVisibility(View.GONE);
                                mRedeemButton.setText("Coupon code : " + mExtra.getString(getString(R.string.coupon)));
                            //showCouponDialog();
                        }else{
                            Toast.makeText(OfferDetailActivity.this,responseJson.optString("msg"),Toast.LENGTH_SHORT).show();
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
                params.put("userid",mPreferenceManager.getStringValue(getString(R.string.user_id)));
                params.put("businessid",String.valueOf(mBusinesId));
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }
}
