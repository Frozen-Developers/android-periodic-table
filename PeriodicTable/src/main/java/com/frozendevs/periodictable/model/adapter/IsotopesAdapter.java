package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.Isotope;

public class IsotopesAdapter extends BaseExpandableListAdapter {

    private Context context;

    private ElementProperties properties;
    private Typeface typeface;
    private Isotope[] isotopes = new Isotope[0];

    private class LoadProperties extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            properties = Database.getElementProperties(context, params[0]);
            isotopes = Database.getIsotopes(context, params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyDataSetChanged();
        }
    }

    public IsotopesAdapter(Context context, int atomicNumber) {
        this.context = context;

        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");

        new LoadProperties().execute(atomicNumber);
    }

    @Override
    public int getGroupCount() {
        return isotopes.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 4;
    }

    @Override
    public String getGroup(int groupPosition) {
        return isotopes[groupPosition].getSymbol();
    }

    @Override
    public String[] getChild(int groupPosition, int childPosition) {
        switch (childPosition) {
            case 0:
                return new String[] { getString(R.string.property_half_life),
                        isotopes[groupPosition].getHalfLife() };

            case 1:
                String[] decayModes = isotopes[groupPosition].getDecayModes();
                String[] daughterIsotopes = isotopes[groupPosition].getDaughterIsotopes();

                String combined = "";
                for(int i = 0; i < decayModes.length; i++) {
                    combined += decayModes[i] + " -> " + daughterIsotopes[i];
                    if(i < decayModes.length - 1) combined += "\n";
                }

                return new String[] { getString(R.string.property_decay_modes), combined };

            case 2:
                return new String[] { getString(R.string.property_spin),
                        isotopes[groupPosition].getSpin() };

            case 3:
                return new String[] { getString(R.string.property_abundance),
                        isotopes[groupPosition].getAbundance() };
        }

        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition + 1;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getGroupId(groupPosition) * (childPosition + 1);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.isotope_list_item, parent, false);

        ((CheckedTextView)view).setText(getGroup(groupPosition) + properties.getSymbol());
        ((CheckedTextView)view).setTypeface(typeface);

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.properties_list_item, parent, false);

        String[] property = getChild(groupPosition, childPosition);

        TextView name = (TextView)view.findViewById(R.id.property_name);
        name.setText(property[0]);
        name.setTypeface(typeface);

        TextView value = (TextView)view.findViewById(R.id.property_value);
        if((getChild(groupPosition, 0)[1].equals("Stable") &&
                (childPosition == 1 || childPosition == 2)) ||
                (property[1].equals("") && childPosition == 4))
            value.setText(getString(R.string.property_value_none));
        else if(property[1].equals(""))
            value.setText(getString(R.string.property_value_unknown));
        else
            value.setText(property[1]);
        value.setTypeface(typeface);

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private String getString(int resId) {
        return context.getString(resId);
    }
}
