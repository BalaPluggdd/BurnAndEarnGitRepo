package com.pluggdd.burnandearn.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;
import com.pluggdd.burnandearn.view.adapter.OfferRewardsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link interface
 * to handle interaction events.
 */
public class OffersAndRewardsFragment extends Fragment {

    private FragmentInteraction mListener;
    private RecyclerView mOfferAndRewardsRecyclerView;
    private ProgressBar mLoadingProgressBar;
    private TextView mNoOfferText;
    private ArrayList<BusinessDetails> mBusinessOfferList = new ArrayList<>();

    public OffersAndRewardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_offers_and_rewards, container, false);
        mLoadingProgressBar = (ProgressBar) view.findViewById(R.id.loading_progress_bar);
        mOfferAndRewardsRecyclerView = (RecyclerView) view.findViewById(R.id.offers_and_rewards_recycle_view);
        mNoOfferText = (TextView) view.findViewById(R.id.txt_no_offers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mOfferAndRewardsRecyclerView.setLayoutManager(layoutManager);
        getBusinessOfferList();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteraction) {
            mListener = (FragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void getBusinessOfferList(){
        RequestQueue mRequestQueue = VolleySingleton.getSingletonInstance().getRequestQueue();
        mRequestQueue.add((new StringRequest(Request.Method.GET, WebserviceAPI.ALL_BUSINESS_OFFER_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        mBusinessOfferList = new ArrayList<BusinessDetails>();
                        if (!responseJson.optString("msg").trim().equalsIgnoreCase("No Offer Found")) {
                            JSONArray business_list = responseJson.optJSONArray("businesslist");
                            if (business_list != null && business_list.length() > 0) {
                                for (int i = 0; i < business_list.length(); i++) {
                                    JSONObject business_object = business_list.optJSONObject(i);
                                    BusinessDetails businessDetails = new BusinessDetails();
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
                                    businessDetails.setTerms_and_conditions(business_object.optString("termsandconditions"));
                                    businessDetails.setCoupon(business_object.optString("couponCode"));
                                    mBusinessOfferList.add(businessDetails);
                                }
                                mLoadingProgressBar.setVisibility(View.GONE);
                                mNoOfferText.setVisibility(View.GONE);
                                mOfferAndRewardsRecyclerView.setVisibility(View.VISIBLE);
                                mOfferAndRewardsRecyclerView.setAdapter(new OfferRewardsAdapter(getActivity(),mBusinessOfferList,mListener,OffersAndRewardsFragment.this));

                            } else {
                                mLoadingProgressBar.setVisibility(View.GONE);
                                mNoOfferText.setVisibility(View.VISIBLE);
                                mOfferAndRewardsRecyclerView.setVisibility(View.GONE);
                                if(!isDetached())
                                    Toast.makeText(getContext(), "Business Offer list not found", Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            mLoadingProgressBar.setVisibility(View.GONE);
                            mNoOfferText.setVisibility(View.VISIBLE);
                            mOfferAndRewardsRecyclerView.setVisibility(View.GONE);
                            if(!isDetached())
                                Toast.makeText(getContext(), "Business Offer list not found", Toast.LENGTH_SHORT).show();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        mLoadingProgressBar.setVisibility(View.GONE);
                        mNoOfferText.setVisibility(View.VISIBLE);
                        mOfferAndRewardsRecyclerView.setVisibility(View.GONE);
                        if(!isDetached())
                            Toast.makeText(getContext(), "Failure response from server", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    mLoadingProgressBar.setVisibility(View.GONE);
                    mNoOfferText.setVisibility(View.VISIBLE);
                    mOfferAndRewardsRecyclerView.setVisibility(View.GONE);
                    if(isVisible())
                        Toast.makeText(getActivity(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        })));
    }

}
