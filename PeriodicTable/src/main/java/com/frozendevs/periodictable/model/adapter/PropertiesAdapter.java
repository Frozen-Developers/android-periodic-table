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

public class PropertiesAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private Context context;
    private String[][] propertiesPairs = new String[][] {  };

    private class LoadProperties extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            ElementProperties properties = Database.getElementProperties(context, params[0]);

            propertiesPairs = new String[][] {
                    { getString(R.string.properties_header_general), null },
                    { getString(R.string.property_symbol), properties.getSymbol() },
                    { getString(R.string.property_atomic_number), intToStr(properties.getAtomicNumber()) },
                    { getString(R.string.property_weight), properties.getStandardAtomicWeight() },
                    { getString(R.string.property_group),
                            intToStr(properties.getGroup() > 0 ? properties.getGroup() : 3) },
                    { getString(R.string.property_period), intToStr(properties.getPeriod()) },
                    { getString(R.string.property_block), properties.getBlock() },
                    { getString(R.string.property_category), properties.getCategory() },
                    { getString(R.string.property_electron_configuration),
                            properties.getElectronConfiguration() },
                    { getString(R.string.properties_header_physical), null },
                    { getString(R.string.property_appearance), properties.getAppearance() },
                    { getString(R.string.property_phase), properties.getPhase() }
            };

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyDataSetChanged();
        }
    }

    public PropertiesAdapter(Context context, int atomicNumber) {
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
        View view = LayoutInflater.from(context).inflate(getItemViewType(position) == VIEW_TYPE_ITEM
                ? R.layout.properties_list_item : R.layout.properties_list_header, parent, false);

        String[] property = getItem(position);

        if(getItemViewType(position) == VIEW_TYPE_ITEM) {
            ((TextView)view.findViewById(R.id.property_name)).setText(property[0]);
            ((TextView)view.findViewById(R.id.property_value)).setText(!property[1].equals("") ?
                    property[1] : getString(R.string.property_value_unknown));
        }
        else {
            ((TextView)view).setText(property[0]);
        }

        return view;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != VIEW_TYPE_HEADER;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position)[1] != null ? VIEW_TYPE_ITEM : VIEW_TYPE_HEADER;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private String getString(int resId) {
        return context.getString(resId);
    }

    private String intToStr(int integer) {
        return String.valueOf(integer);
    }
}
