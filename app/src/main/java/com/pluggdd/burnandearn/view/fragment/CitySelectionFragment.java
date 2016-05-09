package com.pluggdd.burnandearn.view.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.model.City;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;
import com.pluggdd.burnandearn.view.adapter.CityAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CitySelectionFragment extends Fragment {

    private FragmentInteraction mListener;
    private Spinner mCitySpinner;
    private int mSelectedCityID = -1;
    private View mView;
    private Context mContext;
    private ArrayList<City> mCityList = new ArrayList<>();

    public CitySelectionFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_city_selection, container, false);
        mCitySpinner = (Spinner) mView.findViewById(R.id.spinner_city);
        mCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                City selectedCity = mCityList.get(position);
                mSelectedCityID = selectedCity.getId();
                if(mSelectedCityID != -1){
                    new PreferencesManager(mContext).setIntValue(getString(R.string.selected_city),mSelectedCityID);
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.page_flag), CitySelectionFragment.this.getClass().getSimpleName());
                    mListener.changeFragment(bundle);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getcityList();
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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

    private void getcityList() {
        final ProgressDialog mLoadingProgress = new ProgressDialog(mContext);
        mLoadingProgress.setMessage("Loading city please wait!!!");
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.GET, WebserviceAPI.CITY_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("city list response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        mCityList = new ArrayList<City>();
                        if (responseJson.optInt("status") == 1) {
                            JSONArray cityArray = responseJson.optJSONArray("citylist");
                            City defaultCity = new City();
                            defaultCity.setId(-1);
                            defaultCity.setName("Select City");
                            mCityList.add(defaultCity);
                            if(cityArray != null && cityArray.length() > 0){
                                for(int i=0 ; i< cityArray.length() ; i++){
                                    JSONObject cityObject = cityArray.getJSONObject(i);
                                    City city = new City();
                                    city.setId(cityObject.optInt("cityid"));
                                    city.setName(cityObject.optString("cityname"));
                                    mCityList.add(city);
                                }
                                mCitySpinner.setAdapter(new CityAdapter(mContext,mCityList));
                            }else{
                                Snackbar.make(mView, "We are adding up city please wait", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(mView, responseJson.optString("msg"), Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(mView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                    }
                    mLoadingProgress.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoadingProgress.dismiss();
                Snackbar.make(mView, "Unable to connect to our server", Snackbar.LENGTH_SHORT).show();
            }
        }));
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }
}
