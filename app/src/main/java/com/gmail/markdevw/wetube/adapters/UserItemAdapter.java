package com.gmail.markdevw.wetube.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.api.DataSource;
import com.gmail.markdevw.wetube.api.model.UserItem;

import java.lang.ref.WeakReference;

/**
 * Created by Mark on 4/3/2015.
 */
public class UserItemAdapter extends RecyclerView.Adapter<UserItemAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onItemClicked(UserItemAdapter itemAdapter, UserItem userItem, View view, int index);
    }

    private WeakReference<Delegate> delegate;

    public Delegate getDelegate() {
        if (delegate == null) {
            return null;
        }
        return delegate.get();
    }
    public void setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);
    }


    @Override
    public ItemAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int index) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        DataSource sharedDataSource = WeTubeApplication.getSharedDataSource();
        itemAdapterViewHolder.update(sharedDataSource.getUsers().get(index), index);
    }

    @Override
    public int getItemCount() {
        return WeTubeApplication.getSharedDataSource().getUsers().size();
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        ImageView status;
        ImageView friend;
        UserItem userItem;
        int index;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.user_item_name);
            status = (ImageView) itemView.findViewById(R.id.user_item_status);
            friend = (ImageView) itemView.findViewById(R.id.user_item_friend);

            itemView.setOnClickListener(this);
        }

        void update(UserItem userItem, int index) {
            this.userItem = userItem;
            this.index = index;
            name.setText(userItem.getName());

            if(!userItem.getOnlineStatus()) {
                status.setImageResource(R.drawable.offline);
            }else if(userItem.getSessionStatus()){
                status.setImageResource(R.drawable.unavailable);
            }else{
                status.setImageResource(R.drawable.available);
            }

           // if(userItem.getFriendStatus()){
          //      friend.setVisibility(View.VISIBLE);
           // }else{
           //     friend.setVisibility(View.GONE);
          //  }
        }

        @Override
        public void onClick(View view) {
            getDelegate().onItemClicked(UserItemAdapter.this, userItem, view, index);
        }
    }
}
