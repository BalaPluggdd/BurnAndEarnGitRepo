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
 * Use the {@link TotalDistanceTravelledFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TotalDistanceTravelledFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DISTANCE_TRAVELLED = "distance_travelled";
    private static final String ARG_DISTANCE_AVERAGE = "distance_average";

    // TODO: Rename and change types of parameters
    private double mDistanceTravelled,mDistanceAverage;
    private TextView mTotalDistanceTravelledText,mDistaceTravelledText,mDistanceUnitText,mDistanceAverageText;

    public TotalDistanceTravelledFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param distance_travelled Parameter 1.
     * @param distanceAverage
     * @return A new instance of fragment TotalCaloriesBurnedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TotalDistanceTravelledFragment newInstance(double distance_travelled, double distanceAverage) {
        TotalDistanceTravelledFragment fragment = new TotalDistanceTravelledFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_DISTANCE_TRAVELLED, distance_travelled);
        args.putDouble(ARG_DISTANCE_AVERAGE, distanceAverage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDistanceTravelled = getArguments().getDouble(ARG_DISTANCE_TRAVELLED);
            mDistanceAverage =  getArguments().getDouble(ARG_DISTANCE_AVERAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities_text, container, false);
        mTotalDistanceTravelledText = (TextView) view.findViewById(R.id.txt_activities_detail);
        mDistaceTravelledText = (TextView) view.findViewById(R.id.txt_activities_header);
        mDistanceUnitText = (TextView) view.findViewById(R.id.txt_activities_unit);
        mDistanceAverageText = (TextView) view.findViewById(R.id.txt_activities_average);
        mDistaceTravelledText.setText(getString(R.string.distance_travelled));
        mTotalDistanceTravelledText.setText(String.format("%.2f",mDistanceTravelled));
        mDistanceUnitText.setText(getString(R.string.distance_dimension));
        mDistanceAverageText.setText("Your avg is " + String.format("%.2f",mDistanceAverage) + " km");
        return view;
    }


}
