package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.adapter.PropropertiesAdapter;

public class PropertiesFragment extends Fragment {

    private int atomicNumber;

    public PropertiesFragment(int atomicNumber) {
        super();

        this.atomicNumber = atomicNumber;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.properties_fragment, container, false);

        ListView listView = (ListView)layout.findViewById(R.id.details_list);
        listView.setEmptyView(layout.findViewById(R.id.progress_bar));
        listView.setAdapter(new PropropertiesAdapter(getActivity(), atomicNumber));

        getActivity().registerForContextMenu(listView);

        return layout;
    }
}
