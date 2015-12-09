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
import com.gmail.markdevw.wetube.api.model.video.VideoItem;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mark on 3/27/2015.
 */
public class VideoListFragment extends Fragment implements VideoItemAdapter.Delegate {

    @Bind(R.id.fragment_search_search_button) Button mSearchButton;
    @Bind(R.id.fragment_search_search_video) EditText mSearchBox;
    @Bind(R.id.fragment_search_prev_page) ImageButton mPrevPage;
    @Bind(R.id.fragment_search_next_page) ImageButton mNextPage;
    @Bind(R.id.rv_fragment_video_list) RecyclerView mRecyclerView;

    private VideoItemAdapter mVideoItemAdapter;
    private Delegate mListener;

    public static interface Delegate {
        public void onVideoItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem);
        public void onSearchButtonClicked(VideoListFragment videoListFragment, EditText search);
        public void onPrevPageButtonClicked(VideoListFragment videoListFragment, EditText search, ImageButton prevPage);
        public void onNextPageButtonClicked(VideoListFragment videoListFragment, EditText search, ImageButton nextPage);
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

        mRecyclerView.setLayoutManager(new LinearLayoutManager(WeTubeApplication.getSharedInstance()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mVideoItemAdapter);

        return view;
    }

    public VideoItemAdapter getVideoItemAdapter() { return mVideoItemAdapter; }
    public RecyclerView getRecyclerView() { return mRecyclerView; }

    @OnClick({
            R.id.fragment_search_next_page,
            R.id.fragment_search_prev_page,
            R.id.fragment_search_search_button})
    public void searchBarNavigation(View view){
        switch(view.getId()){
            case R.id.fragment_search_next_page:
                mListener.onNextPageButtonClicked(VideoListFragment.this, mSearchBox, mNextPage);
                break;
            case R.id.fragment_search_prev_page:
                mListener.onPrevPageButtonClicked(VideoListFragment.this, mSearchBox, mPrevPage);
                break;
            case R.id.fragment_search_search_button:
                WeTubeApplication.getSharedDataSource().setCurrentSearch(mSearchBox.getText().toString());
                mListener.onSearchButtonClicked(VideoListFragment.this, mSearchBox);
                break;
        }
    }

    @Override
    public void onItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem) {
        mListener.onVideoItemClicked(itemAdapter, videoItem);
    }

    public void firstPage(){
        mPrevPage.setVisibility(View.VISIBLE);
        mPrevPage.setEnabled(false);
        mPrevPage.setAlpha(0.5f);

        mNextPage.setVisibility(View.VISIBLE);
    }

    public void lastPage(){
        mNextPage.setVisibility(View.VISIBLE);
        mNextPage.setEnabled(false);
        mNextPage.setAlpha(0.5f);
    }

    public void defaultPage() {
        mPrevPage.setEnabled(true);
        mPrevPage.setAlpha(1f);

        mNextPage.setEnabled(true);
        mNextPage.setAlpha(1f);
    }
}
