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
import com.frozendevs.periodictable.model.TableElementItem;
import com.frozendevs.periodictable.view.RecyclerView;

public class PropertiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_ITEM = 1;
    public static final int VIEW_TYPE_SUMMARY = 2;

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
    private TableElementItem mTableElementItem;
    private TableAdapter mTableAdapter;

    private class Property {
        String mName = "";
        Object mValue;

        Property(int name) {
            mName = mContext.getString(name);
        }

        Property(int name, Object value) {
            this(name);

            if (value instanceof String) {
                mValue = parseString((String) value);
            } else if (value instanceof String[]) {
                String[] values = (String[]) value;

                for (int i = 0; i < values.length; i++) {
                    values[i] = parseString(values[i]);
                }

                mValue = values;
            } else {
                mValue = value;
            }
        }

        String parseString(String value) {
            if (value.equals("")) {
                return mContext.getString(R.string.property_value_unknown);
            } else if (value.equals("-")) {
                return mContext.getString(R.string.property_value_none);
            }

            return value;
        }

        String getName() {
            return mName;
        }

        Object getValue() {
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

    public class SummaryViewHolder extends RecyclerView.ViewHolder {
        private TextView mConfiguration, mShells, mElectronegativity, mOxidationStates;

        public SummaryViewHolder(View itemView) {
            super(itemView);

            View tileView = itemView.findViewById(R.id.tile_view);
            mTableAdapter.getView(mTableElementItem, tileView, (ViewGroup) itemView);
            tileView.setClickable(false);
            tileView.setDuplicateParentStateEnabled(true);

            mConfiguration = (TextView) itemView.findViewById(R.id.element_electron_configuration);
            mConfiguration.setTypeface(mTypeface);
            mShells = (TextView) itemView.findViewById(R.id.element_electrons_per_shell);
            mShells.setTypeface(mTypeface);
            mElectronegativity = (TextView) itemView.findViewById(R.id.element_electronegativity);
            mElectronegativity.setTypeface(mTypeface);
            mOxidationStates = (TextView) itemView.findViewById(R.id.element_oxidation_states);
            mOxidationStates.setTypeface(mTypeface);
        }

        public void setElectronConfiguration(String configuration) {
            mConfiguration.setText(configuration);
        }

        public void setElectronsPerShell(String electronsPerShell) {
            mShells.setText(electronsPerShell);
        }

        public void setElectronegativity(String electronegativity) {
            mElectronegativity.setText(electronegativity);
        }

        public void setOxidationStates(String oxidationStates) {
            mOxidationStates.setText(oxidationStates);
        }
    }

    public PropertiesAdapter(Context context, ElementProperties properties) {
        mContext = context;

        mTableElementItem = properties;

        mTableAdapter = new TableAdapter(context);

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        mProperties = new Property[]{
                new Property(R.string.properties_header_summary),
                new Property(R.string.properties_header_summary, new String[]{
                        properties.getElectronConfiguration(), properties.getElectronsPerShell(),
                        properties.getElectronegativity(), properties.getOxidationStates()
                }),
                new Property(R.string.properties_header_general),
                new Property(R.string.property_symbol, properties.getSymbol()),
                new Property(R.string.property_atomic_number,
                        String.valueOf(properties.getNumber())),
                new Property(R.string.property_weight, properties.getStandardAtomicWeight()),
                new Property(R.string.property_group, String.valueOf(properties.getGroup())),
                new Property(R.string.property_period,
                        String.valueOf(properties.getPeriod())),
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
        if (viewType == VIEW_TYPE_SUMMARY) {
            return new SummaryViewHolder(LayoutInflater.from(mContext).inflate(
                    R.layout.properties_summary_item, parent, false));
        }

        return new ViewHolder(LayoutInflater.from(mContext).inflate(viewType == VIEW_TYPE_HEADER
                ? R.layout.properties_list_header : R.layout.properties_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Property property = mProperties[position];

        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
            case VIEW_TYPE_ITEM:
                ((ViewHolder) holder).setName(property.getName());

                if (getItemViewType(position) == VIEW_TYPE_ITEM) {
                    ((ViewHolder) holder).setValue((String) property.getValue());
                }
                break;

            case VIEW_TYPE_SUMMARY:
                String[] properties = (String[]) property.getValue();

                ((SummaryViewHolder) holder).setElectronConfiguration(properties[0]);
                ((SummaryViewHolder) holder).setElectronsPerShell(properties[1]);
                ((SummaryViewHolder) holder).setElectronegativity(properties[2]);
                ((SummaryViewHolder) holder).setOxidationStates(properties[3]);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mProperties.length;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 1) return VIEW_TYPE_SUMMARY;
        return mProperties[position].getValue() == null ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }
}
