package com.frozendevs.periodictable.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.ElementsFragment;
import com.frozendevs.periodictable.fragment.TableFragment;
import com.frozendevs.periodictable.model.adapter.PagesAdapter;
import com.frozendevs.periodictable.view.ViewPagerTabs;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        PagesAdapter pagesAdapter = new PagesAdapter(this);
        pagesAdapter.addPage(R.string.fragment_title_elements, ElementsFragment.class, null);
        pagesAdapter.addPage(R.string.fragment_title_table, TableFragment.class, null);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagesAdapter);

        ViewPagerTabs viewPagerTabs = (ViewPagerTabs) findViewById(R.id.pager_header);
        viewPagerTabs.setViewPager(viewPager);
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
}
