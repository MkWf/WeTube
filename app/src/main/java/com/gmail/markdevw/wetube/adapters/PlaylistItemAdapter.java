package com.gmail.markdevw.wetube.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.api.DataSource;
import com.gmail.markdevw.wetube.api.model.PlaylistItem;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;

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
        this.delegate = new WeakReference<>(delegate);
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

        @Bind(R.id.playlist_thumbnail) ImageView mThumbnail;
        @Bind(R.id.playlist_delete) ImageView mDelete;
        @Bind(R.id.playlist_title) TextView mTitle;
        @Bind(R.id.playlist_index) TextView mCount;
        @Bind(R.id.playlist_video_duration) TextView mDuration;

        private View mItemView;
        private PlaylistItem mPlaylistItem;
        private int mItemIndex;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            this.mItemView = itemView;

            mDelete.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        void update(PlaylistItem playlistItem, int index) {
            this.mPlaylistItem = playlistItem;
            this.mItemIndex = index;

            mTitle.setText(playlistItem.getTitle());
            mCount.setText(String.valueOf(playlistItem.getIndex()));
            Glide.with(WeTubeApplication.getSharedInstance())
                    .load(playlistItem.getThumbnailURL())
                    .into(mThumbnail);
            mDuration.setText(mPlaylistItem.getDuration());


            if(WeTubeApplication.getSharedDataSource().isSessionController() && !WeTubeApplication.getSharedDataSource().isPlayerVisible()){
                mDelete.setVisibility(View.VISIBLE);
            }else{
                mDelete.setVisibility(View.GONE);
            }

            if(playlistItem.isSelected()){
                mItemView.setBackgroundResource(R.color.black_26);
            }else{
                mItemView.setBackgroundResource(R.color.off_white);
            }
        }

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.playlist_delete:
                    if(!mPlaylistItem.isToBeDeleted()){
                        getDelegate().onDeleteItemClicked(PlaylistItemAdapter.this, mPlaylistItem);
                    }
                    break;
                default:
                    getDelegate().onPlayListItemClicked(PlaylistItemAdapter.this, mPlaylistItem, mItemIndex, mItemView);
            }

        }
    }


}