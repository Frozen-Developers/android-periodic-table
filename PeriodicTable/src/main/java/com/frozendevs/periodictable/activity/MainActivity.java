package com.frozendevs.periodictable.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.ElementsFragment;
import com.frozendevs.periodictable.fragment.TableFragment;
import com.frozendevs.periodictable.model.adapter.ElementsAdapter;
import com.frozendevs.periodictable.model.adapter.TabsAdapter;

public class MainActivity extends ActionBarActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        TabsAdapter tabsAdapter = new TabsAdapter(this, mViewPager);
        tabsAdapter.addTab(actionBar.newTab().setText(R.string.fragment_title_elements),
                ElementsFragment.class, null);
        tabsAdapter.addTab(actionBar.newTab().setText(R.string.fragment_title_isotopes),
                TableFragment.class, null);
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
                    mViewPager.setCurrentItem(0);

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
                    mViewPager.setCurrentItem(0);
                return true;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConf) {
        super.onConfigurationChanged(newConf);
    }
}
