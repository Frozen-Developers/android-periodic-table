package com.frozendevs.periodictable.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.PropertiesFragment;
import com.frozendevs.periodictable.fragment.IsotopesFragment;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.BasicElementProperties;

public class PropertiesActivity extends ActionBarActivity {

    private BasicElementProperties elementProperties;

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private int atomicNumber;

        SectionsPagerAdapter(FragmentManager fm, int atomicNumber) {
            super(fm);

            this.atomicNumber = atomicNumber;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new PropertiesFragment(atomicNumber);

                case 1:
                    return new IsotopesFragment(atomicNumber);
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

        setContentView(R.layout.properties_activity);

        int atomicNumber = getIntent().getIntExtra("atomicNumber", 1);

        elementProperties = Database.getBasicElementProperties(this, atomicNumber);

        getSupportActionBar().setTitle(elementProperties.getName());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager(), atomicNumber));

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        pagerTabStrip.setTabIndicatorColorResource(R.color.holo_blue_light);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.properties_action_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_wiki:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(elementProperties.getWikiLink())));
                return true;

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConf) {
        super.onConfigurationChanged(newConf);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.properties_context_menu, menu);
        menu.setHeaderTitle(R.string.context_title_options);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        TextView propertyName = (TextView)info.targetView.findViewById(R.id.property_name);
        TextView propertyValue = (TextView)info.targetView.findViewById(R.id.property_value);

        switch (item.getItemId()) {
            case R.id.context_copy:
                ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(
                        ClipData.newPlainText(propertyName.getText(), propertyValue.getText())
                );
                return true;
        }

        return super.onContextItemSelected(item);
    }
}
