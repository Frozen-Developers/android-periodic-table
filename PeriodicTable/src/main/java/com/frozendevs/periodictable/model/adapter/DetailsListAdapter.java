package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.ElementProperties;

public class DetailsListAdapter extends BaseAdapter {

    private Context context;
    private String[][] propertiesPairs = new String[][] {};

    private class LoadProperties extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            ElementProperties properties = Database.getElementDetails(context, params[0]);

            propertiesPairs = new String[][] {
                    { getString(R.string.property_symbol), properties.getSymbol() },
                    { getString(R.string.property_atomic_number), intToStr(properties.getAtomicNumber()) },
                    { getString(R.string.property_weight), properties.getStandardAtomicWeight() },
                    { getString(R.string.property_group), intToStr(properties.getGroup()) },
                    { getString(R.string.property_period), intToStr(properties.getPeriod()) },
                    { getString(R.string.property_category), capitalize(properties.getCategory()) }
            };

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyDataSetChanged();
        }
    }

    public DetailsListAdapter(Context context, int atomicNumber) {
        this.context = context;

        new LoadProperties().execute(atomicNumber);
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

    private String getString(int resId) {
        return context.getString(resId);
    }

    private String intToStr(int integer) {
        return String.valueOf(integer);
    }
}
