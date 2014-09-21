package com.frozendevs.periodictable.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.TableItem;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.view.PeriodicTableView;

public class TableFragment extends Fragment {

    private class LoadData extends AsyncTask<Void, Void, TableItem[]> {

        private TableAdapter mAdapter;

        private LoadData(TableAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        protected TableItem[] doInBackground(Void... params) {
            return Database.getInstance(getActivity()).getTableItems();
        }

        @Override
        protected void onPostExecute(TableItem[] result) {
            mAdapter.setItems(result);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.table_fragment, container, false);

        PeriodicTableView tableView = (PeriodicTableView)rootView.findViewById(R.id.elements_table);
        tableView.setEmptyView(rootView.findViewById(R.id.progress_bar));

        TableAdapter adapter = new TableAdapter(getActivity());
        tableView.setAdapter(adapter);

        new LoadData(adapter).execute();

        return rootView;
    }
}
