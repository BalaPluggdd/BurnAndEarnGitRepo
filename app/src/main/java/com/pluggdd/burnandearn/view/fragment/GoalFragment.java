package com.pluggdd.burnandearn.view.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.ChildFragmentInteraction;

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

        mUserGoalHeaderText.setText("We recommend " + mUserGoal +  " calories as goal for your healthy life");
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
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.page_flag), GoalFragment.class.getSimpleName());
                    mChildFragmentInteraction.changeChildFragment(bundle);
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

}

