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
import com.gmail.markdevw.wetube.data.models.Notification;
import com.gmail.markdevw.wetube.data.viewholders.NotificationViewHolder;
import com.gmail.markdevw.wetube.utils.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Mark on 4/4/2016.
 */
public class NotificationsFragment extends Fragment {

    public static NotificationsFragment newInstance() {

        Bundle args = new Bundle();

        NotificationsFragment fragment = new NotificationsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public NotificationsFragment() {

    }

    @Bind(R.id.rv_fragment_notifications)
    RecyclerView recyclerView;

    private FirebaseRecyclerAdapter<Notification, NotificationViewHolder> notificationAdapter;
    private Firebase firebaseNotificationsRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseNotificationsRef = new Firebase(Constants.FIREBASE_URL_NOTIFICATIONS).child("markwassefdev@gmail,com");
        notificationAdapter = new FirebaseRecyclerAdapter<Notification, NotificationViewHolder>(Notification.class, R.layout.notification_item, NotificationViewHolder.class, firebaseNotificationsRef) {
            @Override
            protected void populateViewHolder(NotificationViewHolder notificationViewHolder, Notification notification, int i) {
                notificationViewHolder.name.setText(notification.getName());
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(notificationAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
