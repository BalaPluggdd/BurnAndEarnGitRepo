package com.pluggdd.burnandearn.view.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluggdd.burnandearn.R;


/**
 * Fragment to show app introduction1 screen
 */
public class AppIntroduction1Fragment extends Fragment {


    public AppIntroduction1Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_introduction1, container, false);
    }

}
