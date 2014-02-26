package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.view.PeriodicTableView;

public class TableFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.table_fragment, container, false);

        PeriodicTableView tableView = (PeriodicTableView)rootView.findViewById(R.id.elements_table);
        tableView.setEmptyView(rootView.findViewById(R.id.progress_bar));
        tableView.setAdapter(new TableAdapter(getActivity()));

        return rootView;
    }
}
