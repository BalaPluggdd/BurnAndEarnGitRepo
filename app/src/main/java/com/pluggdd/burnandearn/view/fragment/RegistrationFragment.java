package com.pluggdd.burnandearn.view.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.BurnAndEarnMainActivity;
import com.pluggdd.burnandearn.utils.ChildFragmentInteraction;
import com.pluggdd.burnandearn.utils.FragmentHelper;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PreferencesManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment implements ChildFragmentInteraction,View.OnClickListener {

    private static final String SOURCE_PAGE_FLAG = "Source_Flag";
    private FragmentInteraction mFragmentInteraction;
    private Context mContext;
    private LinearLayout mSteps1Container,mSteps2Container,mSteps3Container;
    private TextView mStep1Text, mStep1TitleText, mStep2Text, mStep2TitleText, mStep3Text, mStep3TitleText;
    private FragmentHelper mFragmentHelper;
    private View mView;
    private String mSourcePageFlag;
    private PreferencesManager mPreferencesManager;
    private boolean mIsProfileEdited,mIsOccupationSet,mIsGoalSet;

    public RegistrationFragment() {

    }

    public static RegistrationFragment getInstance(String source_flag) {
        RegistrationFragment registrationFragment = new RegistrationFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SOURCE_PAGE_FLAG, source_flag);
        registrationFragment.setArguments(bundle);
        return registrationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSourcePageFlag = getArguments().getString(SOURCE_PAGE_FLAG);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_registration, container, false);
        mSteps1Container = (LinearLayout) mView.findViewById(R.id.step1_container);
        mSteps2Container = (LinearLayout) mView.findViewById(R.id.step2_container);
        mSteps3Container = (LinearLayout) mView.findViewById(R.id.step3_container);
        mStep1Text = (TextView) mView.findViewById(R.id.txt_step1);
        mStep2Text = (TextView) mView.findViewById(R.id.txt_step2);
        mStep3Text = (TextView) mView.findViewById(R.id.txt_step3);
        mStep1TitleText = (TextView) mView.findViewById(R.id.txt_step1_title);
        mStep2TitleText = (TextView) mView.findViewById(R.id.txt_step2_title);
        mStep3TitleText = (TextView) mView.findViewById(R.id.txt_step3_title);
        mFragmentHelper = new FragmentHelper(getChildFragmentManager());
        mPreferencesManager = new PreferencesManager(mContext);
        mFragmentHelper.addFragment(R.id.registration_fragment_container, new ProfileFragment());
        //mSteps1Container.setOnClickListener(this);
        //mSteps2Container.setOnClickListener(this);
        //mSteps3Container.setOnClickListener(this);
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment mCurrentFragment = getChildFragmentManager().findFragmentById(R.id.registration_fragment_container);
        mCurrentFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Fragment mCurrentFragment = getChildFragmentManager().findFragmentById(R.id.registration_fragment_container);
        mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void changeChildFragment(Bundle extras) {
        switch (extras.getString(getString(R.string.page_flag))) {
            case "ProfileFragment":
                mIsProfileEdited = true;
                setHeaderStepsActive(2);
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                String selected_occupation = extras.getString(getString(R.string.occupation));
                switch (selected_occupation) {
                    case "Student":
                        mFragmentHelper.replaceFragment(R.id.registration_fragment_container, StudentFragment.getInstance(extras), false);
                        break;
                    case "Homemaker":
                        mFragmentHelper.replaceFragment(R.id.registration_fragment_container, new StandardOccupaionFragment(), false);
                        break;
                    case "Self-Employed":
                        mFragmentHelper.replaceFragment(R.id.registration_fragment_container, new SelfEmployeedFragment(), false);
                        break;
                    case "Private Company":
                        mFragmentHelper.replaceFragment(R.id.registration_fragment_container, new PrivateFirmFragment(), false);
                        break;
                    case "Government Service":
                        mFragmentHelper.replaceFragment(R.id.registration_fragment_container, new GovernmentFragment(), false);
                        break;
                    case "Retired":
                        mFragmentHelper.replaceFragment(R.id.registration_fragment_container, new StandardOccupaionFragment(), false);
                        break;
                    case "Not Working":
                        mFragmentHelper.replaceFragment(R.id.registration_fragment_container, new StandardOccupaionFragment(), false);
                        break;
                }

                break;
            case "StudentFragment":

            case "StandardOccupaionFragment":

            case "SelfEmployeedFragment":

            case "PrivateFirmFragment":

            case "GovernmentFragment":
                setHeaderStepsActive(3);
                mIsOccupationSet = true;
                mFragmentHelper.replaceFragment(R.id.registration_fragment_container, new GoalFragment(), false);
                break;
            case "GoalFragment":
                if (mSourcePageFlag.equalsIgnoreCase("login")) { // From login fragment
                    mPreferencesManager.setBooleanValue(getString(R.string.is_goal_set), true);
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.page_flag), RegistrationFragment.class.getSimpleName());
                    mFragmentInteraction.changeFragment(bundle);
                } else {
                    Snackbar.make(mView, "Profile Updated Successfully", Snackbar.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private void setHeaderStepsActive(int position) {
        switch (position){
            case 1:
                mStep1Text.setBackgroundResource(R.drawable.ic_orange_stepper_active);
                mStep1TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.secondary_text));
                mStep2Text.setBackgroundResource(R.drawable.ic_orange_stepper_inactive);
                mStep2TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.divider));
                mStep2Text.setBackgroundResource(R.drawable.ic_orange_stepper_inactive);
                mStep2TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.divider));
                break;
            case 2:
                mStep2Text.setBackgroundResource(R.drawable.ic_orange_stepper_active);
                mStep2TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.secondary_text));
                mStep1Text.setBackgroundResource(R.drawable.ic_orange_stepper_inactive);
                mStep1TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.divider));
                mStep3Text.setBackgroundResource(R.drawable.ic_orange_stepper_inactive);
                mStep3TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.divider));
                break;
            case 3:
                mStep3Text.setBackgroundResource(R.drawable.ic_orange_stepper_active);
                mStep3TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.secondary_text));
                mStep2Text.setBackgroundResource(R.drawable.ic_orange_stepper_inactive);
                mStep2TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.divider));
                mStep1Text.setBackgroundResource(R.drawable.ic_orange_stepper_inactive);
                mStep1TitleText.setTextColor(ContextCompat.getColor(mContext,R.color.divider));
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Fragment mCurrentFragment = getChildFragmentManager().findFragmentById(R.id.registration_fragment_container);
        switch (v.getId()){
            case R.id.step1_container:
                setHeaderStepsActive(1);
                if(mCurrentFragment instanceof ProfileFragment == false)
                     mFragmentHelper.replaceFragmentWithAlterAnim(R.id.registration_fragment_container,new ProfileFragment(),false);
                break;
            case R.id.step2_container:
                if(mIsOccupationSet && (mCurrentFragment instanceof ProfileFragment == false && mCurrentFragment instanceof  ProfileFragment == false)){
                    setHeaderStepsActive(2);
                    mFragmentHelper.replaceFragmentWithAlterAnim(R.id.registration_fragment_container,new ProfileFragment(),false);
                }
                break;
            case R.id.step3_container:
                if(mIsGoalSet && (mCurrentFragment instanceof  ProfileFragment == false))
                  setHeaderStepsActive(3);
                break;
        }
    }
}
