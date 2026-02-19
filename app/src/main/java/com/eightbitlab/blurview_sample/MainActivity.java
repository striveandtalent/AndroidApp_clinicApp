package com.eightbitlab.blurview_sample;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private BlurTarget target;
    private TabLayout tabLayout;
    private BlurView bottomBlurView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setupBlurView();
        setupViewPager();
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(bottomBlurView, (v, windowInsets) -> {
            bottomBlurView.setPadding(0, 0, 0, 0);
            return windowInsets;
        });

    }

    private void initView() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        bottomBlurView = findViewById(R.id.bottomBlurView);
        target = findViewById(R.id.target);
    }

    private void setupViewPager() {
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupBlurView() {
        final float radius = 25f;
        final Drawable windowBackground = getWindow().getDecorView().getBackground();

        bottomBlurView.setupWith(target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return Page.values()[position].getFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Page.values()[position].getTitle();
        }

        @Override
        public int getCount() {
            return Page.values().length;
        }
    }

    enum Page {
        SECOND("病例") {
            @Override
            Fragment getFragment() {
                return new CaseFragment();
            }
        },
        THIRD("设置") {
            @Override
            Fragment getFragment() {
                return new SettingFragment();
            }
        };

        private String title;

        Page(String title) {
            this.title = title;
        }

        String getTitle() {
            return title;
        }

        abstract Fragment getFragment();
    }
}
