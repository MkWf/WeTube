package com.gmail.markdevw.wetube.data.adapters;


import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.gmail.markdevw.wetube.data.models.Friend;
import com.gmail.markdevw.wetube.data.viewholders.FriendViewHolder;


/**
 * Created by Mark on 4/13/2016.
 */
public class FriendAdapter extends FirebaseRecyclerAdapter<Friend, FriendViewHolder> {

    public FriendAdapter(Class<Friend> modelClass, int modelLayout, Class<FriendViewHolder> viewHolderClass, Firebase ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(FriendViewHolder friendViewHolder, Friend friend, int i) {
        friendViewHolder.name.setText(friend.getName());
    }
}
