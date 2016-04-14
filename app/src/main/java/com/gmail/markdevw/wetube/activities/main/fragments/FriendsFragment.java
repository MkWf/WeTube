package com.gmail.markdevw.wetube.activities.main.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.gmail.markdevw.wetube.DividerItemDecoration;
import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.data.models.User;
import com.gmail.markdevw.wetube.data.viewholders.FriendViewHolder;
import com.gmail.markdevw.wetube.utils.Constants;

import butterknife.Bind;
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

    @Bind(R.id.rv_fragment_friends)
    RecyclerView recyclerView;

    private FirebaseRecyclerAdapter<User, FriendViewHolder> friendAdapter;
    private Firebase firebaseFriendsRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseFriendsRef = new Firebase(Constants.FIREBASE_URL_FRIENDS).child("markwassefdev@gmail,com");
        friendAdapter = new FirebaseRecyclerAdapter<User, FriendViewHolder>(User.class, R.layout.friend_item, FriendViewHolder.class, firebaseFriendsRef) {
            @Override
            protected void populateViewHolder(FriendViewHolder friendViewHolder, User friend, int i) {
                friendViewHolder.name.setText(friend.getName());
                friendViewHolder.logged.setText(Boolean.toString(friend.isLoggedIn()));
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(friendAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
