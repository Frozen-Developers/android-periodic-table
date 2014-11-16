package com.frozendevs.periodictable.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.ElementsFragment;
import com.frozendevs.periodictable.fragment.TableFragment;
import com.frozendevs.periodictable.model.adapter.TabsAdapter;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        TabsAdapter tabsAdapter = new TabsAdapter(this, viewPager);
        tabsAdapter.addTab(actionBar.newTab().setText(R.string.fragment_title_elements),
                ElementsFragment.class, null);
        tabsAdapter.addTab(actionBar.newTab().setText(R.string.fragment_title_isotopes),
                TableFragment.class, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
