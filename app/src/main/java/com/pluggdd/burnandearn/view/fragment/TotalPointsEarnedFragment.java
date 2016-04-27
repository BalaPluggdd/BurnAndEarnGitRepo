package com.pluggdd.burnandearn.view.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.model.BusinessDetails;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
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
 * Activities that contain this fragment must implement the
 *  interface
 * to handle interaction events.
 * Use the {@link TotalPointsEarnedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TotalPointsEarnedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POINTS_EARNED = "points_earned";
    private static final String ARG_POINTS_AVERAGE = "points_average";

    // TODO: Rename and change types of parameters
    private int mPointsEarned,mPointsAverage;
    private TextView mTotalPointsEarnedText,mTotalPointsText,mPointsUnitText,mPointsAverageText;


    public TotalPointsEarnedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param points_earned Parameter 1.
     * @return A new instance of fragment TotalCaloriesBurnedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TotalPointsEarnedFragment newInstance(int points_earned,int points_average) {
        TotalPointsEarnedFragment fragment = new TotalPointsEarnedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POINTS_EARNED, points_earned);
        args.putInt(ARG_POINTS_AVERAGE, points_average);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPointsEarned = getArguments().getInt(ARG_POINTS_EARNED);
            mPointsAverage = getArguments().getInt(ARG_POINTS_AVERAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities_text, container, false);
        mTotalPointsText = (TextView) view.findViewById(R.id.txt_activities_detail);
        mTotalPointsEarnedText = (TextView) view.findViewById(R.id.txt_activities_header);
        mPointsUnitText = (TextView) view.findViewById(R.id.txt_activities_unit);
        mPointsAverageText = (TextView) view.findViewById(R.id.txt_activities_average);
        mTotalPointsEarnedText.setText(getString(R.string.points_earned));
        mTotalPointsText.setText(String.valueOf(mPointsEarned));
        mPointsUnitText.setText(getString(R.string.points));
        mPointsAverageText.setText("Your daily avg is "+mPointsAverage);
        return view;
    }


}
