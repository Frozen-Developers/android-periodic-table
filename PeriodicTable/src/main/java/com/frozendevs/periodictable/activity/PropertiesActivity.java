package com.frozendevs.periodictable.activity;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.IsotopesFragment;
import com.frozendevs.periodictable.fragment.PropertiesFragment;
import com.frozendevs.periodictable.fragment.TableFragment;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.adapter.PagesAdapter;

public class PropertiesActivity extends ActionBarActivity {

    public static final String EXTRA_ATOMIC_NUMBER = "com.frozendevs.periodictable.AtomicNumber";

    public static final String ARGUMENT_PROPERTIES = "properties";

    private String mWikipediaUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.properties_activity);

        TableFragment tableFragment = TableFragment.getInstance();

        if (tableFragment != null) {
            supportPostponeEnterTransition();

            setEnterSharedElementCallback(tableFragment.mSharedElementCallback);
        }

        ElementProperties elementProperties = Database.getInstance(this).getElementProperties(
                getIntent().getIntExtra(EXTRA_ATOMIC_NUMBER, 1));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(elementProperties.getName());

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_PROPERTIES, elementProperties);

        PagesAdapter pagesAdapter = new PagesAdapter(this);
        pagesAdapter.addPage(R.string.fragment_title_properties, PropertiesFragment.class, bundle);
        pagesAdapter.addPage(R.string.fragment_title_isotopes, IsotopesFragment.class, bundle);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagesAdapter);

        TypedArray typedArray = getTheme().obtainStyledAttributes(R.style.Theme_Application,
                new int[]{R.attr.colorAccent});

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        pagerTabStrip.setTabIndicatorColorResource(typedArray.getResourceId(0,
                R.color.accent_material_dark));

        typedArray.recycle();

        mWikipediaUrl = elementProperties.getWikipediaLink();
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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mWikipediaUrl)));
                return true;

            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.properties_context_menu, menu);
        menu.setHeaderTitle(R.string.context_title_options);

        super.onCreateContextMenu(menu, view, menuInfo);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String propertyName, propertyValue;

        ContextMenu.ContextMenuInfo info = item.getMenuInfo();

        View view;

        if (info instanceof ExpandableListView.ExpandableListContextMenuInfo) {
            view = ((ExpandableListView.ExpandableListContextMenuInfo) info).targetView;
        } else {
            view = ((ListView.AdapterContextMenuInfo) info).targetView;
        }

        TextView symbol = (TextView) view.findViewById(R.id.property_symbol);
        TextView configuration = (TextView) view.findViewById(R.id.element_electron_configuration);

        if (configuration != null) {
            propertyName = getString(R.string.properties_header_summary);
            propertyValue = (String) ((TextView) view.findViewById(R.id.element_symbol)).getText() +
                    '\n' + ((TextView) view.findViewById(R.id.element_number)).getText() + '\n' +
                    ((TextView) view.findViewById(R.id.element_name)).getText() + '\n' +
                    ((TextView) view.findViewById(R.id.element_weight)).getText() + '\n' +
                    configuration.getText() + '\n' +
                    ((TextView) view.findViewById(R.id.element_electrons_per_shell)).getText() + '\n'
                    + ((TextView) view.findViewById(R.id.element_electronegativity)).getText() + '\n'
                    + ((TextView) view.findViewById(R.id.element_oxidation_states)).getText();
        } else if (symbol != null) {
            propertyName = getString(R.string.property_symbol);
            propertyValue = (String) symbol.getText();
        } else {
            propertyName = (String) ((TextView) view.findViewById(R.id.property_name)).getText();
            propertyValue = (String) ((TextView) view.findViewById(R.id.property_value)).getText();
        }

        switch (item.getItemId()) {
            case R.id.context_copy:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    ((android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).
                            setText(propertyValue);
                } else {
                    ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).
                            setPrimaryClip(ClipData.newPlainText(propertyName, propertyValue));
                }
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        TableFragment tableFragment = TableFragment.getInstance();

        if (tableFragment != null) {
            tableFragment.onChildActivityDestroy();
        }
    }
}
