package com.example.mychat.fragment.refresh;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.framework.base.BaseFragment;
import com.example.mychat.R;
import com.example.mychat.fragment.chat.AllFriendFragment;
import com.example.mychat.fragment.chat.ChatRecordFragment;
import com.google.android.material.tabs.TabLayout;
//import android.support.design.widget.TabLayout;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;

/**
 * Created by Oleksii Shliama.
 */
public class PullToRefreshActivity extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, null);
        initView(view);
        return view;
    }



    protected void initView(View view ) {
      //  super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_pull_to_refresh);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);

//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//        }

       // viewPager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ChatRecordFragment();
                case 1:
                default:
                    return new AllFriendFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "ListView";
                case 1:
                default:
                    return "RecyclerView";
            }
        }
    }

}
