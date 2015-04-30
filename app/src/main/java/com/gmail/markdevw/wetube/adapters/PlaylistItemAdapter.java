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
        public void onItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem);
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
        itemAdapterViewHolder.update(sharedDataSource.getPlaylist().get(index));
    }

    @Override
    public int getItemCount() {
        return WeTubeApplication.getSharedDataSource().getVideos().size();
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView thumbnail;
        TextView title;
        PlaylistItem playlistItem;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            thumbnail = (ImageView) itemView.findViewById(R.id.playlist_thumbnail);
            title = (TextView) itemView.findViewById(R.id.playlist_title);

            itemView.setOnClickListener(this);
        }

        void update(PlaylistItem playlistItem) {
            this.playlistItem = playlistItem;

            title.setText(playlistItem.getTitle());
            Picasso.with(WeTubeApplication.getSharedInstance()).load(playlistItem.getThumbnailURL()).into(thumbnail);
        }

        @Override
        public void onClick(View view) {
            getDelegate().onItemClicked(PlaylistItemAdapter.this, playlistItem);
        }
    }


}