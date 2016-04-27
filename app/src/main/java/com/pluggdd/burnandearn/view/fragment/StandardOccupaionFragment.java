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
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.ChildFragmentInteraction;
import com.pluggdd.burnandearn.utils.NetworkCheck;
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
public class StandardOccupaionFragment extends Fragment {

    public final static String BUNDLE_ARGS = "bundle";
    private ChildFragmentInteraction mChildFragmentInteraction;
    private AutoCompleteTextView mSociety;
    private Button mNextButton;
    private View mView;
    private Bundle mExtrasBundle;
    private Context mContext;


    public StandardOccupaionFragment() {
        // Required empty public constructor
    }


    public static StandardOccupaionFragment getInstance(Bundle bundle){
        StandardOccupaionFragment standardOccupationFragment = new StandardOccupaionFragment();
        Bundle studentBundle = new Bundle();
        studentBundle.putBundle(BUNDLE_ARGS,bundle);
        standardOccupationFragment.setArguments(studentBundle);
        return  standardOccupationFragment;
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
        mView = inflater.inflate(R.layout.fragment_standard_occupaion, container, false);
        mSociety = (AutoCompleteTextView) mView.findViewById(R.id.edt_society);
        mNextButton = (Button) mView.findViewById(R.id.btn_next);

        if(new NetworkCheck().ConnectivityCheck(mContext)){
            getStandardOccupationList();
        }else{
            Snackbar.make(mView,getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
        }

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mSociety.getText().toString())) {
                    Snackbar.make(mView, "Enter society", Snackbar.LENGTH_SHORT).show();
                } else {
                    if(new NetworkCheck().ConnectivityCheck(mContext)){
                        mExtrasBundle.putString(getString(R.string.paramter1),mSociety.getText().toString().trim());
                        mExtrasBundle.putString(getString(R.string.paramter2),"");
                        mExtrasBundle.putString(getString(R.string.paramter3),"");
                        mExtrasBundle.putString(getString(R.string.paramter4),"");
                        new WebserviceHelper(mContext,mView,StandardOccupaionFragment.class.getSimpleName(),mExtrasBundle,mChildFragmentInteraction).updateProfile();
                    }else {
                        Snackbar.make(mView,getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
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

    private void getStandardOccupationList() {
        final ProgressDialog mLoadingProgress = new ProgressDialog(mContext);
        mLoadingProgress.setMessage("Loading please wait!!!");
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.OCCUPATION_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("student list response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        ArrayList<String> mSocietyList = new ArrayList<>();
                        if (responseJson.optInt("status") == 1) {
                            JSONArray societyArray = responseJson.optJSONArray("societylist");
                            if(societyArray != null && societyArray.length() > 0){
                                for(int i=0 ; i< societyArray.length() ; i++){
                                    JSONObject societyObject = societyArray.getJSONObject(i);
                                    mSocietyList.add(societyObject.optString("societyname"));
                                }
                            }
                            mSociety.setAdapter(new ArrayAdapter<String>(mContext,R.layout.list_row_autocomplete_view,R.id.txt_autocomplete,mSocietyList));
                        } else {
                            Snackbar.make(mView, responseJson.optString("msg"), Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(mView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                    }finally {
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
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("flag", String.valueOf(mExtrasBundle.getInt(getString(R.string.standard_occupation)))); // 2 - for Homemaker occupation
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }

}

