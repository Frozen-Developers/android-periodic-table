package com.frozendevs.periodic.table.model.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.activity.DetailsActivity;
import com.frozendevs.periodic.table.activity.MainActivity;
import com.frozendevs.periodic.table.fragment.DetailsFragment;
import com.frozendevs.periodic.table.fragment.ElementListFragment;
import com.frozendevs.periodic.table.fragment.IsotopesFragment;
import com.frozendevs.periodic.table.fragment.TableFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Activity activity;

    public SectionsPagerAdapter(FragmentManager fm, Activity activity) {
        super(fm);

        this.activity = activity;
    }

    @Override
    public Fragment getItem(int i) {
        if(activity instanceof MainActivity) {
            switch (i) {
                case 0:
                    return new ElementListFragment();

                case 1:
                    return new TableFragment();
            }
        }
        else if(activity instanceof DetailsActivity) {
            switch (i) {
                case 0:
                    return new DetailsFragment();

                case 1:
                    return new IsotopesFragment();
            }
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(activity instanceof MainActivity) {
            switch (position) {
                case 0:
                    return activity.getString(R.string.elements_title);

                case 1:
                    return activity.getString(R.string.table_title);
            }
        }
        else if(activity instanceof DetailsActivity) {
            switch (position) {
                case 0:
                    return activity.getString(R.string.details_title).toUpperCase();

                case 1:
                    return activity.getString(R.string.isotopes_title).toUpperCase();
            }
        }

        return null;
    }
}