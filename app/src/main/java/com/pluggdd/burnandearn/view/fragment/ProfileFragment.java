package com.pluggdd.burnandearn.view.fragment;


import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.utils.PreferencesManager;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String FIRST_NAME="FirstName",LASTNAME="LastName",EMAIL="email",PROFILE_URL="profile_url",GENDER="gender",DOB="dob";
    private FragmentInteraction mFragmentInteraction;
    private Button mSubmitButton;
    private PreferencesManager mPreferenceManager;
    /*private String mEmail,mFirstName,mLastName,mProfileImageUrl,mDateofBirth,mGender;*/
    private ImageView mProfileImage;
    private EditText mFirstNameEdt,mLastNameEdt,mEmailEdt,mDateOfBirthEdt;
    private RadioButton mMaleOption,mFemaleOption,mOtherOption;
    private ProgressBar mLoadingProgress;
    private Spinner mGoalSetUpSpinner;
    private DatePickerDialog mDatePickerDialog;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        mProfileImage = (ImageView) view.findViewById(R.id.img_profile);
        mFirstNameEdt = (EditText) view.findViewById(R.id.edt_first_name);
        mLastNameEdt = (EditText) view.findViewById(R.id.edt_last_name);
        mEmailEdt = (EditText) view.findViewById(R.id.edt_email);
        mDateOfBirthEdt = (EditText) view.findViewById(R.id.edt_dob);
        mSubmitButton = (Button) view.findViewById(R.id.btn_submit);
        mMaleOption = (RadioButton) view.findViewById(R.id.rbtn_male);
        mFemaleOption = (RadioButton) view.findViewById(R.id.rbtn_female);
        mOtherOption = (RadioButton) view.findViewById(R.id.rbtn_other);
        mGoalSetUpSpinner = (Spinner) view.findViewById(R.id.spinner_goal);
        mLoadingProgress = (ProgressBar) view.findViewById(R.id.loading_progress_bar);
        mPreferenceManager = new PreferencesManager(getContext());
        // To populate values
        new PicassoImageLoaderHelper(getContext(),mProfileImage,mLoadingProgress).loadImage(mPreferenceManager.getStringValue(getString(R.string.profile_image_url)));
        mFirstNameEdt.setText(mPreferenceManager.getStringValue(getString(R.string.first_name)));
        mLastNameEdt.setText(mPreferenceManager.getStringValue(getString(R.string.last_name)));
        mEmailEdt.setText(mPreferenceManager.getStringValue(getString(R.string.email)));
        String dob = mPreferenceManager.getStringValue(getString(R.string.dob));
        String mGender = mPreferenceManager.getStringValue(getString(R.string.gender));
        if(mGender.equalsIgnoreCase("male")){
            mMaleOption.setChecked(true);
        }else if(mGender.equalsIgnoreCase("female")){
            mFemaleOption.setChecked(true);
        }else if(mGender.equalsIgnoreCase("other")){
            mOtherOption.setChecked(true);
        }
        if(!dob.equalsIgnoreCase(""))
            dob = dob.split("/")[1]+"/"+dob.split("/")[0]+"/"+dob.split("/")[2];
        mDateOfBirthEdt.setText(dob);
        mFirstNameEdt.setSelection(mFirstNameEdt.getText().toString().length());
        mDateOfBirthEdt.setInputType(InputType.TYPE_NULL);

        mDateOfBirthEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                mDatePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                       mDateOfBirthEdt.setText(String.format("%02d",dayOfMonth)+"/"+String.format("%02d",monthOfYear)+"/"+year);
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                mDatePickerDialog.show();
            }

        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreferenceManager.setBooleanValue(getString(R.string.is_goal_set), true);
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.page_flag), ProfileFragment.class.getSimpleName());
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
