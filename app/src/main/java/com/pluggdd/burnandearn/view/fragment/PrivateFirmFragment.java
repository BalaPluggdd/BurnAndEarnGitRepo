package com.pluggdd.burnandearn.view.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.ChildFragmentInteraction;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;
import com.pluggdd.burnandearn.utils.WebserviceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrivateFirmFragment extends Fragment {

    public final static String BUNDLE_ARGS = "bundle";
    private ChildFragmentInteraction mChildFragmentInteraction;
    private AutoCompleteTextView mCompanyName,mBusinessUnit,mFunction,mSociety;
    private Button mNextButton;
    private View mView;
    private Context mContext;
    private Bundle mExtrasBundle;
    private PreferencesManager mPreferenceManager;


    public static PrivateFirmFragment getInstance(Bundle bundle) {
        PrivateFirmFragment privateFirmFragment = new PrivateFirmFragment();
        Bundle privateFirnBundle = new Bundle();
        privateFirnBundle.putBundle(BUNDLE_ARGS, bundle);
        privateFirmFragment.setArguments(privateFirnBundle);
        return privateFirmFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExtrasBundle = getArguments().getBundle(BUNDLE_ARGS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_private_firm, container, false);
        mCompanyName = (AutoCompleteTextView) mView.findViewById(R.id.edt_company_name);
        mBusinessUnit = (AutoCompleteTextView) mView.findViewById(R.id.edt_business_unit);
        mFunction = (AutoCompleteTextView) mView.findViewById(R.id.edt_function);
        mSociety = (AutoCompleteTextView) mView.findViewById(R.id.edt_society);
        mNextButton = (Button) mView.findViewById(R.id.btn_next);

        // Set default value if already entered
        mPreferenceManager = new PreferencesManager(mContext);
        String companyName = mPreferenceManager.getStringValue(getString(R.string.paramter1));
        if(!TextUtils.isEmpty(companyName))
            mCompanyName.setText(companyName);
        String businessUnit = mPreferenceManager.getStringValue(getString(R.string.paramter2));
        if(!TextUtils.isEmpty(businessUnit))
            mBusinessUnit.setText(businessUnit);
        String function = mPreferenceManager.getStringValue(getString(R.string.paramter3));
        if(!TextUtils.isEmpty(function))
            mFunction.setText(function);
        String society = mPreferenceManager.getStringValue(getString(R.string.paramter4));
        if(!TextUtils.isEmpty(society))
            mSociety.setText(society);

        if (new NetworkCheck().ConnectivityCheck(mContext)) {
            getPrivateFirmOccupationList();
        } else {
            Snackbar.make(mView, getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
        }

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mCompanyName.getText().toString())){
                    Snackbar.make(mView,"Enter company name",Snackbar.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(mBusinessUnit.getText().toString())){
                    Snackbar.make(mView,"Enter business unit",Snackbar.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(mFunction.getText().toString())){
                    Snackbar.make(mView,"Enter company function",Snackbar.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(mSociety.getText().toString())){
                    Snackbar.make(mView,"Enter society",Snackbar.LENGTH_SHORT).show();
                }else{
                    if (new NetworkCheck().ConnectivityCheck(mContext)) {
                        mExtrasBundle.putString(getString(R.string.paramter1), mCompanyName.getText().toString().trim());
                        mExtrasBundle.putString(getString(R.string.paramter2), mBusinessUnit.getText().toString().trim());
                        mExtrasBundle.putString(getString(R.string.paramter3), mFunction.getText().toString().trim());
                        mExtrasBundle.putString(getString(R.string.paramter4), mSociety.getText().toString().trim());
                        new WebserviceHelper(mContext, mView, PrivateFirmFragment.class.getSimpleName(), mExtrasBundle, mChildFragmentInteraction).updateProfile();
                    } else {
                        Snackbar.make(mView, getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });


        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mChildFragmentInteraction = (ChildFragmentInteraction) getParentFragment();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mChildFragmentInteraction = null;
    }

    private void getPrivateFirmOccupationList() {
        final ProgressDialog mLoadingProgress = new ProgressDialog(mContext);
        mLoadingProgress.setMessage("Loading please wait!!!");
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.OCCUPATION_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("private firm response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        ArrayList<String> mCompanyList = new ArrayList<>();
                        ArrayList<String> mBusinessUnitList = new ArrayList<>();
                        ArrayList<String> mFuncitonList = new ArrayList<>();
                        ArrayList<String> mSocietyList = new ArrayList<>();
                        if (responseJson.optInt("status") == 1) {
                            JSONArray collegeArray = responseJson.optJSONArray("companylist");
                            if (collegeArray != null && collegeArray.length() > 0) {
                                for (int i = 0; i < collegeArray.length(); i++) {
                                    JSONObject collegeObject = collegeArray.getJSONObject(i);
                                    mCompanyList.add(collegeObject.optString("companyname"));
                                }
                            }
                            JSONArray businessUnitArray = responseJson.optJSONArray("businessunitlist");
                            if (businessUnitArray != null && businessUnitArray.length() > 0) {
                                for (int i = 0; i < businessUnitArray.length(); i++) {
                                    JSONObject collegeObject = businessUnitArray.getJSONObject(i);
                                    mBusinessUnitList.add(collegeObject.optString("businessunitname"));
                                }
                            }
                            JSONArray functionArray = responseJson.optJSONArray("functionlist");
                            if (functionArray != null && functionArray.length() > 0) {
                                for (int i = 0; i < functionArray.length(); i++) {
                                    JSONObject collegeObject = functionArray.getJSONObject(i);
                                    mFuncitonList.add(collegeObject.optString("functionname"));
                                }
                            }
                            JSONArray societyArray = responseJson.optJSONArray("societylist");
                            if (societyArray != null && societyArray.length() > 0) {
                                for (int i = 0; i < societyArray.length(); i++) {
                                    JSONObject societyObject = societyArray.getJSONObject(i);
                                    mSocietyList.add(societyObject.optString("societyname"));
                                }
                            }
                            mCompanyName.setAdapter(new ArrayAdapter<String>(mContext, R.layout.list_row_autocomplete_view, R.id.txt_autocomplete, mCompanyList));
                            mBusinessUnit.setAdapter(new ArrayAdapter<String>(mContext, R.layout.list_row_autocomplete_view, R.id.txt_autocomplete, mBusinessUnitList));
                            mFunction.setAdapter(new ArrayAdapter<String>(mContext, R.layout.list_row_autocomplete_view, R.id.txt_autocomplete, mFuncitonList));
                            mSociety.setAdapter(new ArrayAdapter<String>(mContext, R.layout.list_row_autocomplete_view, R.id.txt_autocomplete, mSocietyList));
                        } else {
                            Snackbar.make(mView, responseJson.optString("msg"), Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(mView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                    } finally {
                        mLoadingProgress.dismiss();
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoadingProgress.dismiss();
                Snackbar.make(mView, "Unable to connect to our server", Snackbar.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("flag", "4"); // 4 - for private firm occupation
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }


}
