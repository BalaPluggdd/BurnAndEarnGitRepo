package com.pluggdd.burnandearn.view.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.activity.FitBitActivity;
import com.pluggdd.burnandearn.utils.FitnessSource;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.utils.PreferencesManager;


/**
 * Fragment to manage App Tour screens
 */
public class AppIntroductionFragment extends Fragment {

    // Iniitialization of views and variables

    public static final String SOURCE_FLAG = "SOURCE";
    private FragmentInteraction mFragmentInteraction;
    private ViewPager mAppIntroViewPager;
    private TextView mSkipText,mDoneText;
    private ImageView mViewPagerIndicator1Image,mViewPagerIndicator2Image,mViewPagerIndicator3Image,mViewPagerIndicator4Image,mViewPagerForwardCloseImage;
    private PreferencesManager mPreferenceManager;
    private Context mContext;
    private String mSourceFlag;

    public AppIntroductionFragment() {
        // Required empty public constructor
    }

    public static AppIntroductionFragment getInstance(String sourceFlag){ // False  if from Registration Process // True from Settings menu
        AppIntroductionFragment appIntroductionFragment = new AppIntroductionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SOURCE_FLAG,sourceFlag);
        appIntroductionFragment.setArguments(bundle);
        return appIntroductionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSourceFlag = getArguments().getString(SOURCE_FLAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_app_introduction, container, false);
        mAppIntroViewPager = (ViewPager) view.findViewById(R.id.intro_viewpager);
        mViewPagerIndicator1Image = (ImageView) view.findViewById(R.id.viewpager_indicator1_image);
        mViewPagerIndicator2Image = (ImageView) view.findViewById(R.id.viewpager_indicator2_image);
        mViewPagerIndicator3Image = (ImageView) view.findViewById(R.id.viewpager_indicator3_image);
        mViewPagerIndicator4Image = (ImageView) view.findViewById(R.id.viewpager_indicator4_image);
        mViewPagerForwardCloseImage = (ImageView) view.findViewById(R.id.viewpager_forward_close_image);
        mSkipText = (TextView) view.findViewById(R.id.txt_skip);
        mDoneText = (TextView) view.findViewById(R.id.txt_done);
        mAppIntroViewPager.setAdapter(new CustomPagerAdapter(getChildFragmentManager()));
        mAppIntroViewPager.setPageTransformer(true, new CrossfadePageTransformer());
        mPreferenceManager = new PreferencesManager(getContext());

        mAppIntroViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator4Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        break;
                    case 1:
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator4Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mSkipText.setVisibility(View.VISIBLE);
                        mViewPagerForwardCloseImage.setVisibility(View.VISIBLE);
                        mDoneText.setVisibility(View.GONE);
                        break;
                    case 2:
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        mViewPagerIndicator4Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mSkipText.setVisibility(View.INVISIBLE);
                        mDoneText.setVisibility(View.VISIBLE);
                        mViewPagerForwardCloseImage.setVisibility(View.GONE);
                        break;
                   /* case 3:
                        mViewPagerIndicator1Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator2Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator3Image.setImageResource(R.drawable.viewpager_indicator_unselected);
                        mViewPagerIndicator4Image.setImageResource(R.drawable.viewpager_indicator_selected);
                        mSkipText.setVisibility(View.GONE);
                        mDoneText.setVisibility(View.VISIBLE);
                        mViewPagerForwardCloseImage.setVisibility(View.GONE);
                        break;*/
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPagerForwardCloseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppIntroViewPager.setCurrentItem(mAppIntroViewPager.getCurrentItem() + 1, true);
            }
        });

        mSkipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSourceFlag.equalsIgnoreCase(getString(R.string.registration)))
                     showFitnessSourceSelectionDialog();
                else{
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.source_flag),mSourceFlag);
                    bundle.putString(getString(R.string.page_flag), AppIntroductionFragment.class.getSimpleName());
                    mFragmentInteraction.changeFragment(bundle);
                }
            }
        });

        mDoneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSourceFlag.equalsIgnoreCase(getString(R.string.registration)))
                    showFitnessSourceSelectionDialog();
                else{
                    Bundle bundle = new Bundle();
                    bundle.putString(getString(R.string.source_flag),mSourceFlag);
                    bundle.putString(getString(R.string.page_flag), AppIntroductionFragment.class.getSimpleName());
                    mFragmentInteraction.changeFragment(bundle);
                }

            }
        });

        return  view;
    }

    private void showFitnessSourceSelectionDialog() {
        final Dialog dialog = new Dialog(mContext,R.style.Theme_Dialog_Translucent);
        dialog.setContentView(R.layout.dialog_select_fitness_source);
        dialog.setCancelable(false);
        LinearLayout googleFit = (LinearLayout) dialog.findViewById(R.id.google_fit_container);
        LinearLayout fitbit = (LinearLayout) dialog.findViewById(R.id.fibit_container);

        googleFit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mPreferenceManager.setIntValue(getString(R.string.selected_fitness_source), FitnessSource.GOOGLE_FIT.getId());
                mPreferenceManager.setBooleanValue(getString(R.string.is_how_its_works_learned),true);
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.page_flag), AppIntroductionFragment.class.getSimpleName());
                bundle.putString(getString(R.string.source_flag),mSourceFlag);
                mFragmentInteraction.changeFragment(bundle);
                  }
        });

        fitbit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mPreferenceManager.setIntValue(getString(R.string.selected_fitness_source), FitnessSource.FITBIT.getId());
                mPreferenceManager.setBooleanValue(getString(R.string.is_how_its_works_learned),true);
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.source_flag),mSourceFlag);
                bundle.putString(getString(R.string.page_flag), AppIntroductionFragment.class.getSimpleName());
                mFragmentInteraction.changeFragment(bundle);
            }
        });
        dialog.show();
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

    private class CustomPagerAdapter extends FragmentStatePagerAdapter {

        public CustomPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new AppIntroduction1Fragment();
                case 1:
                    return new AppIntroduction2Fragment();
                case 2:
                    return new AppIntroduction3Fragment();
                /*case 3:
                    return new AppIntroduction4Fragment();*/

            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    /**
     * Class used to cross fade animation while swiping between fragments in viewpager
     */
    public class CrossfadePageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {
            int pageWidth = page.getWidth();

            View backgroundView = page.findViewById(R.id.app_tour_container);
            View text_head= page.findViewById(R.id.txt_app_tour_header);
            View text_content = page.findViewById(R.id.txt_app_tour_content);

            if(0 <= position && position < 1){
                ViewHelper.setTranslationX(page, pageWidth * -position);
            }
            if(-1 < position && position < 0){
                ViewHelper.setTranslationX(page,pageWidth * -position);
            }

            if(position <= -1.0f || position >= 1.0f) {
            } else if( position == 0.0f ) {
            } else {
                if(backgroundView != null) {
                    ViewHelper.setAlpha(backgroundView,1.0f - Math.abs(position));

                }

                if (text_head != null) {
                    ViewHelper.setTranslationX(text_head,pageWidth * position);
                    ViewHelper.setAlpha(text_head,1.0f - Math.abs(position));
                }

                if (text_content != null) {
                    ViewHelper.setTranslationX(text_content,pageWidth * position);
                    ViewHelper.setAlpha(text_content,1.0f - Math.abs(position));
                }
            }
        }
    }

}
