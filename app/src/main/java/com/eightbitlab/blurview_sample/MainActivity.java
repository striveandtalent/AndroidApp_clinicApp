package com.eightbitlab.blurview_sample;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

/**
 * 主界面职责：
 * 1) 承载 ViewPager，切换“病例 / 设置”两个页面；
 * 2) 只管理“底部导航栏”的 BlurView；
 * 3) 提供一个 Activity 级别的 BlurTarget（id=target），用于底部导航采样背景。
 *
 * 注意：顶部搜索框不在这里管理，而是在 CaseFragment 内独立管理。
 * 这样可以确保搜索框跟随病例页，而不是固定在 Activity 顶层。
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;

    /**
     * Activity 级模糊采样源。
     * bottomBlurView 会对这个 target 的内容做实时采样 + 高斯模糊。
     */
    private BlurTarget target;

    private TabLayout tabLayout;

    /** 底部导航的 BlurView（玻璃态效果）。 */
    private BlurView bottomBlurView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setupBlurView();
        setupViewPager();
        EdgeToEdge.enable(this);

        // 这里保留系统 inset 回调，后续如果要适配全面屏/手势条，可在此扩展。
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
        // 只有两个页面，预加载 2 个可以减少切换闪烁和重复初始化。
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * BlurView 初始化关键步骤：
     * 1) setupWith(target): 指定“采样谁”；
     * 2) setFrameClearDrawable(windowBackground): 指定清屏背景（某些机型/场景必须，防止残影）；
     * 3) setBlurRadius(radius): 设置模糊半径。
     */
    private void setupBlurView() {
        final float radius = 25f;

        // DecorView 可能没有显式背景，兜底透明色，避免空指针/崩溃。
        Drawable windowBackground = getWindow().getDecorView().getBackground();
        if (windowBackground == null) {
            windowBackground = new ColorDrawable(Color.TRANSPARENT);
        }

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
