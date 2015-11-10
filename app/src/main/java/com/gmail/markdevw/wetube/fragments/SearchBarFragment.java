package com.gmail.markdevw.wetube.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;

/**
 * Created by Mark on 3/27/2015.
 */
public class SearchBarFragment extends Fragment {

    private Button searchButton;
    private EditText searchBox;
    private ImageButton prevPage;
    private ImageButton nextPage;
    private Delegate listener;

    public static interface Delegate {
        public void onSearchButtonClicked(SearchBarFragment searchBarFragment, EditText search);
        public void onPrevPageButtonClicked(SearchBarFragment searchBarFragment, EditText search);
        public void onNextPageButtonClicked(SearchBarFragment searchBarFragment, EditText search);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            listener = (Delegate) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Delegate interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_search, container, false);
        searchButton = (Button) inflate.findViewById(R.id.fragment_search_search_button);
        searchBox = (EditText) inflate.findViewById(R.id.fragment_search_search_video);
        prevPage = (ImageButton) inflate.findViewById(R.id.fragment_search_prev_page);
        nextPage = (ImageButton) inflate.findViewById(R.id.fragment_search_next_page);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeTubeApplication.getSharedDataSource().setCurrentSearch(searchBox.getText().toString());
                listener.onSearchButtonClicked(SearchBarFragment.this, searchBox);
                if(!searchBox.getText().toString().isEmpty()){
                    prevPage.setVisibility(View.VISIBLE);
                    nextPage.setVisibility(View.VISIBLE);
                }
            }
        });

        prevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPrevPageButtonClicked(SearchBarFragment.this, searchBox);
            }
        });

        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNextPageButtonClicked(SearchBarFragment.this, searchBox);
            }
        });

        return inflate;
    }
}

