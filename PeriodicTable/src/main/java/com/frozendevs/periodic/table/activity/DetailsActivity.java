package com.frozendevs.periodic.table.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.fragment.DetailsFragment;
import com.frozendevs.periodic.table.fragment.IsotopesFragment;
import com.frozendevs.periodic.table.helper.Database;
import com.frozendevs.periodic.table.model.BasicElementProperties;

public class DetailsActivity extends ActionBarActivity {

    private BasicElementProperties elementProperties;

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new DetailsFragment();

                case 1:
                    return new IsotopesFragment();
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
                    return getString(R.string.details_title).toUpperCase();

                case 1:
                    return getString(R.string.isotopes_title).toUpperCase();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.details_activity);

        elementProperties = Database.getBasicElementProperties(this, getIntent().getIntExtra("atomicNumber", 1));

        getSupportActionBar().setTitle(elementProperties.getName());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        pagerTabStrip.setTabIndicatorColorResource(R.color.holo_blue_light);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wiki:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(elementProperties.getWikiLink())));
                return true;

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
