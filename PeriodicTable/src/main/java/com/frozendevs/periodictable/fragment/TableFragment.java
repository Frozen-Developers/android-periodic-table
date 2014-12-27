package com.frozendevs.periodictable.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.TableItem;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.view.PeriodicTableView;

import java.util.List;

public class TableFragment extends Fragment implements PeriodicTableView.OnItemClickListener {

    private TableAdapter mAdapter;
    private PeriodicTableView mPeriodicTableView;
    private static TableFragment mInstance;

    private class LoadData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mAdapter.destroyDrawingCache();

            mAdapter.setItems(Database.getInstance(getActivity()).getTableItems());

            mAdapter.buildDrawingCache(mPeriodicTableView);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public SharedElementCallback mSharedElementCallback = new SharedElementCallback() {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements,
                                         List<View> sharedElementSnapshots) {
            super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);

            int tileSize = getResources().getDimensionPixelSize(R.dimen.table_item_size);

            View view = sharedElements.get(0);
            view.measure(View.MeasureSpec.makeMeasureSpec(tileSize, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(tileSize, View.MeasureSpec.EXACTLY));
            view.layout(view.getLeft(), view.getTop(), view.getLeft() + view.getMeasuredWidth(),
                    view.getTop() + view.getMeasuredHeight());
            view.setPivotX(0f);
            view.setPivotY(0f);
            view.setScaleX(mPeriodicTableView.getZoom());
            view.setScaleY(mPeriodicTableView.getZoom());
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

    public static synchronized TableFragment getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mInstance = this;

        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mAdapter = new TableAdapter(getActivity());

        new LoadData().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.table_fragment, container, false);

        mPeriodicTableView = (PeriodicTableView) rootView.findViewById(R.id.elements_table);
        mPeriodicTableView.setAdapter(mAdapter);
        mPeriodicTableView.setOnItemClickListener(this);
        mPeriodicTableView.setEmptyView(rootView.findViewById(R.id.progress_bar));

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAdapter.destroyDrawingCache();
    }

    @Override
    public void onItemClick(PeriodicTableView parent, View view, int position) {
        TableItem item = mAdapter.getItem(position);

        if (item != null) {
            parent.setEnabled(false);

            Intent intent = new Intent(getActivity(), PropertiesActivity.class);
            intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER, item.getNumber());

            ActivityCompat.startActivity(getActivity(), intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view,
                            getString(R.string.transition_table_item)).toBundle());
        }
    }

    public void onChildActivityDestroy() {
        mPeriodicTableView.removeAllViews();
        mPeriodicTableView.setEnabled(true);
    }
}
