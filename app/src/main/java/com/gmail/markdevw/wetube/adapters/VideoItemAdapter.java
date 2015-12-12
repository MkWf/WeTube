package com.gmail.markdevw.wetube.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.api.DataSource;
import com.gmail.markdevw.wetube.api.model.video.VideoItem;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Mark on 3/26/2015.
 */
public class VideoItemAdapter extends RecyclerView.Adapter<VideoItemAdapter.ItemAdapterViewHolder> {

    public static interface Delegate {
        public void onItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem);
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
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_item, viewGroup, false);
        return new ItemAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ItemAdapterViewHolder itemAdapterViewHolder, int index) {
        DataSource sharedDataSource = WeTubeApplication.getSharedDataSource();
        itemAdapterViewHolder.update(sharedDataSource.getVideos().get(index));
    }

    @Override
    public int getItemCount() {
        return WeTubeApplication.getSharedDataSource().getVideos().size();
    }

    class ItemAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.video_thumbnail) ImageView mThumbnail;
        @Bind(R.id.video_title) TextView mTitle;
        @Bind(R.id.video_description) TextView mDescription;
        @Bind(R.id.video_duration) TextView mDuration;
        @Bind(R.id.video_load_progress) ProgressBar progress;

        private VideoItem mVideoItem;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        void update(VideoItem videoItem) {
            if(videoItem.getTitle() == null){
                progress.setVisibility(View.VISIBLE);
            }else{
                progress.setVisibility(View.GONE);
            }

            this.mVideoItem = videoItem;

            mTitle.setText(videoItem.getTitle());
            mDescription.setText(videoItem.getDescription());
            Glide.with(WeTubeApplication.getSharedInstance())
                    .load(videoItem.getThumbnailURL())
                    .into(mThumbnail);
            mDuration.setText(videoItem.getDuration());
        }

        @Override
        public void onClick(View view) {
            if(mVideoItem.getDuration() != null){
                getDelegate().onItemClicked(VideoItemAdapter.this, mVideoItem);
            }
        }
    }


}
