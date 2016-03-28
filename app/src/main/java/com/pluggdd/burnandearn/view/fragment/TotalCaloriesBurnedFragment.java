package com.pluggdd.burnandearn.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pluggdd.burnandearn.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *  interface
 * to handle interaction events.
 * Use the {@link TotalCaloriesBurnedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TotalCaloriesBurnedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CALORIES_BURNED = "calories_burned";
    private static final String ARG_CALORIES_AVERAGE = "calories_average";

    // TODO: Rename and change types of parameters
    private double mCaloriesBurned;
    private int mCaloriesAverage;
    private TextView mTotalCaloriedBurnedText,mCaloriesBurntText,mCaloriesUnitText,mCaloriesAverageText;

    public TotalCaloriesBurnedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param calories_burned Parameter 1.

     * @param caloriesAverage
     * @return A new instance of fragment TotalCaloriesBurnedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TotalCaloriesBurnedFragment newInstance(double calories_burned, int caloriesAverage) {
        TotalCaloriesBurnedFragment fragment = new TotalCaloriesBurnedFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_CALORIES_BURNED, calories_burned);
        args.putInt(ARG_CALORIES_AVERAGE, caloriesAverage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCaloriesBurned = getArguments().getDouble(ARG_CALORIES_BURNED);
            mCaloriesAverage = getArguments().getInt(ARG_CALORIES_AVERAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities_text, container, false);
        mCaloriesBurntText = (TextView) view.findViewById(R.id.txt_activities_header);
        mTotalCaloriedBurnedText = (TextView) view.findViewById(R.id.txt_activities_detail);
        mCaloriesUnitText = (TextView) view.findViewById(R.id.txt_activities_unit);
        mCaloriesAverageText = (TextView) view.findViewById(R.id.txt_activities_average);
        mCaloriesBurntText.setText(getString(R.string.calories_burnt));
        mTotalCaloriedBurnedText.setText(String.valueOf(Math.round(mCaloriesBurned)));
        mCaloriesUnitText.setText(getString(R.string.calories_unit));
        mCaloriesAverageText.setText("Your goal is "+ mCaloriesAverage+ " cal");
        return view;
    }


}
