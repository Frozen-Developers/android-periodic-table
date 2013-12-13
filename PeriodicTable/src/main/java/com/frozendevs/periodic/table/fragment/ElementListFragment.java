package com.frozendevs.periodic.table.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.model.ElementListItem;
import com.frozendevs.periodic.table.model.adapter.ElementListAdapter;

public class ElementListFragment extends Fragment {

    private static ElementListAdapter adapter;

    public ElementListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.element_list_fragment, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.elementList);
        listView.setEmptyView(rootView.findViewById(R.id.loadingProgressBar));
        if(adapter == null)
            adapter = new ElementListAdapter(getActivity());
        listView.setAdapter(adapter);

        return rootView;
    }
}
