package com.frozendevs.periodic.table.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.model.adapter.TableAdapter;
import com.frozendevs.periodic.table.view.GridView;

public class TableFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.table_fragment, container, false);

        GridView table = (GridView)rootView.findViewById(R.id.table);
        table.setEmptyView(rootView.findViewById(R.id.generatingProgressBar));
        table.setAdapter(new TableAdapter(getActivity()));

        return rootView;
    }
}
