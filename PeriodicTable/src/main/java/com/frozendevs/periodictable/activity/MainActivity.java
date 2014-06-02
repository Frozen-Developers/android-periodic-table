package com.frozendevs.periodictable.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.ElementsFragment;
import com.frozendevs.periodictable.fragment.TableFragment;
import com.frozendevs.periodictable.model.adapter.ElementsAdapter;
import com.frozendevs.periodictable.view.NonSwipeableViewPager;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private NonSwipeableViewPager mViewPager;

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] mFragments = new Fragment[] {
                new ElementsFragment(),
                new TableFragment()
        };

        private int[] mTitles = new int[] {
            R.string.fragment_title_elements,
            R.string.fragment_title_table
        };

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragments[i];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(mTitles[position]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (NonSwipeableViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(pagerAdapter);

        mViewPager.setOnPageChangeListener(new NonSwipeableViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(pagerAdapter.getPageTitle(i)).
                    setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_action_menu, menu);

        final ElementsAdapter adapter =
                (ElementsAdapter)((ListView) findViewById(R.id.elements_list)).getAdapter();

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_query_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mViewPager.getCurrentItem() > 0)
                    mViewPager.setCurrentItem(0, false);

                adapter.filter(newText.toLowerCase(getResources().getConfiguration().locale));
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                adapter.clearFilter();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if(mViewPager.getCurrentItem() > 0)
                    mViewPager.setCurrentItem(0, false);
                return true;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConf) {
        super.onConfigurationChanged(newConf);
    }
}
