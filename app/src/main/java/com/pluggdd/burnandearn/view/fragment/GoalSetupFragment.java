package com.pluggdd.burnandearn.view.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PreferencesManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class GoalSetupFragment extends Fragment {

    private FragmentInteraction mFragmentInteraction;
    private Button mSubmitButton;
    private PreferencesManager mPreferenceManager;

    public GoalSetupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_goal_set_up, container, false);
        mSubmitButton = (Button) view.findViewById(R.id.btn_submit);
        mPreferenceManager = new PreferencesManager(getContext());

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreferenceManager.setBooleanValue(getString(R.string.is_goal_set),true);
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.page_flag), GoalSetupFragment.class.getSimpleName());
                mFragmentInteraction.changeFragment(bundle);
            }
        });

        return  view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteraction) {
            mFragmentInteraction = (FragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentInteraction = null;
    }


}
