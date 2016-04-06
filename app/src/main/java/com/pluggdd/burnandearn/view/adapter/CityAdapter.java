package com.pluggdd.burnandearn.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pluggdd.burnandearn.model.City;

import java.util.ArrayList;

/**
 * Created by User on 06-Apr-16.
 */
public class CityAdapter  extends BaseAdapter{

    private Context mContext;
    private ArrayList<City> mCityList;

    public CityAdapter(Context context,ArrayList<City> cityList){
        mContext = context;
        mCityList = cityList;
    }

    @Override
    public int getCount() {
        return mCityList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        TextView cityTxt = (TextView) view.findViewById(android.R.id.text1);
        cityTxt.setTextColor(Color.BLACK);
        City city = mCityList.get(position);
        cityTxt.setText(city.getName());
        return view;
    }


}
