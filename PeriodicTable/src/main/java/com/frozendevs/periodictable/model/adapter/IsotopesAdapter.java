package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementProperties.Isotope;

public class IsotopesAdapter extends BaseExpandableListAdapter {

    private static final int PROPERTY_HALF_LIFE = 0;
    private static final int PROPERTY_DECAY_MODES = 1;
    private static final int PROPERTY_SPIN = 2;
    private static final int PROPERTY_ABUNDANCE = 3;

    private Context mContext;

    private Typeface mTypeface;
    private Isotope[] mIsotopes;

    public IsotopesAdapter(Context context, Isotope[] isotopes) {
        mContext = context;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        mIsotopes = isotopes;
    }

    @Override
    public int getGroupCount() {
        return mIsotopes.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 4;
    }

    @Override
    public String getGroup(int groupPosition) {
        return mIsotopes[groupPosition].getSymbol();
    }

    @Override
    public String[] getChild(int groupPosition, int childPosition) {
        switch (childPosition) {
            case PROPERTY_HALF_LIFE:
                return new String[] { getString(R.string.property_half_life),
                        mIsotopes[groupPosition].getHalfLife() };

            case PROPERTY_DECAY_MODES:
                String[] decayModes = mIsotopes[groupPosition].getDecayModes();
                String[] daughterIsotopes = mIsotopes[groupPosition].getDaughterIsotopes();

                String combined = "";
                for(int i = 0; i < decayModes.length && i < daughterIsotopes.length; i++) {
                    combined += decayModes[i];
                    if(i < daughterIsotopes.length)
                        combined += " \u2192 " + daughterIsotopes[i];
                    if(i < decayModes.length - 1)
                        combined += "\n";
                }

                return new String[] { getString(R.string.property_decay_modes), combined };

            case PROPERTY_SPIN:
                return new String[] { getString(R.string.property_spin),
                        mIsotopes[groupPosition].getSpin() };

            case PROPERTY_ABUNDANCE:
                return new String[] { getString(R.string.property_abundance),
                        mIsotopes[groupPosition].getAbundance() };
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
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.isotope_list_item, parent, false);
        }

        TextView symbol = (TextView)convertView.findViewById(R.id.property_symbol);
        symbol.setText(getGroup(groupPosition));
        symbol.setTypeface(mTypeface);

        String propertyHalfLife = getChild(groupPosition, PROPERTY_HALF_LIFE)[1];

        TextView halfLife = (TextView)convertView.findViewById(R.id.property_half_life);
        halfLife.setText(getString(R.string.property_half_life_symbol) + ": " +
                (!propertyHalfLife.equals("") ? propertyHalfLife : getString(R.string.property_value_unknown)));
        halfLife.setTypeface(mTypeface);

        String propertyAbundance = getChild(groupPosition, PROPERTY_ABUNDANCE)[1];

        TextView abundance = (TextView)convertView.findViewById(R.id.property_abundance);
        abundance.setText(getString(R.string.property_natural_abundance_symbol) + ": " +
                (!propertyAbundance.equals("") ? propertyAbundance : getString(R.string.property_value_none)));
        abundance.setTypeface(mTypeface);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.properties_list_item, parent, false);
        }

        String[] property = getChild(groupPosition, childPosition);

        TextView name = (TextView)convertView.findViewById(R.id.property_name);
        name.setText(property[0]);

        TextView value = (TextView)convertView.findViewById(R.id.property_value);
        if((getChild(groupPosition, 0)[1].equalsIgnoreCase("Stable") && childPosition == PROPERTY_DECAY_MODES)
                || (property[1].equals("") && childPosition == PROPERTY_ABUNDANCE))
            value.setText(getString(R.string.property_value_none));
        else if(property[1].equals(""))
            value.setText(getString(R.string.property_value_unknown));
        else
            value.setText(property[1]);
        value.setTypeface(mTypeface);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private String getString(int resId) {
        return mContext.getString(resId);
    }
}
