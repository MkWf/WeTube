package com.gmail.markdevw.wetube.activities.main;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.pedrovgs.DraggablePanel;
import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.activities.main.fragments.FriendsFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.NotificationsFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.ProfileFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.SearchFragment;
import com.gmail.markdevw.wetube.data.models.Notification;
import com.gmail.markdevw.wetube.utils.Constants;
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
    private static final String VIDEO_KEY = "gCg33Qh_6IA";
    private YouTubePlayer youtubePlayer;
    private YouTubePlayerSupportFragment youtubeFragment;
    private Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL);

    @Bind(R.id.tb_activity_main)
    Toolbar toolbar;
    @Bind(R.id.vp_activity_main)
    ViewPager viewPager;
    @Bind(R.id.tl_activity_main)
    TabLayout tabLayout;
    @Bind(R.id.dp_activity_main)
    DraggablePanel draggablePanel;
    @Bind(R.id.fl_root)
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitle("Friends");
        setSupportActionBar(toolbar);

        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(FriendsFragment.newInstance(), "Friends");
        adapter.addFragment(SearchFragment.newInstance(), "Search");
        adapter.addFragment(NotificationsFragment.newInstance(), "Notifications");
        adapter.addFragment(ProfileFragment.newInstance(), "Profile");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.selector_friends_tab);
        tabLayout.getTabAt(1).setIcon(R.drawable.selector_search_tab);

        firebaseRef.child("notifications").child("markwassefdev@gmail,com").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot == null){
                    tabLayout.getTabAt(2).setIcon(R.drawable.selector_notifications_tab);
                }else{
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Notification post = postSnapshot.getValue(Notification.class);
                        if(!post.isRead()){
                            tabLayout.getTabAt(2).setIcon(R.drawable.selector_notifications_active_tab);
                            return;
                        }
                        tabLayout.getTabAt(2).setIcon(R.drawable.selector_notifications_tab);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        tabLayout.getTabAt(3).setIcon(R.drawable.selector_profile_tab);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        toolbar.setTitle("Friends");
                        break;
                    case 1:
                        toolbar.setTitle("Search");
                        break;
                    case 2:
                        toolbar.setTitle("Notifications");
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
                        toolbar.setTitle("Friends");
                        break;
                    case 1:
                        toolbar.setTitle("Friends");
                        break;
                    case 2:
                        toolbar.setTitle("Search");
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

        //FRIENDS
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user1@gmail,com").setValue(new User("User1", "user1@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user2@gmail,com").setValue(new User("User2", "user2@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user3@gmail,com").setValue(new User("User3", "user3@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user4@gmail,com").setValue(new User("User4", "user4@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user5@gmail,com").setValue(new User("User5", "user5@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user6@gmail,com").setValue(new User("User6", "user6@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user7@gmail,com").setValue(new User("User7", "user7@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user8@gmail,com").setValue(new User("User8", "user8@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user9@gmail,com").setValue(new User("User9", "user9@gmail.com", true, true));
//        firebaseRef.child("friends").child("markwassefdev@gmail,com").child("user10@gmail,com").setValue(new User("User10", "user10@gmail.com", true, true));






//
//        youtubeFragment = new YouTubePlayerSupportFragment();
//        youtubeFragment.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
//
//            @Override
//            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
//                if (!wasRestored) {
//                    youtubePlayer = player;
//                    youtubePlayer.loadVideo(VIDEO_KEY);
//                    youtubePlayer.setShowFullscreenButton(true);
//                }
//            }
//
//            @Override
//            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
//
//            }
//        });
//
//        draggablePanel.setFragmentManager(getSupportFragmentManager());
//        draggablePanel.setTopFragment(youtubeFragment);
//        ChatFragment moviePosterFragment = ChatFragment.newInstance();
//        draggablePanel.setBottomFragment(moviePosterFragment);
//        draggablePanel.setDraggableListener(new DraggableListener() {
//            @Override
//            public void onMaximized() {
//
//            }
//
//            @Override
//            public void onMinimized() {
//
//            }
//
//            @Override
//            public void onClosedToLeft() {
//
//            }
//
//            @Override
//            public void onClosedToRight() {
//
//            }
//        });
//        draggablePanel.initializeView();
    }

    private static void setLayoutSize(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            draggablePanel.getDraggableView().disableDraggableView();
//            if(draggablePanel.isMaximized()) {
//                Display
//                        display =
//                        MainActivity.this.getWindowManager()
//                                .getDefaultDisplay();
//
//                Point size = new Point();
//                display.getSize(size);
//                draggablePanel.getDraggableView().setTopViewHeight(size.y);
//            }else if(draggablePanel.isMinimized()){
//
//            }else if(!draggablePanel.isMinimized() && !draggablePanel.isMaximized()){
////                ViewHelper.setScaleX(draggablePanel.getDraggableView(), 4);
////                ViewHelper.setScaleY(draggablePanel.getDraggableView(), 4);
//                draggablePanel.getDraggableView().getTransformer().updateScale(0);
//                draggablePanel.getDraggableView().getTransformer().updatePosition(0);
//                Display
//                        display =
//                        MainActivity.this.getWindowManager()
//                                .getDefaultDisplay();
//
//                Point size = new Point();
//                display.getSize(size);
//                draggablePanel.getDraggableView().setTopViewHeight(size.y);
//            }
//        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            if(!draggablePanel.isMinimized()) {
//                draggablePanel.getDraggableView().enableDraggableView();
//
//                Resources r = getResources();
//                int px = (int) TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics()
//                );
//                draggablePanel.getDraggableView().setTopViewHeight(px);
//            }
//        }
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

//    @Override
//    public void onBackPressed() {
//        if(draggablePanel.getVisibility() == View.VISIBLE && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
//            draggablePanel.getDraggableView().enableDraggableView();
//            Toast.makeText(MainActivity.this, "portraite", Toast.LENGTH_SHORT)
//                    .show();
//
//            Resources r = getResources();
//            int px = (int) TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics()
//            );
//            draggablePanel.getDraggableView().setTopViewHeight(px);
//            draggablePanel.minimize();
//        }else{
//            super.onBackPressed();
//        }
//    }
}
