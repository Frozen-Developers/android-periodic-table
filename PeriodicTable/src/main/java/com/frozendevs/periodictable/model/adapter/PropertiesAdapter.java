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
import com.frozendevs.periodictable.model.TableItem;

import java.util.Arrays;

public class PropertiesAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_SUMMARY = 2;

    private Context mContext;
    private Typeface mTypeface;
    private TableAdapter mTableAdapter;
    private Property[] mPropertiesPairs = new Property[0];
    private View mTileView;

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

        mTableAdapter = new TableAdapter(context);
        mTableAdapter.setItems(Arrays.asList((TableItem)properties));

        mPropertiesPairs = new Property[] {
                new Property(R.string.properties_header_summary, null),
                null,
                new Property(R.string.properties_header_general, null),
                new Property(R.string.property_symbol, properties.getSymbol()),
                new Property(R.string.property_atomic_number, properties.getNumber()),
                new Property(R.string.property_weight, properties.getStandardAtomicWeight()),
                new Property(R.string.property_group, properties.getGroup()),
                new Property(R.string.property_period, properties.getPeriod()),
                new Property(R.string.property_block, properties.getBlock()),
                new Property(R.string.property_category, properties.getCategory()),
                new Property(R.string.property_electron_configuration, properties.getElectronConfiguration()),
                new Property(R.string.property_electrons_per_shell, properties.getElectronsPerShell()),
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
                new Property(R.string.property_thermal_diffusivity, properties.getThermalDiffusivity()),
                new Property(R.string.property_electrical_resistivity, properties.getElectricalResistivity()),
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
        Property property = getItem(position);

        if(property == null) {
            if(convertView == null || (Integer)convertView.getTag() != VIEW_TYPE_SUMMARY) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.properties_summary_item, parent, false);
                convertView.setTag(VIEW_TYPE_SUMMARY);
            }

            ElementProperties properties = (ElementProperties)mTableAdapter.getAllItems().get(0);

            if(mTileView == null) {
                mTileView = mTableAdapter.getView(mTableAdapter.getItemPosition(properties),
                        null, (ViewGroup)convertView);
                mTileView.setEnabled(false);
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

            TextView electronegativity = (TextView)convertView.findViewById(R.id.element_electronegativity);
            electronegativity.setText(mContext.getString(R.string.property_electronegativity_symbol) + ": ");
            if(properties.getElectronegativity() != null &&
                    properties.getElectronegativity().equals("-")) {
                electronegativity.append(mContext.getString(R.string.property_value_none));
            }
            else if(properties.getElectronegativity() != null &&
                    !properties.getElectronegativity().equals("")) {
                electronegativity.append(properties.getElectronegativity());
            }
            else {
                electronegativity.append(mContext.getString(R.string.property_value_unknown));
            }

            TextView oxidationStates = (TextView)convertView.findViewById(R.id.element_oxidation_states);
            if(properties.getOxidationStates() != null && !properties.getOxidationStates().equals("")) {
                oxidationStates.setText(properties.getOxidationStates());
            }
            else {
                oxidationStates.setText(R.string.property_value_unknown);
            }
        }
        else {
            int viewType = getItemViewType(position);

            if(convertView == null || (Integer)convertView.getTag() != viewType) {
                convertView = LayoutInflater.from(mContext).inflate(viewType == VIEW_TYPE_ITEM ?
                        R.layout.properties_list_item : R.layout.properties_list_header, parent, false);
                convertView.setTag(viewType);
            }

            if (viewType == VIEW_TYPE_ITEM) {
                TextView name = (TextView) convertView.findViewById(R.id.property_name);
                name.setText(property.getName());

                TextView value = (TextView) convertView.findViewById(R.id.property_value);
                if(property.getValue() != null && property.getValue().equals("-")) {
                    value.setText(mContext.getString(R.string.property_value_none));
                }
                else if(property.getValue() != null && !property.getValue().equals("")) {
                    value.setText(property.getValue());
                }
                else {
                    value.setText(R.string.property_value_unknown);
                }
                value.setTypeface(mTypeface);
            } else {
                ((TextView) convertView).setText(property.getName());
            }
        }

        return convertView;
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
