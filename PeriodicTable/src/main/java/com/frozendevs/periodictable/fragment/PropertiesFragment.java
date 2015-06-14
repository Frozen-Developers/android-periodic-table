package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.adapter.PropertiesAdapter;
import com.frozendevs.periodictable.view.RecyclerView;
import com.frozendevs.periodictable.widget.DividerDecoration;

public class PropertiesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.properties_fragment, container, false);

        final RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.properties_list);

        PropertiesAdapter adapter = new PropertiesAdapter(getActivity(),
                (ElementProperties) getArguments().get(PropertiesActivity.ARGUMENT_PROPERTIES));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerDecoration(getActivity()));
        recyclerView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (child.findViewById(R.id.tile_view) != null) {
                    getActivity().supportStartPostponedEnterTransition();

                    recyclerView.setOnHierarchyChangeListener(null);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });

        getActivity().registerForContextMenu(recyclerView);

        return layout;
    }
}
