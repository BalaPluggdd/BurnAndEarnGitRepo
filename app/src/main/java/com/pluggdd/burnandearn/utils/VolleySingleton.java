package com.pluggdd.burnandearn.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


/**
 * Singleton class to manage network operatios using Volley library
 */
public class VolleySingleton {

    private static  VolleySingleton mVolleySingleTon;
    private RequestQueue mVolleyRequestQueque;
    private ImageLoader mVolleyImageLoader;

    private VolleySingleton(){
       mVolleyRequestQueque = Volley.newRequestQueue(BurnAndEarnApplication.getAppcontext());

       mVolleyImageLoader = new ImageLoader(mVolleyRequestQueque, new ImageLoader.ImageCache() {

            LruCache<String,Bitmap> cache = new LruCache<>((int)Runtime.getRuntime().maxMemory()/1024/8);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url,bitmap);
            }
        });
    }

    public static VolleySingleton getSingletonInstance(){
        if(mVolleySingleTon == null)
                mVolleySingleTon = new VolleySingleton();
        return  mVolleySingleTon;
    }

    public void setRequestPolicy(Request request){
        int socketTimeout = 30000;//30 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
    }

    public RequestQueue getRequestQueue(){
        return  mVolleyRequestQueque;
    }

    public ImageLoader getImageLoader(){
        return  mVolleyImageLoader;
    }
}
