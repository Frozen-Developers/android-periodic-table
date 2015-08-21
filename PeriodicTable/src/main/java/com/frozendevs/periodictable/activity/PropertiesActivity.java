package com.frozendevs.periodictable.activity;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.fragment.IsotopesFragment;
import com.frozendevs.periodictable.fragment.PropertiesFragment;
import com.frozendevs.periodictable.fragment.TableFragment;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.adapter.ViewPagerAdapter;
import com.frozendevs.periodictable.view.PeriodicTableView;
import com.frozendevs.periodictable.view.RecyclerView;

import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class PropertiesActivity extends AppCompatActivity implements
        View.OnAttachStateChangeListener {
    public static final String EXTRA_ATOMIC_NUMBER = "com.frozendevs.periodictable.AtomicNumber";

    public static final String ARGUMENT_PROPERTIES = "properties";

    private String mWikipediaUrl;

    private SharedElementCallback mSharedElementCallback = new SharedElementCallback() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements,
                                         List<View> sharedElementSnapshots) {
            super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);

            final PeriodicTableView periodicTableView = TableFragment.getPeriodicTableView();

            if (periodicTableView == null) {
                return;
            }

            final float zoom = periodicTableView.getZoom();

            View view = sharedElements.get(0);

            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

            view.measure(View.MeasureSpec.makeMeasureSpec(layoutParams.width,
                    View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                    layoutParams.height, View.MeasureSpec.EXACTLY));
            view.layout(view.getLeft(), view.getTop(), view.getLeft() + view.getMeasuredWidth(),
                    view.getTop() + view.getMeasuredHeight());
            view.setPivotX(0f);
            view.setPivotY(0f);
            view.setScaleX(zoom);
            view.setScaleY(zoom);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements,
                                       List<View> sharedElementSnapshots) {
            super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

            View view = sharedElements.get(0);
            view.setScaleX(1f);
            view.setScaleY(1f);
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.properties_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportPostponeEnterTransition();

            setEnterSharedElementCallback(mSharedElementCallback);

            /*
             * Work around shared view alpha state not being restored on exit transition finished.
             */
            getWindow().getDecorView().addOnAttachStateChangeListener(this);
        }

        ElementProperties elementProperties = Database.getInstance(this).getElementProperties(
                getIntent().getIntExtra(EXTRA_ATOMIC_NUMBER, 1));
        mWikipediaUrl = elementProperties.getWikipediaLink();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(elementProperties.getName());

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_PROPERTIES, elementProperties);

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);
        pagerAdapter.addPage(R.string.fragment_title_properties, PropertiesFragment.class, bundle);
        pagerAdapter.addPage(R.string.fragment_title_isotopes, IsotopesFragment.class, bundle);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
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

        View view = ((RecyclerView.RecyclerContextMenuInfo) item.getMenuInfo()).targetView;

        TextView symbol = (TextView) view.findViewById(R.id.property_symbol);

        if (symbol != null) {
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
    public void onViewAttachedToWindow(View view) {
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onViewDetachedFromWindow(View view) {
        if (isFinishing()) {
            return;
        }

        final PeriodicTableView periodicTableView = TableFragment.getPeriodicTableView();

        if (periodicTableView != null) {
            final View activeView = periodicTableView.getActiveView();

            if (activeView != null) {
                activeView.setAlpha(1f);
            }
        }
    }
}
