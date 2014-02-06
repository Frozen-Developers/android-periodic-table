package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.adapter.TableAdapter;
import com.frozendevs.periodictable.view.GridView;

public class TableFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.table_fragment, container, false);

        GridView table = (GridView)rootView.findViewById(R.id.elements_table);
        table.setEmptyView(rootView.findViewById(R.id.progress_bar));
        table.setAdapter(new TableAdapter(getActivity()));

        return rootView;
    }
}
