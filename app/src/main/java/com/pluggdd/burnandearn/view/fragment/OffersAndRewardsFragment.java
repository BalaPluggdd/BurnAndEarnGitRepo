package com.pluggdd.burnandearn.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.pluggdd.burnandearn.model.FitnessActivity;
import com.pluggdd.burnandearn.model.FitnessHistory;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;
import com.pluggdd.burnandearn.view.adapter.OfferRewardsAdapter;

import org.joda.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link interface
 * to handle interaction events.
 */
public class OffersAndRewardsFragment extends Fragment {

    public static final String ARG_FLAG = "Flag";
    public static final String ARG_PAGE_FLAG = "PageFlag";
    private Context mContext;
    private View mView;
    private RecyclerView mOfferAndRewardsRecyclerView;
    private ProgressBar mLoadingProgressBar;
    private TextView mNoOfferText;
    private int mFlag;
    private String mPageFlag;
    private ArrayList<BusinessDetails> mBusinessOfferList = new ArrayList<>();
    private boolean mIsOfferListLoaded = false;

    public OffersAndRewardsFragment() {
        // Required empty public constructor
    }

    public static OffersAndRewardsFragment newInstance(int flag, String pageFlag){
        OffersAndRewardsFragment mOfferAndRewardFragment = new OffersAndRewardsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_FLAG, flag);
        bundle.putString(ARG_PAGE_FLAG,pageFlag);
        mOfferAndRewardFragment.setArguments(bundle);
        return mOfferAndRewardFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mFlag = getArguments().getInt(ARG_FLAG);
            mPageFlag = getArguments().getString(ARG_PAGE_FLAG);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(new NetworkCheck().ConnectivityCheck(mContext)){
                if(mLoadingProgressBar != null && mNoOfferText != null && mOfferAndRewardsRecyclerView != null){
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                    mNoOfferText.setVisibility(View.GONE);
                    mOfferAndRewardsRecyclerView.setVisibility(View.VISIBLE);
                    mOfferAndRewardsRecyclerView.setAdapter(new OfferRewardsAdapter(mContext,new ArrayList<BusinessDetails>(),mPageFlag));
                }
                getBusinessOfferList();
            }else{
                Toast.makeText(mContext,getString(R.string.no_network),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_offers_and_rewards, container, false);
        mLoadingProgressBar = (ProgressBar) mView.findViewById(R.id.loading_progress_bar);
        mOfferAndRewardsRecyclerView = (RecyclerView) mView.findViewById(R.id.offers_and_rewards_recycle_view);
        mNoOfferText = (TextView) mView.findViewById(R.id.txt_no_offers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mOfferAndRewardsRecyclerView.setLayoutManager(layoutManager);
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void getBusinessOfferList(){
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
         String url;
         if(mPageFlag.equalsIgnoreCase(getString(R.string.my_offers)) || mPageFlag.equalsIgnoreCase(getString(R.string.my_offers_inactive)))
             url = WebserviceAPI.MY_OFFERS;
         else
             url = WebserviceAPI.ALL_BUSINESS_OFFER_LIST;
        Request request = (new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        mBusinessOfferList = new ArrayList<BusinessDetails>();
                        if (responseJson.optInt("status") == 1) {
                            JSONArray business_list = responseJson.optJSONArray("businesslist");
                            if (business_list != null && business_list.length() > 0) {
                                for (int i = 0; i < business_list.length(); i++) {
                                    JSONObject business_object = business_list.optJSONObject(i);
                                    BusinessDetails businessDetails = new BusinessDetails();
                                    businessDetails.setId(Integer.valueOf(business_object.optInt("Businessid")));
                                    businessDetails.setName(business_object.optString("Business Name"));
                                    businessDetails.setOffer_name(business_object.optString("offerName"));
                                    businessDetails.setLogo(business_object.optString("Business Image"));
                                    businessDetails.setPromo(business_object.optString("offerText"));
                                    businessDetails.setPoints_needed(business_object.optInt("requiredPoints"));
                                    businessDetails.setCoupon_expiry_date(business_object.optString("endingDate"));
                                    businessDetails.setHow_to_reedem(business_object.optString("How to Redeem"));
                                    businessDetails.setUrl(business_object.optString("site"));
                                    businessDetails.setPhone_number(business_object.optInt("Phone No"));
                                    businessDetails.setAddress(business_object.optString("Address"));
                                    businessDetails.setOffer_type(business_object.optInt("onlinestatus"));
                                    businessDetails.setTerms_and_conditions(business_object.optString("termsandconditions"));
                                    businessDetails.setCoupon(business_object.optString("couponCode"));
                                    businessDetails.setOfferLogo(business_object.optString("offerimage"));
                                    mBusinessOfferList.add(businessDetails);
                                }
                                mLoadingProgressBar.setVisibility(View.GONE);
                                mNoOfferText.setVisibility(View.GONE);
                                mOfferAndRewardsRecyclerView.setVisibility(View.VISIBLE);
                                mOfferAndRewardsRecyclerView.setAdapter(new OfferRewardsAdapter(mContext,mBusinessOfferList,mPageFlag));
                            } else {
                                mLoadingProgressBar.setVisibility(View.GONE);
                                mNoOfferText.setVisibility(View.VISIBLE);
                                setNoOfferText();
                                mOfferAndRewardsRecyclerView.setVisibility(View.GONE);
                                /*if(!isDetached())
                                    Toast.makeText(getContext(), "Business Offer list not found", Toast.LENGTH_SHORT).show();*/
                            }

                        } else {
                            mLoadingProgressBar.setVisibility(View.GONE);
                            mNoOfferText.setVisibility(View.VISIBLE);
                            setNoOfferText();
                            mOfferAndRewardsRecyclerView.setVisibility(View.GONE);
                            /*if(!isDetached())
                                Toast.makeText(getContext(), "Business Offer list not found", Toast.LENGTH_SHORT).show();*/
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        mLoadingProgressBar.setVisibility(View.GONE);
                        mNoOfferText.setVisibility(View.VISIBLE);
                        setNoOfferText();
                        mOfferAndRewardsRecyclerView.setVisibility(View.GONE);
                        /*if(!isDetached())
                            Toast.makeText(getContext(), "Failure response from server", Toast.LENGTH_SHORT).show();*/
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    mLoadingProgressBar.setVisibility(View.GONE);
                    mNoOfferText.setVisibility(View.VISIBLE);
                    setNoOfferText();
                    mOfferAndRewardsRecyclerView.setVisibility(View.GONE);
                    /*if(isVisible())
                        Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();*/
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if(mPageFlag.equalsIgnoreCase(getString(R.string.my_offers)) || mPageFlag.equalsIgnoreCase(getString(R.string.my_offers_inactive)))
                    params.put("userId", new PreferencesManager(mContext).getStringValue(getString(R.string.user_id)));
                params.put("flag", String.valueOf(mFlag));
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }

    private void setNoOfferText(){
        if(mPageFlag.equalsIgnoreCase(mContext.getString(R.string.my_offers))) { // Active My Offers
            mNoOfferText.setText(mContext.getString(R.string.no_active_offer_text));
        }else if(mPageFlag.equalsIgnoreCase(mContext.getString(R.string.my_offers_inactive))) { // InActive My Offers
            mNoOfferText.setText(mContext.getString(R.string.no_inactive_offer_text));
        }else{
            mNoOfferText.setText(mContext.getString(R.string.no_offer));
        }
    }

}
