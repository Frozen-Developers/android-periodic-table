package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementProperties;

public class PropertiesAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_SUMMARY = 2;

    private Context mContext;
    private Typeface mTypeface;
    private ElementProperties mElementProperties;
    private Property[] mPropertiesPairs = new Property[0];

    private class Property {
        int mName;
        String mValue;

        Property(int name, String value) {
            mName = name;
            mValue = value;
        }

        Property(int name, int value) {
            this(name, String.valueOf(value));
        }

        int getName() {
            return mName;
        }

        String getValue() {
            return mValue;
        }
    }

    public PropertiesAdapter(Context context, ElementProperties properties) {
        mContext = context;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        mElementProperties = properties;

        mPropertiesPairs = new Property[] {
                new Property(R.string.properties_header_summary, null),
                null,
                new Property(R.string.properties_header_general, null),
                new Property(R.string.property_symbol, properties.getSymbol()),
                new Property(R.string.property_atomic_number, properties.getAtomicNumber()),
                new Property(R.string.property_weight, properties.getStandardAtomicWeight()),
                new Property(R.string.property_group, properties.getGroup() > 0 ? properties.getGroup() : 3),
                new Property(R.string.property_period, properties.getPeriod()),
                new Property(R.string.property_block, properties.getBlock()),
                new Property(R.string.property_category, properties.getCategory()),
                new Property(R.string.property_electron_configuration, properties.getElectronConfiguration()),
                new Property(R.string.properties_header_physical, null),
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
                new Property(R.string.properties_header_atomic, null),
                new Property(R.string.property_oxidation_states, properties.getOxidationStates()),
                new Property(R.string.property_electronegativity, properties.getElectronegativity()),
                new Property(R.string.property_ionization_energies, properties.getIonizationEnergies()),
                new Property(R.string.property_atomic_radius, properties.getAtomicRadius()),
                new Property(R.string.property_covalent_radius, properties.getCovalentRadius()),
                new Property(R.string.property_van_der_waals_radius, properties.getVanDerWaalsRadius()),
                new Property(R.string.properties_header_miscellanea, null),
                new Property(R.string.property_crystal_structure, properties.getCrystalStructure()),
                new Property(R.string.property_magnetic_ordering, properties.getMagneticOrdering()),
                new Property(R.string.property_thermal_conductivity, properties.getThermalConductivity()),
                new Property(R.string.property_thermal_expansion, properties.getThermalExpansion()),
                new Property(R.string.property_speed_of_sound, properties.getSpeedOfSound()),
                new Property(R.string.property_youngs_modulus, properties.getYoungsModulus()),
                new Property(R.string.property_shear_modulus, properties.getShearModulus()),
                new Property(R.string.property_bulk_modulus, properties.getBulkModulus()),
                new Property(R.string.property_mohs_hardness, properties.getMohsHardness()),
                new Property(R.string.property_brinell_hardness, properties.getBrinellHardness())
        };
    }

    @Override
    public int getCount() {
        return mPropertiesPairs.length;
    }

    @Override
    public Property getItem(int position) {
        return mPropertiesPairs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        Property property = getItem(position);

        if(property == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.properties_summary_item, parent, false);

            view.findViewById(R.id.table_item).setBackgroundColor(mElementProperties.getBackgroundColor(mContext));

            ((TextView)view.findViewById(R.id.element_symbol)).setText(mElementProperties.getSymbol());
            ((TextView)view.findViewById(R.id.element_number)).setText(String.valueOf(
                    mElementProperties.getAtomicNumber()));
            ((TextView)view.findViewById(R.id.element_name)).setText(mElementProperties.getName());
            ((TextView)view.findViewById(R.id.element_weight)).setText(
                    mElementProperties.getStandardAtomicWeight());
            ((TextView)view.findViewById(R.id.element_electron_configuration)).setText(
                    mElementProperties.getElectronConfiguration());
            ((TextView)view.findViewById(R.id.element_electron_configuration)).setTypeface(mTypeface);
            ((TextView)view.findViewById(R.id.element_electronegativity)).setText(
                    mContext.getString(R.string.property_electronegativity_symbol) +
                    ": " + (!mElementProperties.getElectronegativity().equals("") ?
                    mElementProperties.getElectronegativity() : mContext.getString(R.string.property_value_unknown)));
            ((TextView)view.findViewById(R.id.element_oxidation_states)).setText(
                    mElementProperties.getOxidationStates());
        }
        else {
            view = LayoutInflater.from(mContext).inflate(getItemViewType(position) == VIEW_TYPE_ITEM
                    ? R.layout.properties_list_item : R.layout.properties_list_header, parent, false);

            if (getItemViewType(position) == VIEW_TYPE_ITEM) {
                ((TextView) view.findViewById(R.id.property_name)).setText(property.getName());

                TextView value = (TextView) view.findViewById(R.id.property_value);
                value.setText(!property.getValue().equals("") ? property.getValue() :
                        mContext.getString(R.string.property_value_unknown));
                value.setTypeface(mTypeface);
            } else {
                ((TextView) view).setText(property.getName());
            }
        }

        return view;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position) == null) return VIEW_TYPE_SUMMARY;

        return getItem(position).getValue() != null ? VIEW_TYPE_ITEM : VIEW_TYPE_HEADER;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }
}
