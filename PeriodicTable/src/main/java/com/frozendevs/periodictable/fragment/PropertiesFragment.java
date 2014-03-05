package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.adapter.PropertiesAdapter;

public class PropertiesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.properties_fragment, container, false);

        ListView listView = (ListView)layout.findViewById(R.id.properties_list);
        listView.setAdapter(new PropertiesAdapter(getActivity(),
                (ElementProperties)getArguments().get(PropertiesActivity.ARGUMENT_PROPERTIES)));

        getActivity().registerForContextMenu(listView);

        return layout;
    }
}
