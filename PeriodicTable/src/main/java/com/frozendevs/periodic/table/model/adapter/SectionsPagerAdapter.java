package com.frozendevs.periodic.table.model.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.fragment.ElementListFragment;
import com.frozendevs.periodic.table.fragment.TableFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Activity activity;

    public SectionsPagerAdapter(FragmentManager fm, Activity activity) {
        super(fm);

        this.activity = activity;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new ElementListFragment();

            case 1:
                return new TableFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return activity.getString(R.string.elements_title);

            case 1:
                return activity.getString(R.string.table_title);
        }

        return null;
    }
}