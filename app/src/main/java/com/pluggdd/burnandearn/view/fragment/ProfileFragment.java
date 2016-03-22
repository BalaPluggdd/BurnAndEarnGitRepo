package com.pluggdd.burnandearn.view.fragment;


import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pluggdd.burnandearn.BuildConfig;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.NetworkCheck;
import com.pluggdd.burnandearn.utils.PicassoImageLoaderHelper;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.pluggdd.burnandearn.utils.VolleySingleton;
import com.pluggdd.burnandearn.utils.WebserviceAPI;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String FIRST_NAME="FirstName",LASTNAME="LastName",EMAIL="email",PROFILE_URL="profile_url",GENDER="gender",DOB="dob",SOURCE_PAGE_FLAG="source_flag";
    public  static final int IMAGE_CAPTURE_REQUEST_CODE = 1,PICK_IMAGE_REQUEST = 2,REQUEST_BOTH_PERMISSION_CODE = 3;
    private FragmentInteraction mFragmentInteraction;
    private Button mSubmitButton;
    private PreferencesManager mPreferenceManager;
    private String mEmail,mFirstName,mLastName,mFacebookId,mProfileImageUrl,mDateofBirth;
    private int mGender = -1; // 0 - male , 1 - Female , 2 - Other
    private ImageView mProfileImage;
    private EditText mFirstNameEdt,mLastNameEdt,mEmailEdt,mDateOfBirthEdt;
    private RadioButton mMaleOption,mFemaleOption,mOtherOption;
    private ProgressBar mLoadingProgress;
    private Spinner mGoalSetUpSpinner;
    private DatePickerDialog mDatePickerDialog;
    private String mSourcePageFlag,mCapturedImagePath,mImageBase64;
    private boolean mIsPermissionRequestRaised;
    private View mView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment getInstance(String source_flag){
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SOURCE_PAGE_FLAG,source_flag);
        profileFragment.setArguments(bundle);
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mSourcePageFlag = getArguments().getString(SOURCE_PAGE_FLAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_profile, container, false);
        mProfileImage = (ImageView) mView.findViewById(R.id.img_profile);
        mFirstNameEdt = (EditText) mView.findViewById(R.id.edt_first_name);
        mLastNameEdt = (EditText) mView.findViewById(R.id.edt_last_name);
        mEmailEdt = (EditText) mView.findViewById(R.id.edt_email);
        mDateOfBirthEdt = (EditText) mView.findViewById(R.id.edt_dob);
        mSubmitButton = (Button) mView.findViewById(R.id.btn_submit);
        mMaleOption = (RadioButton) mView.findViewById(R.id.rbtn_male);
        mFemaleOption = (RadioButton) mView.findViewById(R.id.rbtn_female);
        mOtherOption = (RadioButton) mView.findViewById(R.id.rbtn_other);
        mGoalSetUpSpinner = (Spinner) mView.findViewById(R.id.spinner_goal);
        mLoadingProgress = (ProgressBar) mView.findViewById(R.id.loading_progress_bar);
        mPreferenceManager = new PreferencesManager(getContext());
        // To populate values
        new PicassoImageLoaderHelper(getContext(),mProfileImage,mLoadingProgress).loadImage(mPreferenceManager.getStringValue(getString(R.string.profile_image_url)));
        mFirstNameEdt.setText(mPreferenceManager.getStringValue(getString(R.string.first_name)));
        mLastNameEdt.setText(mPreferenceManager.getStringValue(getString(R.string.last_name)));
        mEmailEdt.setText(mPreferenceManager.getStringValue(getString(R.string.email)));
        mFacebookId = mPreferenceManager.getStringValue(getString(R.string.facebookId));
        mProfileImageUrl = mPreferenceManager.getStringValue(getString(R.string.profile_image_url));
        mGender = mPreferenceManager.getIntValue(getString(R.string.gender));
        if(mGender == 0){
            mMaleOption.setChecked(true);
        }else if(mGender == 1){
            mFemaleOption.setChecked(true);
        }else if(mGender == 2){
            mOtherOption.setChecked(true);
        }
        mDateOfBirthEdt.setText(mPreferenceManager.getStringValue(getString(R.string.dob)));
        mFirstNameEdt.setSelection(mFirstNameEdt.getText().toString().length());

        mGoalSetUpSpinner.setSelection(mPreferenceManager.getIntValue(getString(R.string.user_goal)),true);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().openContextMenu(mProfileImage);
            }
        });

        registerForContextMenu(mProfileImage);
        mDateOfBirthEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                mDatePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                       mDateOfBirthEdt.setText(String.format("%02d",dayOfMonth)+"-"+String.format("%02d",(monthOfYear+1))+"-"+year);
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
                mDatePickerDialog.show();
            }

        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirstName = mFirstNameEdt.getText().toString();
                mLastName = mLastNameEdt.getText().toString();
                mEmail = mEmailEdt.getText().toString();
                mDateofBirth = mDateOfBirthEdt.getText().toString();
                if(mMaleOption.isChecked())
                    mGender = 0;
                else if(mFemaleOption.isChecked())
                    mGender = 1;
                else
                    mGender = 2;

               if(TextUtils.isEmpty(mFirstName)){
                   Snackbar.make(mView, "Enter first name", Snackbar.LENGTH_SHORT).show();
               }else if(TextUtils.isEmpty(mLastName)){
                   Snackbar.make(mView, "Enter last name", Snackbar.LENGTH_SHORT).show();
               }else if(TextUtils.isEmpty(mEmail)){
                   Snackbar.make(mView, "Enter email address", Snackbar.LENGTH_SHORT).show();
               }else if(!Patterns.EMAIL_ADDRESS.matcher(mEmailEdt.getText().toString()).matches()){
                   Snackbar.make(mView, "Enter valid email address", Snackbar.LENGTH_SHORT).show();
               }else if(TextUtils.isEmpty(mDateofBirth)){
                   Snackbar.make(mView, "Choose date of birth", Snackbar.LENGTH_SHORT).show();
               }else if(mGender == -1){
                   Snackbar.make(mView, "Choose any gender", Snackbar.LENGTH_SHORT).show();
               }else{
                   if(new NetworkCheck().ConnectivityCheck(getActivity())){
                       new UpdateProfileTask().execute();
                   }else{
                       Snackbar.make(mView, "Please check your network connectivity", Snackbar.LENGTH_SHORT).show();
                   }
               }
            }
        });

        return  mView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select Image Source");
        menu.add(0, v.getId(), 0, "Camera");
        menu.add(0, v.getId(), 1, "Gallery");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Camera") {
              checkPermissionAndLaunchCameraIntent();
        } else if (item.getTitle() == "Gallery") {
            Intent intent = new Intent();
            intent.setType("image/jpeg");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK){
            switch (requestCode){
                case IMAGE_CAPTURE_REQUEST_CODE:
                    mProfileImage.setImageBitmap(decodeSampledBitmapFromFile(mCapturedImagePath, mProfileImage.getWidth()*2, mProfileImage.getHeight()*2));
                    break;
                case PICK_IMAGE_REQUEST:
                    mProfileImage.setImageBitmap(decodeSampledBitmapFromStream(data, mProfileImage.getWidth(), mProfileImage.getHeight()));
                    break;
            }

        }
    }

    private void checkPermissionAndLaunchCameraIntent() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<String>();
            final List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.CAMERA))
                permissionsNeeded.add("Camera");
            if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                permissionsNeeded.add("External Storage");
            if (permissionsList.size() > 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_BOTH_PERMISSION_CODE);
                return;
            }
            dispatchTakePictureIntent(IMAGE_CAPTURE_REQUEST_CODE);
        } else {
            dispatchTakePictureIntent(IMAGE_CAPTURE_REQUEST_CODE);
        }

    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        Log.i("Permission Result", "onRequestPermissionResult");
        switch (requestCode) {
            case REQUEST_BOTH_PERMISSION_CODE: {
                mIsPermissionRequestRaised = true;
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.GET_ACCOUNTS, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent(IMAGE_CAPTURE_REQUEST_CODE);
                } else {
                    Snackbar.make(
                            mView,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.settings, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Build intent that displays the App settings screen.
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .show();

                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        /*String image_path;
        if(path != null && !path.equalsIgnoreCase("")){
            image_path = path;
        }else {
            image_path = mCapturedImagePath;
        }*/
        Log.i("Image path", path);
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap resized_bitmap = BitmapFactory.decodeFile(path, options);
        convertImageto64(resized_bitmap);
        return resized_bitmap;
    }

    public Bitmap decodeSampledBitmapFromStream(Intent intent,int reqWidth, int reqHeight) {
        try {
            Uri uri = intent.getData();
            InputStream is = getActivity().getContentResolver().openInputStream(uri);
             // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is,null,options);
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap resized_bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri), null, options);
            convertImageto64(resized_bitmap);
            return resized_bitmap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight  && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void convertImageto64(Bitmap profile_photo){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        profile_photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        mImageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private class UpdateProfileTask extends AsyncTask<String,Void,Void> {

        private ProgressDialog mLoadingProgress;

        public UpdateProfileTask(){
            mLoadingProgress = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingProgress.setMessage("Updating please wait !!!");
            mLoadingProgress.setCancelable(false);
            mLoadingProgress.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            if(mImageBase64 == null){
                try {
                    Bitmap profile_photo = BitmapFactory.decodeStream((InputStream) new URL(mProfileImageUrl).getContent());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    profile_photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    mImageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);
                }catch (Exception e){
                    e.printStackTrace();
                    mImageBase64="";
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            updateProfile(mLoadingProgress);
        }
    }

    private void updateProfile(final ProgressDialog mLoadingProgress) {
        mLoadingProgress.show();
        RequestQueue mRequestQueue = VolleySingleton.getSingletonInstance().getRequestQueue();
        mRequestQueue.add((new StringRequest(Request.Method.POST, WebserviceAPI.LOGIN_AND_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(" login response", response);
                mLoadingProgress.dismiss();
                if (response != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response);
                        if (responseJson.optInt("status") == 1 || responseJson.optInt("status") == 2 ) {
                            PreferencesManager mPreferenceManager = new PreferencesManager(getActivity());
                            mPreferenceManager.setStringValue(getString(R.string.user_id), responseJson.optString("userid"));
                            mPreferenceManager.setStringValue(getString(R.string.facebook_share), responseJson.optString("facebook_share"));
                            mPreferenceManager.setStringValue(getString(R.string.first_name), responseJson.optString("firstname"));
                            mPreferenceManager.setStringValue(getString(R.string.last_name), responseJson.optString("lastname"));
                            mPreferenceManager.setStringValue(getString(R.string.email), responseJson.optString("emailid"));
                            mPreferenceManager.setStringValue(getString(R.string.profile_image_url), responseJson.optString("profilepicture"));
                            mPreferenceManager.setIntValue(getString(R.string.gender), responseJson.optInt("gender"));
                            mPreferenceManager.setStringValue(getString(R.string.dob), responseJson.optString("dob").split("-")[2] + "-" + responseJson.optString("dob").split("-")[1] + "-" + responseJson.optString("dob").split("-")[0]);
                            mPreferenceManager.setIntValue(getString(R.string.user_goal), mGoalSetUpSpinner.getSelectedItemPosition());
                            if(responseJson.optString("lastcaloriesupdate")!= null && !responseJson.optString("lastcaloriesupdate").equalsIgnoreCase("") && !responseJson.optString("lastcaloriesupdate").startsWith("0000")){
                                LocalDateTime lastUpdateddatetime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseLocalDateTime(responseJson.optString("lastcaloriesupdate"));
                                mPreferenceManager.setLongValue(getString(R.string.last_updated_calories_time), lastUpdateddatetime.toDateTime().getMillis());
                            }else
                                mPreferenceManager.setLongValue(getString(R.string.last_updated_calories_time), 0);

                            if(mSourcePageFlag.equalsIgnoreCase("login")){ // From login fragment
                                mPreferenceManager.setBooleanValue(getString(R.string.is_goal_set), true);
                                Bundle bundle = new Bundle();
                                bundle.putString(getString(R.string.page_flag), ProfileFragment.class.getSimpleName());
                                mFragmentInteraction.changeFragment(bundle);
                            }else{
                                Snackbar.make(mView, "Profile Updated Successfully", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(mView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(mView, "Failure response from server", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoadingProgress.dismiss();
                Snackbar.make(mView, "Unable to connect to server", Snackbar.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("fbId", mFacebookId);
                params.put("firstName", mFirstName);
                params.put("lastName", mLastName);
                params.put("emailId", mEmail);
                params.put("image",mImageBase64);
                params.put("dob",mDateofBirth.split("-")[2]+"-"+mDateofBirth.split("-")[1]+"-"+mDateofBirth.split("-")[0]);
                params.put("usergoal",getSelectedUserGoal());
                params.put("gender",String.valueOf(mGender));
                params.put("deviceId",mPreferenceManager.getStringValue(getString(R.string.gcm_reg_id)));
                params.put("deviceType",String.valueOf(0));
                return params;
            }
        }));
    }

    private String getSelectedUserGoal() {

        switch (mGoalSetUpSpinner.getSelectedItemPosition()){
            case 0:
                return  "1000";
            case  1:
                return "2000";
            case 2:
                return "3000";
            case 3:
                return "4000";
            case 4:
                return "5000";
            case 5:
                return "6000";
            default:
                return "1000";
        }
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
        mImageBase64 = null;
    }

    protected void dispatchTakePictureIntent(int request_code) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCapturedImagePath = photoFile.getAbsolutePath();
                Uri fileUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(takePictureIntent, request_code);
            } else {
                Toast.makeText(getActivity(), "Failed to create image file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() {
        try {
            File newFolder = new File(Environment.getExternalStorageDirectory(), "BurnAndEarn");
            if (!newFolder.exists()) {
                newFolder.mkdir();
            }
            try {
                File file = new File(newFolder, "BurnAndEarn_" + new Date().getTime() + ".jpg");
                file.createNewFile();
                return file;
            } catch (Exception ex) {
                System.out.println("ex: " + ex);
            }
        } catch (Exception e) {
            System.out.println("e: " + e);
        }
        return null;
    }

}
