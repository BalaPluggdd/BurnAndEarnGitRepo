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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.model.City;
import com.pluggdd.burnandearn.utils.ChildFragmentInteraction;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;
import com.pluggdd.burnandearn.utils.WebserviceHelper;
import com.pluggdd.burnandearn.view.adapter.CityAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class StudentFragment extends Fragment {

    public final static String BUNDLE_ARGS = "bundle";
    private ChildFragmentInteraction mChildFragmentInteraction;
    private AutoCompleteTextView mInstutionName, mSociety;
    private Spinner mSchoolCollegeSpinner;
    private Button mNextButton;
    private View mView;
    private Context mContext;
    private Bundle mExtrasBundle;
    private ArrayList<String> mCollegeList,mSchoolList;

    public static StudentFragment getInstance(Bundle bundle){
        StudentFragment studentFragment = new StudentFragment();
        Bundle studentBundle = new Bundle();
        studentBundle.putBundle(BUNDLE_ARGS,bundle);
        studentFragment.setArguments(studentBundle);
        return  studentFragment;
    }


    public StudentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExtrasBundle = getArguments().getBundle(BUNDLE_ARGS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_student, container, false);
        mInstutionName = (AutoCompleteTextView) mView.findViewById(R.id.edt_institution_name);
        mSchoolCollegeSpinner = (Spinner) mView.findViewById(R.id.spinner_school_college);
        mSociety = (AutoCompleteTextView) mView.findViewById(R.id.edt_society);
        mNextButton = (Button) mView.findViewById(R.id.btn_next);

        if(new NetworkCheck().ConnectivityCheck(mContext)){
           getStudentOccupationList();
        }else{
            Snackbar.make(mView,getString(R.string.no_network), Snackbar.LENGTH_SHORT).show();
        }

        mSchoolCollegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){ // School
                    mInstutionName.getText().clear();
                    mInstutionName.setAdapter(new ArrayAdapter<String>(mContext,R.layout.list_row_autocomplete_view,R.id.txt_autocomplete,mSchoolList));
                }else{ // College
                    mInstutionName.getText().clear();
                    mInstutionName.setAdapter(new ArrayAdapter<String>(mContext,R.layout.list_row_autocomplete_view,R.id.txt_autocomplete,mCollegeList));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mInstutionName.getText().toString())) {
                    Snackbar.make(mView, "Enter institution name", Snackbar.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mSociety.getText().toString())) {
                    Snackbar.make(mView, "Enter society", Snackbar.LENGTH_SHORT).show();
                } else {
                    if(new NetworkCheck().ConnectivityCheck(mContext)){
                        mExtrasBundle.putString(getString(R.string.paramter1),mInstutionName.getText().toString().trim());
                        mExtrasBundle.putString(getString(R.string.paramter2),mSchoolCollegeSpinner.getSelectedItem().toString().trim());
                        mExtrasBundle.putString(getString(R.string.paramter3),mSociety.getText().toString().trim());
                        mExtrasBundle.putString(getString(R.string.paramter4),"");
                        new WebserviceHelper(mContext,mView,StudentFragment.class.getSimpleName(),mExtrasBundle,mChildFragmentInteraction).updateProfile();
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

    private void getStudentOccupationList() {
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
                        mCollegeList = new ArrayList<>();
                        mSchoolList = new ArrayList<>();
                        ArrayList<String> mSocietyList = new ArrayList<>();
                        if (responseJson.optInt("status") == 1) {
                            JSONArray collegeArray = responseJson.optJSONArray("collegelist");
                            if(collegeArray != null && collegeArray.length() > 0){
                                for(int i=0 ; i< collegeArray.length() ; i++){
                                    JSONObject collegeObject = collegeArray.getJSONObject(i);
                                    mCollegeList.add(collegeObject.optString("collegename"));
                                }
                            }
                            JSONArray schoolArray = responseJson.optJSONArray("schoollist");
                            if(schoolArray != null && schoolArray.length() > 0){
                                for(int i=0 ; i< schoolArray.length() ; i++){
                                    JSONObject schoolObject = schoolArray.getJSONObject(i);
                                    mSchoolList.add(schoolObject.optString("schoolname"));
                                }
                            }
                            JSONArray societyArray = responseJson.optJSONArray("societylist");
                            if(societyArray != null && societyArray.length() > 0){
                                for(int i=0 ; i< societyArray.length() ; i++){
                                    JSONObject societyObject = societyArray.getJSONObject(i);
                                    mSocietyList.add(societyObject.optString("societyname"));
                                }
                            }
                            mInstutionName.setAdapter(new ArrayAdapter<String>(mContext,R.layout.list_row_autocomplete_view,R.id.txt_autocomplete,mSchoolList));
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
                params.put("flag", "1"); // 1 - for student occupation
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }
}
