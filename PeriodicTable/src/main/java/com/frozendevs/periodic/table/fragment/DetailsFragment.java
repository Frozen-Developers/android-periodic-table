package com.frozendevs.periodic.table.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodic.table.R;

public class DetailsFragment extends Fragment {

    private int atomicNumber;

    public DetailsFragment(int atomicNumber) {
        super();

        this.atomicNumber = atomicNumber;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.details_fragment, container, false);

        return view;
    }
}
