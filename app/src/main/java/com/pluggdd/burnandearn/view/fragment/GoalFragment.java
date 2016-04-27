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
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.ChildFragmentInteraction;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class GoalFragment extends Fragment {

    private ChildFragmentInteraction mChildFragmentInteraction;
    private EditText mGoalEdt;
    private Button mSubmitButton;
    private View mView;
    private TextView mUserGoalHeaderText;
    private Context mContext;
    private static String ARG_GOAL = "USER_GOAL";
    private int mUserGoal;
    private PreferencesManager mPreferenceManager;

    public static GoalFragment getInstance(int goal){
        GoalFragment goalFragment = new GoalFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_GOAL,goal);
        goalFragment.setArguments(bundle);
        return goalFragment;
    }


    public GoalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        mUserGoal = getArguments().getInt(ARG_GOAL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_goal_setup, container, false);
        mUserGoalHeaderText = (TextView) mView.findViewById(R.id.txt_user_goal_header);
        mGoalEdt = (EditText) mView.findViewById(R.id.edt_goal);
        mSubmitButton = (Button) mView.findViewById(R.id.btn_submit);
        mPreferenceManager = new PreferencesManager(mContext);
        mUserGoalHeaderText.setText("We recommend " + mUserGoal +" calories based on your inputs for maintaining a healthy lifestyle, but you can choose your goal too. Input your goal if you differ from ours");
        mGoalEdt.setText(String.valueOf(mUserGoal));

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String goal = mGoalEdt.getText().toString();
                if (TextUtils.isEmpty(goal)) {
                    Snackbar.make(mView, "Enter calories goal", Snackbar.LENGTH_SHORT).show();
                }else if(Integer.valueOf(goal) == 0){
                    Snackbar.make(mView, "Enter valid calories goal", Snackbar.LENGTH_SHORT).show();
                }else {
                    if(mPreferenceManager.getIntValue(getString(R.string.user_goal)) == Integer.valueOf(goal)){
                        mPreferenceManager.setIntValue(mContext.getString(R.string.user_goal),Integer.valueOf(goal));
                        Bundle bundle = new Bundle();
                        bundle.putString(getString(R.string.page_flag), GoalFragment.class.getSimpleName());
                        mChildFragmentInteraction.changeChildFragment(bundle);
                    }else{
                        updateUserGoal();
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

    private void updateUserGoal() {
        final ProgressDialog mLoadingProgress = new ProgressDialog(mContext);
        mLoadingProgress.setMessage("Updating please wait!!!");
        mLoadingProgress.setCancelable(false);
        mLoadingProgress.show();
        VolleySingleton volleyrequest = VolleySingleton.getSingletonInstance();
        RequestQueue mRequestQueue = volleyrequest.getRequestQueue();
        Request request = (new StringRequest(Request.Method.POST, WebserviceAPI.USER_GOAL_UPDATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("user goal response", response);
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1) {
                            mPreferenceManager.setIntValue(mContext.getString(R.string.user_goal),Integer.valueOf(mGoalEdt.getText().toString()));
                            Bundle bundle = new Bundle();
                            bundle.putString(getString(R.string.page_flag), GoalFragment.class.getSimpleName());
                            mChildFragmentInteraction.changeChildFragment(bundle);
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
                params.put("user_id", mPreferenceManager.getStringValue(mContext.getString(R.string.user_id)));
                params.put("usergoal",mGoalEdt.getText().toString().trim());
                return params;
            }
        });
        volleyrequest.setRequestPolicy(request);
        mRequestQueue.add(request);
    }

}

