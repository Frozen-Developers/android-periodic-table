package com.frozendevs.periodictable.model.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.TableItem;

import java.util.Arrays;

public class PropertiesAdapter extends BaseExpandableListAdapter implements
        ExpandableListView.OnGroupClickListener {

    private static final int PROPERTY_TYPE_HEADER = 0;
    private static final int PROPERTY_TYPE_ITEM = 1;
    private static final int PROPERTY_TYPE_SUMMARY = 2;

    private Context mContext;
    private Typeface mTypeface;
    private TableAdapter mTableAdapter;
    private Property[] mPropertiesPairs = new Property[0];
    private View mTileView;
    private StateListDrawable mGroupIndicator;
    private AlertDialog mLegendDialog;

    private class Property {
        int mName;
        String mValue = "";
        int mType = PROPERTY_TYPE_ITEM;

        Property(int name, String value) {
            mName = name;

            if(value != null) {
                if(value.equals("")) {
                    mValue = mContext.getString(R.string.property_value_unknown);
                }
                else if(value.equals("-")) {
                    mValue = mContext.getString(R.string.property_value_none);
                }
                else mValue = value;
            }
        }

        Property(int name, int value) {
            this(name, String.valueOf(value));
        }

        Property(int name, String value, int type) {
            this(name, value);

            mType = type;
        }

        int getName() {
            return mName;
        }

        String getValue() {
            return mValue;
        }

        int getType() {
            return mType;
        }
    }

    public PropertiesAdapter(Context context, ElementProperties properties) {
        mContext = context;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        Resources.Theme theme = mContext.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(android.R.attr.expandableListViewStyle, typedValue , true);
        TypedArray typedArray = theme.obtainStyledAttributes(typedValue.resourceId,
                new int[] { android.R.attr.groupIndicator });
        mGroupIndicator = (StateListDrawable)typedArray.getDrawable(0);
        typedArray.recycle();

        mTableAdapter = new TableAdapter(context);
        mTableAdapter.setItems(Arrays.asList((TableItem)properties));

        mPropertiesPairs = new Property[] {
                new Property(R.string.properties_header_summary, null, PROPERTY_TYPE_HEADER),
                null,
                new Property(R.string.properties_header_general, null, PROPERTY_TYPE_HEADER),
                new Property(R.string.property_symbol, properties.getSymbol()),
                new Property(R.string.property_atomic_number, properties.getNumber()),
                new Property(R.string.property_weight, properties.getStandardAtomicWeight()),
                new Property(R.string.property_group, properties.getGroup()),
                new Property(R.string.property_period, properties.getPeriod()),
                new Property(R.string.property_block, properties.getBlock()),
                new Property(R.string.property_category, properties.getCategory()),
                new Property(R.string.property_electron_configuration, properties.getElectronConfiguration()),
                new Property(R.string.property_electrons_per_shell, properties.getElectronsPerShell()),
                new Property(R.string.properties_header_physical, null, PROPERTY_TYPE_HEADER),
                new Property(R.string.property_appearance, properties.getAppearance()),
                new Property(R.string.property_phase, properties.getPhase()),
                new Property(R.string.property_density, properties.getDensity()),
                new Property(R.string.property_liquid_density_at_mp, properties.getLiquidDensityAtMeltingPoint()),
                new Property(R.string.property_liquid_density_at_bp, properties.getLiquidDensityAtBoilingPoint()),
                new Property(R.string.property_melting_point, properties.getMeltingPoint()),
                new Property(R.string.property_sublimation_point, properties.getSublimationPoint()),
                new Property(R.string.property_boiling_point, properties.getBoilingPoint()),
                new Property(R.string.property_triple_point, properties.getTriplePoint()),
                new Property(R.string.property_critical_point, properties.getCriticalPoint()),
                new Property(R.string.property_heat_of_fusion, properties.getHeatOfFusion()),
                new Property(R.string.property_heat_of_vaporization,properties.getHeatOfVaporization()),
                new Property(R.string.property_molar_heat_capacity, properties.getMolarHeatCapacity()),
                new Property(R.string.properties_header_atomic, null, PROPERTY_TYPE_HEADER),
                new Property(R.string.property_oxidation_states, properties.getOxidationStates()),
                new Property(R.string.property_electronegativity, properties.getElectronegativity()),
                new Property(R.string.property_ionization_energies, properties.getIonizationEnergies()),
                new Property(R.string.property_atomic_radius, properties.getAtomicRadius()),
                new Property(R.string.property_covalent_radius, properties.getCovalentRadius()),
                new Property(R.string.property_van_der_waals_radius, properties.getVanDerWaalsRadius()),
                new Property(R.string.properties_header_miscellanea, null, PROPERTY_TYPE_HEADER),
                new Property(R.string.property_crystal_structure, properties.getCrystalStructure()),
                new Property(R.string.property_magnetic_ordering, properties.getMagneticOrdering()),
                new Property(R.string.property_thermal_conductivity, properties.getThermalConductivity()),
                new Property(R.string.property_thermal_expansion, properties.getThermalExpansion()),
                new Property(R.string.property_thermal_diffusivity, properties.getThermalDiffusivity()),
                new Property(R.string.property_electrical_resistivity, properties.getElectricalResistivity()),
                new Property(R.string.property_band_gap, properties.getBandGap()),
                new Property(R.string.property_curie_point, properties.getCuriePoint()),
                new Property(R.string.property_tensile_strength, properties.getTensileStrength()),
                new Property(R.string.property_speed_of_sound, properties.getSpeedOfSound()),
                new Property(R.string.property_poisson_ratio, properties.getPoissonRatio()),
                new Property(R.string.property_youngs_modulus, properties.getYoungsModulus()),
                new Property(R.string.property_shear_modulus, properties.getShearModulus()),
                new Property(R.string.property_bulk_modulus, properties.getBulkModulus()),
                new Property(R.string.property_mohs_hardness, properties.getMohsHardness()),
                new Property(R.string.property_vickers_hardness, properties.getVickersHardness()),
                new Property(R.string.property_brinell_hardness, properties.getBrinellHardness()),
                new Property(R.string.property_cas_number, properties.getCasNumber())
        };
    }

    @Override
    public int getGroupCount() {
        return mPropertiesPairs.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 0;
    }

    @Override
    public Property getGroup(int groupPosition) {
        return mPropertiesPairs[groupPosition];
    }

    @Override
    public Property getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {
        Property property = getGroup(groupPosition);

        if(property == null) {
            if(convertView == null || (Integer)convertView.getTag() != PROPERTY_TYPE_SUMMARY) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.properties_summary_item, parent, false);
                convertView.setTag(PROPERTY_TYPE_SUMMARY);
            }

            ElementProperties properties = (ElementProperties)mTableAdapter.getAllItems().get(0);

            if(mTileView == null) {
                mTileView = mTableAdapter.getView(mTableAdapter.getItemPosition(properties),
                        null, (ViewGroup)convertView);
                mTileView.setClickable(false);
                mTileView.setDuplicateParentStateEnabled(true);
            }

            if(convertView.findViewById(mTileView.getId()) == null) {
                if(mTileView.getParent() != null) {
                    ((ViewGroup) mTileView.getParent()).removeView(mTileView);
                }
                ((ViewGroup) convertView).addView(mTileView, 0);
            }

            TextView configuration = (TextView)convertView.findViewById(R.id.element_electron_configuration);
            if(properties.getElectronConfiguration() != null &&
                    !properties.getElectronConfiguration().equals("")) {
                configuration.setText(properties.getElectronConfiguration());
            }
            else {
                configuration.setText(R.string.property_value_unknown);
            }
            configuration.setTypeface(mTypeface);

            TextView shells = (TextView)convertView.findViewById(R.id.element_electrons_per_shell);
            if(properties.getElectronsPerShell() != null &&
                    !properties.getElectronsPerShell().equals("")) {
                shells.setText(properties.getElectronsPerShell());
            }
            else {
                shells.setText(R.string.property_value_unknown);
            }
            shells.setTypeface(mTypeface);

            TextView electronegativity = (TextView)convertView.findViewById(R.id.element_electronegativity);
            if(properties.getElectronegativity() != null &&
                    properties.getElectronegativity().equals("-")) {
                electronegativity.setText(R.string.property_value_none);
            }
            else if(properties.getElectronegativity() != null &&
                    !properties.getElectronegativity().equals("")) {
                electronegativity.setText(properties.getElectronegativity());
            }
            else {
                electronegativity.setText(R.string.property_value_unknown);
            }
            electronegativity.setTypeface(mTypeface);

            TextView oxidationStates = (TextView)convertView.findViewById(R.id.element_oxidation_states);
            if(properties.getOxidationStates() != null && !properties.getOxidationStates().equals("")) {
                oxidationStates.setText(properties.getOxidationStates());
            }
            else {
                oxidationStates.setText(R.string.property_value_unknown);
            }
            oxidationStates.setTypeface(mTypeface);
        }
        else {
            int viewType = property.getType();

            if(convertView == null || (Integer)convertView.getTag() != viewType) {
                convertView = LayoutInflater.from(mContext).inflate(viewType == PROPERTY_TYPE_ITEM ?
                        R.layout.properties_list_item : R.layout.properties_list_header, parent, false);
                convertView.setTag(viewType);
            }

            if (viewType == PROPERTY_TYPE_ITEM) {
                ImageView indicator = (ImageView) convertView.findViewById(R.id.group_indicator);

                TextView name = (TextView) convertView.findViewById(R.id.property_name);
                name.setText(property.getName());

                TextView value = (TextView) convertView.findViewById(R.id.property_value);
                String[] lines = property.getValue().split("\\n");
                if(lines.length > 3) {
                    mGroupIndicator.setState(isExpanded ?
                            new int[] { android.R.attr.state_expanded } : null);
                    indicator.setImageDrawable(mGroupIndicator.getCurrent());
                    indicator.setVisibility(View.VISIBLE);

                    value.setText(property.getValue());
                    if (isExpanded) value.setMaxLines(Integer.MAX_VALUE);
                    else value.setMaxLines(1);
                }
                else {
                    indicator.setVisibility(View.GONE);

                    value.setText(property.getValue());
                }
                value.setTypeface(mTypeface);
            } else {
                ((TextView) convertView).setText(property.getName());

                convertView.setOnClickListener(null);
            }
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        Property property = getGroup(groupPosition);

        if(property == null) {
            if(mLegendDialog == null) {
                mLegendDialog = new AlertDialog.Builder(mContext).create();
                mLegendDialog.setTitle(R.string.context_title_legend);
                
                ElementProperties properties =
                        (ElementProperties)mTableAdapter.getAllItems().get(0);

                View view = LayoutInflater.from(mContext).inflate(
                        R.layout.properties_summary_item, null);

                View tileView = mTableAdapter.getView(
                        mTableAdapter.getItemPosition(properties), null, (ViewGroup) view);
                tileView.setEnabled(false);
                ((TextView) tileView.findViewById(R.id.element_symbol)).setText(
                        R.string.property_atom_symbol);
                ((TextView) tileView.findViewById(R.id.element_number)).setText(
                        R.string.property_atomic_number_symbol);
                ((TextView) tileView.findViewById(R.id.element_name)).setText(
                        R.string.property_name);
                ((TextView) tileView.findViewById(R.id.element_weight)).setText(
                        R.string.property_relative_atomic_mass_symbol);

                ((ViewGroup) view).addView(tileView, 0);

                TextView configuration =
                        (TextView) view.findViewById(R.id.element_electron_configuration);
                configuration.setText(R.string.property_electron_configuration);
                configuration.setTypeface(mTypeface);

                TextView shells = (TextView) view.findViewById(R.id.element_electrons_per_shell);
                shells.setText(R.string.property_electrons_per_shell);
                shells.setTypeface(mTypeface);

                TextView electronegativity =
                        (TextView) view.findViewById(R.id.element_electronegativity);
                electronegativity.setText(
                        mContext.getString(R.string.property_electronegativity));
                electronegativity.setTypeface(mTypeface);

                TextView oxidationStates =
                        (TextView) view.findViewById(R.id.element_oxidation_states);
                oxidationStates.setText(R.string.property_oxidation_states);
                oxidationStates.setTypeface(mTypeface);

                mLegendDialog.setView(view);
            }

            if(!mLegendDialog.isShowing()) {
                mLegendDialog.show();
            }
        }

        return false;
    }
}
