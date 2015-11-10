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
import com.gmail.markdevw.wetube.api.model.VideoItem;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

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
        this.delegate = new WeakReference<Delegate>(delegate);
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

        private ImageView mThumbnail;
        private TextView mTitle;
        private TextView mDescription;
        private VideoItem mVideoItem;

        public ItemAdapterViewHolder(View itemView) {
            super(itemView);

            mThumbnail = (ImageView) itemView.findViewById(R.id.video_thumbnail);
            mTitle = (TextView) itemView.findViewById(R.id.video_title);
            mDescription = (TextView) itemView.findViewById(R.id.video_description);

            itemView.setOnClickListener(this);
        }

        void update(VideoItem videoItem) {
            this.mVideoItem = videoItem;

            mTitle.setText(videoItem.getTitle());
            mDescription.setText(videoItem.getDescription());
            Picasso.with(WeTubeApplication.getSharedInstance()).load(videoItem.getThumbnailURL()).into(mThumbnail);
        }

        @Override
        public void onClick(View view) {
            getDelegate().onItemClicked(VideoItemAdapter.this, mVideoItem);
        }
    }


}
