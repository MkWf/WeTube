package com.gmail.markdevw.wetube.activities.main.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import butterknife.ButterKnife;

/**
 * Created by Mark on 4/4/2016.
 */
public class ProfileFragment extends Fragment {
    public static ProfileFragment newInstance() {

        Bundle args = new Bundle();

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {

    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_profile, container, false);
//        ButterKnife.bind(this, view);
//
//
//        return view;
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
