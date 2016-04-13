package com.gmail.markdevw.wetube.data.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.gmail.markdevw.wetube.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Mark on 4/13/2016.
 */

public class FriendViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.friend_name)
    public TextView name;

    public FriendViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
