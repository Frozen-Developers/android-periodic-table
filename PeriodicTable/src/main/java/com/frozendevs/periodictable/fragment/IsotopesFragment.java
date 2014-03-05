package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.adapter.IsotopesAdapter;

public class IsotopesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.isotopes_fragment, container, false);

        ExpandableListView listView = (ExpandableListView)layout.findViewById(R.id.isotopes_list);
        listView.setAdapter(new IsotopesAdapter(getActivity(),
                ((ElementProperties)getArguments().get(PropertiesActivity.ARGUMENT_PROPERTIES)).getIsotopes()));

        getActivity().registerForContextMenu(listView);

        return layout;
    }
}
