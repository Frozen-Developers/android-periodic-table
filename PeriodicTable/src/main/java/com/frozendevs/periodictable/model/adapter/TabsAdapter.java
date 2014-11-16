package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;

public class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,
        ViewPager.OnPageChangeListener {

    private Context mContext;
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    static final class TabInfo {
        private final Class<?> cls;
        private final Bundle args;

        TabInfo(Class<?> _class, Bundle _args) {
            cls = _class;
            args = _args;
        }
    }

    public TabsAdapter(ActionBarActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());

        mContext = activity;
        mActionBar = activity.getSupportActionBar();
        mViewPager = pager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(ActionBar.Tab tab, Class<?> cls, Bundle args) {
        TabInfo info = new TabInfo(cls, args);
        tab.setTag(info);
        tab.setTabListener(this);

        mTabs.add(info);
        mActionBar.addTab(tab);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        TabInfo info = mTabs.get(position);

        return Fragment.instantiate(mContext, info.cls.getName(), info.args);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mActionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        for (int i = 0; i < mTabs.size(); i++) {
            if (mTabs.get(i) == tab.getTag()) {
                mViewPager.setCurrentItem(i);
            }
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
}
