package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.Isotope;
import com.frozendevs.periodictable.view.ExpandableIndicatorView;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

public class IsotopesAdapter extends AbstractExpandableItemAdapter<IsotopesAdapter.GroupViewHolder,
        IsotopesAdapter.ChildViewHolder> {

    private Context mContext;
    private Typeface mTypeface;
    private IsotopeProperties[] mProperties = new IsotopeProperties[0];
    private RecyclerViewExpandableItemManager mItemManager;

    private class Property {
        String mName = "", mValue = "", mValueRaw = "";

        Property(String value) {
            if (value != null) {
                mValueRaw = value;
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

        String getValueRaw() {
            return mValueRaw;
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
            decayModes = new Property(
                    R.string.property_decay_modes, isotope.getHalfLife().equals("-") ?
                    mContext.getString(R.string.property_value_none) : isotope.getDecayModes());
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

    public class GroupViewHolder extends AbstractExpandableItemViewHolder implements
            View.OnClickListener, View.OnCreateContextMenuListener {
        private TextView mSymbol, mHalfLife, mAbundance;
        private ExpandableIndicatorView mIndicator;
        private int mPosition = 0;

        public GroupViewHolder(View view) {
            super(view);

            mIndicator = (ExpandableIndicatorView) view.findViewById(R.id.group_indicator);
            mSymbol = (TextView) view.findViewById(R.id.property_symbol);
            mHalfLife = (TextView) view.findViewById(R.id.property_half_life);
            mAbundance = (TextView) view.findViewById(R.id.property_abundance);

            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

        public void setIndicatorState(boolean expanded) {
            mIndicator.setStateExpanded(expanded);
        }

        public void setSymbol(String symbol) {
            mSymbol.setText(symbol);
        }

        public void setHalfLife(String halfLife) {
            mHalfLife.setText(halfLife);
        }

        public void setAbundance(String abundance) {
            mAbundance.setText(abundance);
        }

        public void setTypeface(Typeface typeface) {
            mSymbol.setTypeface(typeface);
            mHalfLife.setTypeface(typeface);
            mAbundance.setTypeface(typeface);
        }

        public void setPosition(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View view) {
            if ((getExpandStateFlags() & RecyclerViewExpandableItemManager.
                    STATE_FLAG_IS_EXPANDED) == 0) {
                IsotopesAdapter.this.mItemManager.expandGroup(mPosition);
            } else {
                IsotopesAdapter.this.mItemManager.collapseGroup(mPosition);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                                        ContextMenu.ContextMenuInfo menuInfo) {
        }
    }

    public class ChildViewHolder extends AbstractExpandableItemViewHolder implements
            View.OnClickListener, View.OnCreateContextMenuListener {
        private TextView mName, mValue;

        public ChildViewHolder(View view) {
            super(view);

            mName = (TextView) view.findViewById(R.id.property_name);
            mValue = (TextView) view.findViewById(R.id.property_value);

            view.setOnClickListener(this);
            view.setOnCreateContextMenuListener(this);
        }

        public void setName(String name) {
            mName.setText(name);
        }

        public void setValue(String value) {
            mValue.setText(value);
        }

        public void setTypeface(Typeface typeface) {
            mName.setTypeface(typeface);
            mValue.setTypeface(typeface);
        }

        @Override
        public void onClick(View view) {
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                                        ContextMenu.ContextMenuInfo menuInfo) {
        }
    }

    public IsotopesAdapter(Context context, RecyclerViewExpandableItemManager manager,
                           Isotope[] isotopes) {
        mContext = context;

        mItemManager = manager;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        if (isotopes != null) {
            mProperties = new IsotopeProperties[isotopes.length];

            for (int i = 0; i < isotopes.length; i++) {
                mProperties[i] = new IsotopeProperties(isotopes[i]);
            }
        }

        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return mProperties.length;
    }

    @Override
    public int getChildCount(int groupPosition) {
        return 4;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mProperties[groupPosition].hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChild(groupPosition, childPosition).hashCode();
    }

    @Override
    public int getGroupItemViewType(int groupPosition) {
        return 0;
    }

    @Override
    public int getChildItemViewType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup viewGroup, int viewType) {
        GroupViewHolder viewHolder = new GroupViewHolder(LayoutInflater.from(viewGroup.
                getContext()).inflate(R.layout.isotope_list_item, viewGroup, false));

        viewHolder.setTypeface(mTypeface);

        return viewHolder;
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup, int viewType) {
        ChildViewHolder viewHolder = new ChildViewHolder(LayoutInflater.from(viewGroup.
                getContext()).inflate(R.layout.properties_list_item, viewGroup, false));

        viewHolder.setTypeface(mTypeface);

        return viewHolder;
    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder groupViewHolder, int groupPosition,
                                      int viewType) {
        IsotopeProperties properties = mProperties[groupPosition];

        groupViewHolder.setPosition(groupPosition);
        groupViewHolder.setSymbol(properties.getSymbol().getValue());
        groupViewHolder.setHalfLife("");
        groupViewHolder.setAbundance("");
        if (!properties.getHalfLife().getValueRaw().equals("")) {
            groupViewHolder.setHalfLife(properties.getHalfLife().getValue());

            if (!properties.getAbundance().getValueRaw().equals("")) {
                groupViewHolder.setAbundance(properties.getAbundance().getValue());
            }
        }

        final int expandState = groupViewHolder.getExpandStateFlags();

        if ((expandState & RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED) != 0) {
            groupViewHolder.setIndicatorState((expandState & RecyclerViewExpandableItemManager.
                    STATE_FLAG_IS_EXPANDED) != 0);
        }
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder childViewHolder, int groupPosition,
                                      int childPosition, int viewType) {
        Property property = getChild(groupPosition, childPosition);

        childViewHolder.setName(property.getName());
        childViewHolder.setValue(property.getValue());
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(GroupViewHolder groupViewHolder,
                                                   int groupPosition, int x, int y,
                                                   boolean expand) {
        return false;
    }

    private Property getChild(int groupPosition, int childPosition) {
        IsotopeProperties properties = mProperties[groupPosition];

        switch (childPosition) {
            case 0:
                return properties.getHalfLife();
            case 1:
                return properties.getDecayModes();
            case 2:
                return properties.getSpin();
            default:
                return properties.getAbundance();
        }
    }
}
