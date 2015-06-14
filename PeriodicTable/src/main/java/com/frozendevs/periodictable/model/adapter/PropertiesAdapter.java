package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.view.RecyclerView;

public class PropertiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    private static final int[] CATEGORIES = {
            R.string.category_diatomic_nonmetals,
            R.string.category_noble_gases,
            R.string.category_alkali_metals,
            R.string.category_alkaline_earth_metals,
            R.string.category_metalloids,
            R.string.category_polyatomic_nonmetals,
            R.string.category_other_metals,
            R.string.category_transition_metals,
            R.string.category_lanthanides,
            R.string.category_actinides,
            R.string.category_unknown
    };

    private Context mContext;
    private Typeface mTypeface;
    private Property[] mProperties = new Property[0];

    private class Property {
        String mName = "", mValue;

        Property(int name) {
            mName = mContext.getString(name);
        }

        Property(int name, String value) {
            this(name);

            mValue = formatProperty(mContext, value);
        }

        Property(int name, int value) {
            this(name, String.valueOf(value));
        }

        String getName() {
            return mName;
        }

        String getValue() {
            return mValue;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnCreateContextMenuListener {
        TextView mName, mValue;

        public ViewHolder(View itemView) {
            super(itemView);

            mName = (TextView) itemView.findViewById(R.id.property_name);
            mValue = (TextView) itemView.findViewById(R.id.property_value);

            if (mValue != null) {
                mValue.setTypeface(mTypeface);

                itemView.setOnClickListener(this);
                itemView.setOnCreateContextMenuListener(this);
            }
        }

        public void setName(String name) {
            mName.setText(name);
        }

        public void setValue(String value) {
            mValue.setText(value);
        }

        @Override
        public void onClick(View view) {
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                                        ContextMenu.ContextMenuInfo menuInfo) {
        }
    }

    public PropertiesAdapter(Context context, ElementProperties properties) {
        mContext = context;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        mProperties = new Property[]{
                new Property(R.string.properties_header_general),
                new Property(R.string.property_symbol, properties.getSymbol()),
                new Property(R.string.property_atomic_number, properties.getNumber()),
                new Property(R.string.property_weight, properties.getStandardAtomicWeight()),
                new Property(R.string.property_group, properties.getGroup()),
                new Property(R.string.property_period, properties.getPeriod()),
                new Property(R.string.property_block, properties.getBlock()),
                new Property(R.string.property_category, mContext.getString(
                        CATEGORIES[properties.getCategory()])),
                new Property(R.string.property_electron_configuration,
                        properties.getElectronConfiguration()),
                new Property(R.string.property_electrons_per_shell,
                        properties.getElectronsPerShell()),
                new Property(R.string.properties_header_physical),
                new Property(R.string.property_appearance, properties.getAppearance()),
                new Property(R.string.property_phase, properties.getPhase()),
                new Property(R.string.property_density, properties.getDensity()),
                new Property(R.string.property_liquid_density_at_mp,
                        properties.getLiquidDensityAtMeltingPoint()),
                new Property(R.string.property_liquid_density_at_bp,
                        properties.getLiquidDensityAtBoilingPoint()),
                new Property(R.string.property_melting_point, properties.getMeltingPoint()),
                new Property(R.string.property_sublimation_point,
                        properties.getSublimationPoint()),
                new Property(R.string.property_boiling_point, properties.getBoilingPoint()),
                new Property(R.string.property_triple_point, properties.getTriplePoint()),
                new Property(R.string.property_critical_point, properties.getCriticalPoint()),
                new Property(R.string.property_heat_of_fusion, properties.getHeatOfFusion()),
                new Property(R.string.property_heat_of_vaporization,
                        properties.getHeatOfVaporization()),
                new Property(R.string.property_molar_heat_capacity,
                        properties.getMolarHeatCapacity()),
                new Property(R.string.properties_header_atomic),
                new Property(R.string.property_oxidation_states,
                        properties.getOxidationStates()),
                new Property(R.string.property_electronegativity,
                        properties.getElectronegativity()),
                new Property(R.string.property_molar_ionization_energies,
                        properties.getMolarIonizationEnergies()),
                new Property(R.string.property_atomic_radius, properties.getAtomicRadius()),
                new Property(R.string.property_covalent_radius,
                        properties.getCovalentRadius()),
                new Property(R.string.property_van_der_waals_radius,
                        properties.getVanDerWaalsRadius()),
                new Property(R.string.properties_header_miscellanea),
                new Property(R.string.property_crystal_structure,
                        properties.getCrystalStructure()),
                new Property(R.string.property_magnetic_ordering,
                        properties.getMagneticOrdering()),
                new Property(R.string.property_thermal_conductivity,
                        properties.getThermalConductivity()),
                new Property(R.string.property_thermal_expansion,
                        properties.getThermalExpansion()),
                new Property(R.string.property_thermal_diffusivity,
                        properties.getThermalDiffusivity()),
                new Property(R.string.property_electrical_resistivity,
                        properties.getElectricalResistivity()),
                new Property(R.string.property_band_gap, properties.getBandGap()),
                new Property(R.string.property_curie_point, properties.getCuriePoint()),
                new Property(R.string.property_tensile_strength,
                        properties.getTensileStrength()),
                new Property(R.string.property_speed_of_sound, properties.getSpeedOfSound()),
                new Property(R.string.property_poisson_ratio, properties.getPoissonRatio()),
                new Property(R.string.property_youngs_modulus, properties.getYoungsModulus()),
                new Property(R.string.property_shear_modulus, properties.getShearModulus()),
                new Property(R.string.property_bulk_modulus, properties.getBulkModulus()),
                new Property(R.string.property_mohs_hardness, properties.getMohsHardness()),
                new Property(R.string.property_vickers_hardness,
                        properties.getVickersHardness()),
                new Property(R.string.property_brinell_hardness,
                        properties.getBrinellHardness()),
                new Property(R.string.property_cas_number, properties.getCasNumber())
        };
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(viewType == VIEW_TYPE_HEADER
                ? R.layout.properties_list_header : R.layout.properties_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Property property = mProperties[position];

        ((ViewHolder) holder).setName(property.getName());

        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            ((ViewHolder) holder).setValue(property.getValue());
        }
    }

    @Override
    public int getItemCount() {
        return mProperties.length;
    }

    @Override
    public int getItemViewType(int position) {
        return mProperties[position].getValue() == null ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    public static String formatProperty(Context context, String property) {
        switch (property) {
            case "":
                return context.getString(R.string.property_value_unknown);

            case "-":
                return context.getString(R.string.property_value_none);
        }

        return property;
    }
}
