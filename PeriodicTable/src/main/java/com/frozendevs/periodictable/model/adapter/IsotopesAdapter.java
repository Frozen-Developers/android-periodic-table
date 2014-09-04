package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.Isotope;

public class IsotopesAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private Typeface mTypeface;
    private IsotopeProperties[] mProperties = new IsotopeProperties[0];

    private class Property {
        String mName = "", mValue = "";

        Property(String value) {
            if (value != null) {
                if (!value.equals("")) mValue = value;
                else mValue = mContext.getString(R.string.property_value_unknown);
            }
        }

        Property(int name, String value) {
            this(value);

            mName = mContext.getString(name);
        }

        Property(int name, String value, int noneValue) {
            this(name, value);

            if (value != null && value.equals("")) {
                mValue = mContext.getString(noneValue);
            }
        }

        Property(int name, String value, int noneValue, int specialValue) {
            this(name, value, noneValue);

            if (value != null && value.equals("-")) {
                mValue = mContext.getString(specialValue);
            }
        }

        String getName() {
            return mName;
        }

        String getValue() {
            return mValue;
        }
    }

    private class IsotopeProperties {

        Property symbol, halfLife, spin, abundance, decayModes;

        public IsotopeProperties(Isotope isotope) {
            symbol = new Property(isotope.getSymbol());
            halfLife = new Property(R.string.property_half_life, isotope.getHalfLife(),
                    R.string.property_value_unknown, R.string.property_value_stable);
            spin = new Property(R.string.property_spin, isotope.getSpin(),
                    R.string.property_value_unknown);
            abundance = new Property(R.string.property_abundance, isotope.getAbundance(),
                    R.string.property_value_none, R.string.property_value_trace);
            decayModes = new Property(R.string.property_decay_modes, isotope.getHalfLife().equals("-")
                    ? mContext.getString(R.string.property_value_none) : isotope.getDecayModes());
        }

        public Property getSymbol() {
            return symbol;
        }

        public Property getHalfLife() {
            return halfLife;
        }

        public Property getSpin() {
            return spin;
        }

        public Property getAbundance() {
            return abundance;
        }

        public Property getDecayModes() {
            return decayModes;
        }
    }

    public IsotopesAdapter(Context context, Isotope[] isotopes) {
        mContext = context;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        if (isotopes != null) {
            mProperties = new IsotopeProperties[isotopes.length];

            for (int i = 0; i < isotopes.length; i++) {
                mProperties[i] = new IsotopeProperties(isotopes[i]);
            }
        }
    }

    @Override
    public int getGroupCount() {
        return mProperties.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 4;
    }

    @Override
    public IsotopeProperties getGroup(int groupPosition) {
        return mProperties[groupPosition];
    }

    @Override
    public Property getChild(int groupPosition, int childPosition) {
        IsotopeProperties properties = getGroup(groupPosition);

        switch (childPosition) {
            case 0:
                return properties.getHalfLife();
            case 1:
                return properties.getDecayModes();
            case 2:
                return properties.getSpin();
            case 3:
                return properties.getAbundance();
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
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.isotope_list_item, parent, false);
        }

        IsotopeProperties properties = getGroup(groupPosition);

        TextView symbol = (TextView) convertView.findViewById(R.id.property_symbol);
        symbol.setText(properties.getSymbol().getValue());
        symbol.setTypeface(mTypeface);

        TextView halfLife = (TextView) convertView.findViewById(R.id.property_half_life);
        halfLife.setText(mContext.getString(R.string.property_half_life_symbol) + ": " +
                properties.getHalfLife().getValue());
        halfLife.setTypeface(mTypeface);

        TextView abundance = (TextView) convertView.findViewById(R.id.property_abundance);
        abundance.setText(mContext.getString(R.string.property_natural_abundance_symbol) + ": " +
                properties.getAbundance().getValue());
        abundance.setTypeface(mTypeface);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.properties_list_item, parent, false);
        }

        Property property = getChild(groupPosition, childPosition);

        TextView name = (TextView) convertView.findViewById(R.id.property_name);
        name.setText(property.getName());

        TextView value = (TextView) convertView.findViewById(R.id.property_value);
        value.setText(property.getValue());
        value.setTypeface(mTypeface);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
