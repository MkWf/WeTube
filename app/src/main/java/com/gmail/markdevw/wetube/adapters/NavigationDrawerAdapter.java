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
 * Created by Mark on 4/12/2015.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onNavItemClicked(NavigationDrawerAdapter itemAdapter, UserItem userItem, View view, int index);
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
        itemAdapterViewHolder.update(sharedDataSource.getFriends().get(index), index);
    }

    @Override
    public int getItemCount() {
        return WeTubeApplication.getSharedDataSource().getFriends().size();
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        ImageView status;
        UserItem userItem;
        int index;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.user_item_name);
            status = (ImageView) itemView.findViewById(R.id.user_item_status);

            itemView.setOnClickListener(this);
        }

        void update(UserItem userItem, int index) {
            this.userItem = userItem;
            this.index = index;
            name.setText(userItem.getName());

            if(!userItem.getOnlineStatus()){
                status.setImageResource(R.drawable.offline);
            }else if(userItem.getSessionStatus()){
                status.setImageResource(R.drawable.unavailable);
            }else{
                status.setImageResource(R.drawable.available);
            }
        }

        @Override
        public void onClick(View view) {
            getDelegate().onNavItemClicked(NavigationDrawerAdapter.this, userItem, view, index);
        }
    }
}
