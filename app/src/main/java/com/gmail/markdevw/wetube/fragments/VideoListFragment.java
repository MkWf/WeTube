package com.gmail.markdevw.wetube.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.adapters.VideoItemAdapter;
import com.gmail.markdevw.wetube.api.DataSource;
import com.gmail.markdevw.wetube.api.model.video.VideoItem;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Mark on 3/27/2015.
 */
public class VideoListFragment extends Fragment implements VideoItemAdapter.Delegate {

    @Bind(R.id.rv_fragment_video_list) RecyclerView mRecyclerView;

    private VideoItemAdapter mVideoItemAdapter;
    private Delegate mListener;
    private LinearLayoutManager mLayoutManager;
    private int lastVisibleItem, totalItemCount;

    public static interface Delegate {
        public void onVideoItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mListener = (Delegate) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement VideoListDelegate interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);
        ButterKnife.bind(this, view);

        mVideoItemAdapter = new VideoItemAdapter();
        mVideoItemAdapter.setDelegate(this);

        mLayoutManager = new LinearLayoutManager(WeTubeApplication.getSharedInstance());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mVideoItemAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = mLayoutManager.getItemCount();
                lastVisibleItem = mLayoutManager.findLastCompletelyVisibleItemPosition() + 1;

                if (totalItemCount % 2 == 0 && totalItemCount == lastVisibleItem ) {
                    DataSource data = WeTubeApplication.getSharedDataSource();
                    data.getVideos().add(new VideoItem());
                    mVideoItemAdapter.notifyItemInserted(data.getVideos().size()-1);
                    mRecyclerView.scrollToPosition(data.getVideos().size()-1);
                    data.searchForVideos(data.getCurrentSearch(), data.getNextPageToken(), new DataSource.VideoResponseListener() {
                        @Override
                        public void onSuccess() {
                            mVideoItemAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(String search) {

                        }
                    });
                }
            }
        });

        return view;
    }

    public VideoItemAdapter getVideoItemAdapter() { return mVideoItemAdapter; }
    public RecyclerView getRecyclerView() { return mRecyclerView; }



    @Override
    public void onItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem) {
        mListener.onVideoItemClicked(itemAdapter, videoItem);
    }
}
