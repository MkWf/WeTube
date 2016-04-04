package com.gmail.markdevw.wetube.activities.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.activities.main.fragments.ChatFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.ProfileFragment;
import com.gmail.markdevw.wetube.activities.main.fragments.SearchFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 4/4/2016.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);

        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(SearchFragment.newInstance(), "Search");
        adapter.addFragment(ChatFragment.newInstance(), "Chat");
        adapter.addFragment(ProfileFragment.newInstance(), "Profile");

        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
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


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}

