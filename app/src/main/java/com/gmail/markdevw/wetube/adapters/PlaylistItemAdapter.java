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

        private ImageView mThumbnail;
        private ImageView mDelete;
        private TextView mTitle;
        private TextView mCount;
        private View mItemView;

        private PlaylistItem mPlaylistItem;
        private int mItemIndex;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            this.mItemView = itemView;

            mThumbnail = (ImageView) itemView.findViewById(R.id.playlist_thumbnail);
            mTitle = (TextView) itemView.findViewById(R.id.playlist_title);
            mDelete = (ImageView) itemView.findViewById(R.id.playlist_delete);
            mCount = (TextView) itemView.findViewById(R.id.playlist_index);

            mDelete.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        void update(PlaylistItem playlistItem, int index) {
            this.mPlaylistItem = playlistItem;
            this.mItemIndex = index;

            mTitle.setText(playlistItem.getTitle());
            mCount.setText(String.valueOf(playlistItem.getIndex()));
            Picasso.with(WeTubeApplication.getSharedInstance()).load(playlistItem.getThumbnailURL()).into(mThumbnail);

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