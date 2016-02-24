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
 * Use the {@link TotalCaloriesBurnedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TotalCaloriesBurnedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CALORIES_BURNED = "calories_burned";

    // TODO: Rename and change types of parameters
    private double mCaloriesBurned;

    private FragmentInteraction mListener;
    private TextView mTotalCaloriedBurnedText;

    public TotalCaloriesBurnedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param calories_burned Parameter 1.
     * @return A new instance of fragment TotalCaloriesBurnedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TotalCaloriesBurnedFragment newInstance(double calories_burned) {
        TotalCaloriesBurnedFragment fragment = new TotalCaloriesBurnedFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_CALORIES_BURNED, calories_burned);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCaloriesBurned = getArguments().getDouble(ARG_CALORIES_BURNED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities_text, container, false);
        mTotalCaloriedBurnedText = (TextView) view.findViewById(R.id.txt_activities_detail);
        String calories_text = Math.round(mCaloriesBurned) + " Calories \n Burned";
        mTotalCaloriedBurnedText.setText(calories_text);
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
}
