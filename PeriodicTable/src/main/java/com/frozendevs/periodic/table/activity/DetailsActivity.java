package com.frozendevs.periodic.table.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.helper.Database;
import com.frozendevs.periodic.table.model.ElementDetails;
import com.frozendevs.periodic.table.model.adapter.SectionsPagerAdapter;

public class DetailsActivity extends ActionBarActivity {

    private ElementDetails elementDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.details_activity);

        elementDetails = Database.getElementDetails(this, getIntent().getIntExtra("atomicNumber", 1));

        getSupportActionBar().setTitle(elementDetails.getName());

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(elementDetails.getWikiLink())));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
