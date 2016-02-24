package com.pluggdd.burnandearn.view.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.FragmentInteraction;
import com.pluggdd.burnandearn.view.adapter.OfferRewardsAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link interface
 * to handle interaction events.
 */
public class OffersAndRewardsFragment extends Fragment {

    private FragmentInteraction mListener;
    private RecyclerView mOfferAndRewardsRecyclerView;

    public OffersAndRewardsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_offers_and_rewards, container, false);
        mOfferAndRewardsRecyclerView = (RecyclerView) view.findViewById(R.id.offers_and_rewards_recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mOfferAndRewardsRecyclerView.setLayoutManager(layoutManager);
        mOfferAndRewardsRecyclerView.setAdapter(new OfferRewardsAdapter(getActivity(),mListener,this));
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteraction) {
            mListener = (FragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
