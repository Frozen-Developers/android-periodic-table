package com.frozendevs.periodic.table.model.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.helper.Database;
import com.frozendevs.periodic.table.model.ElementProperties;

public class DetailsListAdapter extends BaseAdapter {

    private Context context;

    private String[][] propertiesPairs;

    public DetailsListAdapter(Context context, int atomicNumber) {
        this.context = context;

        ElementProperties properties = Database.getElementDetails(context, atomicNumber);

        propertiesPairs = new String[][] {
                { context.getString(R.string.property_symbol), properties.getSymbol() },
                { context.getString(R.string.property_atomic_number),
                        String.valueOf(properties.getAtomicNumber()) },
                { context.getString(R.string.property_weight), properties.getStandardAtomicWeight() },
                { context.getString(R.string.property_group), String.valueOf(properties.getGroup()) },
                { context.getString(R.string.property_period), String.valueOf(properties.getPeriod()) },
                { context.getString(R.string.property_category), capitalize(properties.getCategory()) }
        };
    }

    @Override
    public int getCount() {
        return propertiesPairs.length;
    }

    @Override
    public String[] getItem(int position) {
        return propertiesPairs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.details_list_item, parent, false);

        String[] property = getItem(position);

        ((TextView)view.findViewById(R.id.property_name)).setText(property[0]);
        ((TextView)view.findViewById(R.id.property_value)).setText(property[1]);

        return view;
    }

    private String capitalize(String string) {
        if (string.length() == 0)
            return string;

        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }
}
