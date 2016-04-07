package com.gmail.markdevw.wetube.activities.main;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.widget.Toast;

import com.github.pedrovgs.DraggablePanel;
import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.activities.main.fragments.ChatFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.FriendsFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.HomeFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.ProfileFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.SearchFragment;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Mark on 4/7/2016.
 */
public class MainActivity extends AppCompatActivity {

    private static final String YOUTUBE_API_KEY = "AIzaSyC1rMU-mkhoyTvBIdTnYU0dss0tU9vtK48";
    private static final String VIDEO_KEY = "gsjtg7m1MMM";
    private YouTubePlayer youtubePlayer;
    private YouTubePlayerSupportFragment youtubeFragment;

    @Bind(R.id.tb_activity_main)
    Toolbar toolbar;
    @Bind(R.id.vp_activity_main)
    ViewPager viewPager;
    @Bind(R.id.tl_activity_main)
    TabLayout tabLayout;
    @Bind(R.id.dp_activity_main)
    DraggablePanel draggablePanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(HomeFragment.newInstance(), "Home");
        adapter.addFragment(SearchFragment.newInstance(), "Search");
        adapter.addFragment(FriendsFragment.newInstance(), "Friends");
        adapter.addFragment(ProfileFragment.newInstance(), "Profile");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.selector_home_tab);
        tabLayout.getTabAt(1).setIcon(R.drawable.selector_search_tab);
        tabLayout.getTabAt(2).setIcon(R.drawable.selector_friends_tab);
        tabLayout.getTabAt(3).setIcon(R.drawable.selector_profile_tab);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        toolbar.setTitle("Home");
                        break;
                    case 1:
                        toolbar.setTitle("Search");
                        break;
                    case 2:
                        toolbar.setTitle("Friends");
                        break;
                    case 3:
                        toolbar.setTitle("Profile");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        toolbar.setTitle("Home");
                        break;
                    case 1:
                        toolbar.setTitle("Search");
                        break;
                    case 2:
                        toolbar.setTitle("Friends");
                        break;
                    case 3:
                        toolbar.setTitle("Profile");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        youtubeFragment = new YouTubePlayerSupportFragment();
        youtubeFragment.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                if (!wasRestored) {
                    youtubePlayer = player;
                    youtubePlayer.loadVideo(VIDEO_KEY);
                    youtubePlayer.setShowFullscreenButton(true);
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {

            }
        });

        draggablePanel.setFragmentManager(getSupportFragmentManager());
        draggablePanel.setTopFragment(youtubeFragment);
        ChatFragment moviePosterFragment = ChatFragment.newInstance();
        draggablePanel.setBottomFragment(moviePosterFragment);
        draggablePanel.initializeView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(MainActivity.this, "landscape", Toast.LENGTH_SHORT)
                    .show();
            Display
                    display =
                    MainActivity.this.getWindowManager()
                            .getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);
            draggablePanel.getDraggableView().setTopViewHeight(size.y);
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(MainActivity.this, "portraite", Toast.LENGTH_SHORT)
                    .show();

            Resources r = getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics()
            );
            draggablePanel.getDraggableView().setTopViewHeight(px);
        }
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }
}
