package com.frozendevs.periodictable.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.PeriodicTableApplication;
import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.content.AsyncTaskLoader;
import com.frozendevs.periodictable.content.Database;
import com.frozendevs.periodictable.model.TableElementItem;
import com.frozendevs.periodictable.model.TableItem;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.view.PeriodicTableView;

import java.util.List;

public class TableFragment extends Fragment implements PeriodicTableView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<List<TableElementItem>> {
    private static final String STATE_TABLE_ADAPTER = "tableAdapter";

    private TableAdapter mAdapter;
    private PeriodicTableView mPeriodicTableView;
    private LruCache<Integer, Bitmap> mBitmapCache = new LruCache<>(1);

    private class SharedElementCallback extends android.support.v4.app.SharedElementCallback {
        @Override
        public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements,
                                         List<View> sharedElementSnapshots) {
            super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);

            View view = sharedElements.get(0);

            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

            view.measure(View.MeasureSpec.makeMeasureSpec(layoutParams.width,
                    View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                    layoutParams.height, View.MeasureSpec.EXACTLY));
            view.layout(view.getLeft(), view.getTop(),
                    view.getLeft() + view.getMeasuredWidth(),
                    view.getTop() + view.getMeasuredHeight());
            view.setPivotX(0f);
            view.setPivotY(0f);
            view.setScaleX(mPeriodicTableView.getZoom());
            view.setScaleY(mPeriodicTableView.getZoom());
        }

        @Override
        public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements,
                                       List<View> sharedElementSnapshots) {
            super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);

            View view = sharedElements.get(0);
            view.setScaleX(1f);
            view.setScaleY(1f);
        }
    }

    private class OnAttachStateChangeListener implements View.OnAttachStateChangeListener {
        @Override
        public void onViewAttachedToWindow(View v) {
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            final View activeView = mPeriodicTableView.getActiveView();

            if (activeView != null) {
                activeView.setAlpha(1f);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (savedInstanceState != null) {
            mAdapter = savedInstanceState.getParcelable(STATE_TABLE_ADAPTER);
        } else {
            mAdapter = new TableAdapter();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PeriodicTableApplication application = (PeriodicTableApplication)
                    getActivity().getApplication();

            application.setSharedElementCallback(new SharedElementCallback());

            application.setOnAttachStateChangeListener(new OnAttachStateChangeListener());
        }

        if (mAdapter.isEmpty()) {
            getLoaderManager().initLoader(R.id.table_loader, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.table_fragment, container, false);

        mPeriodicTableView = (PeriodicTableView) rootView.findViewById(R.id.elements_table);
        mPeriodicTableView.setBitmapCache(mBitmapCache);
        mPeriodicTableView.setAdapter(mAdapter);
        mPeriodicTableView.setOnItemClickListener(this);
        mPeriodicTableView.setEmptyView(rootView.findViewById(R.id.progress_bar));

        return rootView;
    }

    @Override
    public void onItemClick(PeriodicTableView parent, View view, int position) {
        TableItem item = mAdapter.getItem(position);

        if (item instanceof TableElementItem) {
            parent.setEnabled(false);

            Intent intent = new Intent(getActivity(), PropertiesActivity.class);
            intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER,
                    ((TableElementItem) item).getNumber());

            ActivityCompat.startActivity(getActivity(), intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view,
                            getString(R.string.transition_table_item)).toBundle());
        }
    }

    @Override
    public void onResume() {
        if (mPeriodicTableView != null) {
            mPeriodicTableView.setEnabled(true);
        }

        super.onResume();
    }

    @Override
    public Loader<List<TableElementItem>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<TableElementItem>>(getActivity()) {
            @Override
            public List<TableElementItem> loadInBackground() {
                return Database.getAllElements(getContext(), TableElementItem.class);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<TableElementItem>> loader,
                               List<TableElementItem> data) {
        mAdapter.setItems(getActivity(), data);

        mBitmapCache.resize(mAdapter.getCount());

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<TableElementItem>> loader) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_TABLE_ADAPTER, mAdapter);
    }
}
