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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.adapters.VideoItemAdapter;
import com.gmail.markdevw.wetube.api.model.VideoItem;

/**
 * Created by Mark on 3/27/2015.
 */
public class VideoListFragment extends Fragment implements VideoItemAdapter.Delegate {

    private RecyclerView mRecyclerView;
    private VideoItemAdapter mVideoItemAdapter;
    private Button mSearchButton;
    private EditText mSearchBox;
    private ImageButton mPrevPage;
    private ImageButton mNextPage;
    private Delegate mListener;

    public static interface Delegate {
        public void onVideoItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem);
        public void onSearchButtonClicked(VideoListFragment videoListFragment, EditText search);
        public void onPrevPageButtonClicked(VideoListFragment videoListFragment, EditText search);
        public void onNextPageButtonClicked(VideoListFragment videoListFragment, EditText search);
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
        View inflate = inflater.inflate(R.layout.fragment_video_list, container, false);

        mSearchButton = (Button) inflate.findViewById(R.id.fragment_search_search_button);
        mSearchBox = (EditText) inflate.findViewById(R.id.fragment_search_search_video);
        mPrevPage = (ImageButton) inflate.findViewById(R.id.fragment_search_prev_page);
        mNextPage = (ImageButton) inflate.findViewById(R.id.fragment_search_next_page);

        mVideoItemAdapter = new VideoItemAdapter();
        mVideoItemAdapter.setDelegate(this);

        mRecyclerView = (RecyclerView) inflate.findViewById(R.id.rv_fragment_video_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(WeTubeApplication.getSharedInstance()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mVideoItemAdapter);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeTubeApplication.getSharedDataSource().setCurrentSearch(mSearchBox.getText().toString());
                mListener.onSearchButtonClicked(VideoListFragment.this, mSearchBox);
                if(!mSearchBox.getText().toString().isEmpty()){
                    mPrevPage.setVisibility(View.VISIBLE);
                    mNextPage.setVisibility(View.VISIBLE);
                }
            }
        });

        mPrevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPrevPageButtonClicked(VideoListFragment.this, mSearchBox);
            }
        });
        mNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onNextPageButtonClicked(VideoListFragment.this, mSearchBox);
            }
        });

        return inflate;
    }

    public VideoItemAdapter getVideoItemAdapter() { return mVideoItemAdapter; }
    public RecyclerView getRecyclerView() { return mRecyclerView; }

    @Override
    public void onItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem) {
        mListener.onVideoItemClicked(itemAdapter, videoItem);
    }
}
