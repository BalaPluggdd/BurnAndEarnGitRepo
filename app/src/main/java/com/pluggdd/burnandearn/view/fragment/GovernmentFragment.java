package com.pluggdd.burnandearn.view.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.ChildFragmentInteraction;

/**
 * A simple {@link Fragment} subclass.
 */
public class GovernmentFragment extends Fragment {

    private ChildFragmentInteraction mChildFragmentInteraction;
    private AutoCompleteTextView mGovernmentDepartment,mSocietyName;
    private Button mNextButton;
    private View mView;
    private Context mContext;

    public GovernmentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_government, container, false);
        mGovernmentDepartment = (AutoCompleteTextView) mView.findViewById(R.id.edt_gov_department_name);
        mSocietyName = (AutoCompleteTextView) mView.findViewById(R.id.edt_society);
        mNextButton = (Button) mView.findViewById(R.id.btn_next);

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mGovernmentDepartment.getText().toString())){
                    Snackbar.make(mView,"Enter department",Snackbar.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(mSocietyName.getText().toString())){
                    Snackbar.make(mView,"Enter society",Snackbar.LENGTH_SHORT).show();
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.page_flag), GovernmentFragment.class.getSimpleName());
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
