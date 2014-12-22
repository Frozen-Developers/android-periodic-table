package com.frozendevs.periodictable.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.view.PeriodicTableView;

public class TableFragment extends Fragment {

    private TableAdapter mAdapter;

    private class LoadData extends AsyncTask<Void, Void, Void> {

        ViewGroup mParent;

        private LoadData(ViewGroup parent) {
            mParent = parent;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAdapter.setItems(Database.getInstance(getActivity()).getTableItems());

            mAdapter.buildDrawingCache(mParent);

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.table_fragment, container, false);

        PeriodicTableView tableView = (PeriodicTableView) rootView.findViewById(
                R.id.elements_table);
        tableView.setEmptyView(rootView.findViewById(R.id.progress_bar));
        tableView.setAdapter(mAdapter);
        tableView.setOnItemClickListener(mAdapter);

        if (mAdapter.isEmpty()) {
            new LoadData(tableView).execute();
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAdapter.destroyDrawingCache();
    }
}
