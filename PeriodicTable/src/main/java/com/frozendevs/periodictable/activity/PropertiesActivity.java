package com.frozendevs.periodictable.activity;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.PropertiesFragment;
import com.frozendevs.periodictable.fragment.IsotopesFragment;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.ElementProperties;

public class PropertiesActivity extends ActionBarActivity {

    public static final String EXTRA_ATOMIC_NUMBER = "com.frozendevs.periodictable.activity.AtomicNumber";

    public static final String ARGUMENT_PROPERTIES = "properties";

    private ElementProperties mElementProperties;

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;

            switch (i) {
                case 0:
                    fragment = new PropertiesFragment();
                    break;

                case 1:
                    fragment = new IsotopesFragment();
                    break;

                default:
                    return null;
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable(ARGUMENT_PROPERTIES, mElementProperties);
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.fragment_title_details);

                case 1:
                    return getString(R.string.fragment_title_isotopes);
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.properties_activity);

        mElementProperties = Database.getInstance(this).getElementProperties(
                getIntent().getIntExtra(EXTRA_ATOMIC_NUMBER, 1));

        getSupportActionBar().setTitle(mElementProperties.getName());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

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
                        Uri.parse(mElementProperties.getWikipediaLink())));
                return true;

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.properties_context_menu, menu);
        menu.setHeaderTitle(R.string.context_title_options);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        CharSequence propertyName, propertyValue;

        ContextMenu.ContextMenuInfo info = item.getMenuInfo();

        if(info instanceof AdapterView.AdapterContextMenuInfo) {
            propertyName = ((TextView)((AdapterView.AdapterContextMenuInfo)info).targetView.
                    findViewById(R.id.property_name)).getText();
            propertyValue = ((TextView)((AdapterView.AdapterContextMenuInfo)info).targetView.
                    findViewById(R.id.property_value)).getText();
        }
        else {
            View view = ((ExpandableListView.ExpandableListContextMenuInfo)info).targetView;

            if(view instanceof CheckedTextView) {
                propertyName = getString(R.string.property_symbol);
                propertyValue = ((CheckedTextView)view).getText();
            }
            else {
                propertyName = ((TextView)((ExpandableListView.ExpandableListContextMenuInfo)info).
                        targetView.findViewById(R.id.property_name)).getText();
                propertyValue = ((TextView)((ExpandableListView.ExpandableListContextMenuInfo)info).
                        targetView.findViewById(R.id.property_value)).getText();
            }
        }

        switch (item.getItemId()) {
            case R.id.context_copy:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    ((android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).
                            setText(propertyValue);
                }
                else {
                    ((ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE)).
                            setPrimaryClip(ClipData.newPlainText(propertyName, propertyValue));
                }
                return true;
        }

        return super.onContextItemSelected(item);
    }
}
