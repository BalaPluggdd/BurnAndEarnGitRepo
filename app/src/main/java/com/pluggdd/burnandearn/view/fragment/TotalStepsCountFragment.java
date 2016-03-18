package com.pluggdd.burnandearn.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentInteraction;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 *  interface
 * to handle interaction events.
 * Use the {@link TotalStepsCountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TotalStepsCountFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_STEPS_TAKEN = "steps_taken";
    private static final String ARG_DATE_FILTER = "date_filter";

    // TODO: Rename and change types of parameters
    private int mStepsTaken;
    private TextView mTotalStepsCountText,mStepCountText,mStepUnitText;
    public TotalStepsCountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param steps_taken Parameter 1.
     * @return A new instance of fragment TotalCaloriesBurnedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TotalStepsCountFragment newInstance(int steps_taken) {
        TotalStepsCountFragment fragment = new TotalStepsCountFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STEPS_TAKEN,steps_taken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStepsTaken = getArguments().getInt(ARG_STEPS_TAKEN);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities_text, container, false);
        mTotalStepsCountText = (TextView) view.findViewById(R.id.txt_activities_detail);
        mStepCountText = (TextView) view.findViewById(R.id.txt_activities_header);
        mStepUnitText = (TextView) view.findViewById(R.id.txt_activities_unit);
        mStepCountText.setText(getString(R.string.steps_taken));
        mTotalStepsCountText.setText(String.valueOf(mStepsTaken));
        mStepUnitText.setText(getString(R.string.steps_text));


        return view;
    }


}
