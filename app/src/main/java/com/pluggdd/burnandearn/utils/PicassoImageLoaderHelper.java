package com.pluggdd.burnandearn.utils;

import android.content.Context;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Helper class to load image from url using Picasso Library
 */
public class PicassoImageLoaderHelper {

    private Context mContext;
    private ImageView mImageView;
    private ProgressBar mLoadingProgressBar;
    private Picasso mPicasso;

    public PicassoImageLoaderHelper(Context context, ImageView imageView, ProgressBar progressBar){
        mContext = context;
        mImageView = imageView;
        mLoadingProgressBar = progressBar;
        mPicasso = Picasso.with(mContext);
    }


    public void loadImage(String url){
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        if(url != null && !url.equalsIgnoreCase("")){
            url = url.replaceAll(" ","%20");
            mPicasso
                    .load(url)
                    .noFade()
                    .into(mImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            mLoadingProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mLoadingProgressBar.setVisibility(View.GONE);
                        }
                    });

        }else{
            mLoadingProgressBar.setVisibility(View.GONE);
        }
    }


}
