package com.pluggdd.burnandearn.activity;

import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.HeaderView;



public class OfferDetailActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener{


    private Toolbar mToolBar;
    AppBarLayout mAppBarLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    HeaderView toolbarHeaderView;
    HeaderView floatHeaderView;
    TextView mHeaderTitle,mHeaderSubTitle;
    boolean isHideToolbarView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_container);
        toolbarHeaderView = (HeaderView) findViewById(R.id.toolbar_header_view);
        floatHeaderView = (HeaderView) findViewById(R.id.float_header_view);
        mHeaderTitle = (TextView) toolbarHeaderView.findViewById(R.id.header_view_title);
        mHeaderSubTitle = (TextView) toolbarHeaderView.findViewById(R.id.header_view_sub_title);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitle(" ");
        toolbarHeaderView.bindTo("Amazon", "10% off across site");
        floatHeaderView.bindTo("Amazon", "10% off across site");
        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        if (percentage == 1f && isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.VISIBLE);
            mHeaderTitle.setTextColor(Color.WHITE);
            mHeaderSubTitle.setTextColor(Color.WHITE);
            isHideToolbarView = !isHideToolbarView;

        } else if (percentage < 1f && !isHideToolbarView) {
            toolbarHeaderView.setVisibility(View.GONE);
            isHideToolbarView = !isHideToolbarView;
        }
    }
}
