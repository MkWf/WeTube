package com.gmail.markdevw.wetube.activities.main.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import butterknife.ButterKnife;

/**
 * Created by Mark on 4/5/2016.
 */
public class FriendsFragment extends Fragment{

    public static FriendsFragment newInstance() {

        Bundle args = new Bundle();

        FriendsFragment fragment = new FriendsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public FriendsFragment() {

    }

    //@Bind(R.id.rv_fragment_friends)
   // RecyclerView recyclerView;

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_friends, container, false);
//        ButterKnife.bind(this, view);
//        return view;
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
