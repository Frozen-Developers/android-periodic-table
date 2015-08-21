package com.frozendevs.periodictable.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.TableItem;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.view.PeriodicTableView;

public class TableFragment extends Fragment implements PeriodicTableView.OnItemClickListener {

    private TableAdapter mAdapter;
    public static PeriodicTableView mPeriodicTableView;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

    @Override
    public void onResume() {
        if (mPeriodicTableView != null) {
            mPeriodicTableView.setEnabled(true);
        }

        super.onResume();
    }

    public static PeriodicTableView getPeriodicTableView() {
        return mPeriodicTableView;
    }
}
