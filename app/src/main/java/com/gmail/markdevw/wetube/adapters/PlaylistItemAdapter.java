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
import com.gmail.markdevw.wetube.api.model.PlaylistItem;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

/**
 * Created by Mark on 4/30/2015.
 */
public class PlaylistItemAdapter extends RecyclerView.Adapter<PlaylistItemAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onPlayListItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem, int itemIndex, View itemView);
        public void onDeleteItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem);
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
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        DataSource sharedDataSource = WeTubeApplication.getSharedDataSource();
        itemAdapterViewHolder.update(sharedDataSource.getPlaylist().get(index), index);
    }

    @Override
    public int getItemCount() {
        return WeTubeApplication.getSharedDataSource().getPlaylist().size();
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView thumbnail;
        ImageView delete;
        TextView title;
        PlaylistItem playlistItem;
        View itemView;

        int itemIndex;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;

            thumbnail = (ImageView) itemView.findViewById(R.id.playlist_thumbnail);
            title = (TextView) itemView.findViewById(R.id.playlist_title);
            delete = (ImageView) itemView.findViewById(R.id.playlist_delete);

            delete.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        void update(PlaylistItem playlistItem, int index) {
            this.playlistItem = playlistItem;
            this.itemIndex = index;

            title.setText(playlistItem.getTitle());
            Picasso.with(WeTubeApplication.getSharedInstance()).load(playlistItem.getThumbnailURL()).into(thumbnail);

            if(WeTubeApplication.getSharedDataSource().isSessionController()){
                delete.setVisibility(View.VISIBLE);
            }else{
                delete.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.playlist_delete:
                    getDelegate().onDeleteItemClicked(PlaylistItemAdapter.this, playlistItem);
                    break;
                default:
                    getDelegate().onPlayListItemClicked(PlaylistItemAdapter.this, playlistItem, itemIndex, itemView);
            }

        }
    }


}