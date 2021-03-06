package com.gmail.markdevw.wetube.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.adapters.ProfileCommonItemAdapter;
import com.gmail.markdevw.wetube.adapters.ProfileUncommonItemAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Mark on 4/17/2015.
 */
public class ProfileDialogFragment extends DialogFragment {

    @Bind(R.id.rv_fragment_profile_dialog_common) RecyclerView commonRecyclerView;
    @Bind(R.id.rv_fragment_profile_dialog_uncommon) RecyclerView uncommonRecyclerView;

    public ProfileDialogFragment() { /* Do Nothing */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(R.string.activities_usersactivity_view_profile_shared_tags);

        View view = inflater.inflate(R.layout.fragment_profile_dialog, container);
        ButterKnife.bind(this, view);

        ProfileCommonItemAdapter profileCommonItemAdapter = new ProfileCommonItemAdapter();
        ProfileUncommonItemAdapter profileUncommonItemAdapter = new ProfileUncommonItemAdapter();

        commonRecyclerView.setLayoutManager(new LinearLayoutManager(WeTubeApplication.getSharedInstance()));
        commonRecyclerView.setItemAnimator(new DefaultItemAnimator());
        commonRecyclerView.setAdapter(profileCommonItemAdapter);

        uncommonRecyclerView.setLayoutManager(new LinearLayoutManager(WeTubeApplication.getSharedInstance()));
        uncommonRecyclerView.setItemAnimator(new DefaultItemAnimator());
        uncommonRecyclerView.setAdapter(profileUncommonItemAdapter);

        return view;
    }
}
