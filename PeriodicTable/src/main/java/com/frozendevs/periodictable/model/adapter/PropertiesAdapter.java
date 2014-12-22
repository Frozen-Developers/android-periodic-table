package com.frozendevs.periodictable.model.adapter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.TableItem;

public class PropertiesAdapter extends BaseAdapter implements
        ListView.OnItemClickListener {

    private static enum ViewType {
        HEADER,
        ITEM,
        SUMMARY
    }

    private Context mContext;
    private Typeface mTypeface;
    private TableAdapter mTableAdapter;
    private Property[] mProperties = new Property[0];
    private StateListDrawable mGroupIndicator;
    private TableItem mTableItem;

    private class Property<T> {
        String mName = "";
        T mValue;
        ViewType mType = ViewType.ITEM;
        boolean mExpanded = false;

        Property(int name, T value) {
            mName = mContext.getString(name);

            if (value == null) {
                mType = ViewType.HEADER;
            } else if (value instanceof String) {
                mValue = (T) parseString((String) value);
            } else if (value instanceof String[]) {
                String[] values = (String[]) value;
                String[] mValues = new String[values.length];

                for (int i = 0; i < values.length; i++) {
                    mValues[i] = parseString(values[i]);
                }

                mValue = (T) mValues;
            } else {
                mValue = value;
            }
        }

        Property(int name, T value, ViewType type) {
            this(name, value);

            mType = type;
        }

        String parseString(String value) {
            if (value.equals("")) {
                return mContext.getString(R.string.property_value_unknown);
            } else if (value.equals("-")) {
                return mContext.getString(R.string.property_value_none);
            }

            return value;
        }

        boolean isExpanded() {
            return mExpanded;
        }

        String getName() {
            return mName;
        }

        T getValue() {
            return mValue;
        }

        ViewType getType() {
            return mType;
        }

        void setExpanded(boolean expanded) {
            mExpanded = expanded;
        }
    }

    private class ViewHolder {
        ImageView indicator;
        TextView name, value;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PropertiesAdapter(Context context, ElementProperties properties) {
        mContext = context;

        mTableItem = properties;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");

        Resources.Theme theme = mContext.getTheme();
        TypedValue typedValue = new TypedValue();

        theme.resolveAttribute(android.R.attr.expandableListViewStyle, typedValue, true);

        TypedArray typedArray = theme.obtainStyledAttributes(typedValue.resourceId,
                new int[]{android.R.attr.groupIndicator, R.attr.colorControlHighlight});

        mGroupIndicator = (StateListDrawable) typedArray.getDrawable(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mGroupIndicator.setTint(typedArray.getColor(1,
                    R.color.abc_secondary_text_material_dark));
        }

        typedArray.recycle();

        mTableAdapter = new TableAdapter(context);

        mProperties = new Property[]{
                new Property<String>(R.string.properties_header_summary, null),
                new Property<String[]>(R.string.properties_header_summary, new String[]{
                        properties.getElectronConfiguration(), properties.getElectronsPerShell(),
                        properties.getElectronegativity(), properties.getOxidationStates()
                }, ViewType.SUMMARY),
                new Property<String>(R.string.properties_header_general, null),
                new Property<String>(R.string.property_symbol, properties.getSymbol()),
                new Property<String>(R.string.property_atomic_number,
                        String.valueOf(properties.getNumber())),
                new Property<String>(R.string.property_weight, properties.getStandardAtomicWeight()),
                new Property<String>(R.string.property_group, String.valueOf(properties.getGroup())),
                new Property<String>(R.string.property_period,
                        String.valueOf(properties.getPeriod())),
                new Property<String>(R.string.property_block, properties.getBlock()),
                new Property<String>(R.string.property_category, mContext.getString(
                        new int[]{
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
                        }[properties.getCategory()])),
                new Property<String>(R.string.property_electron_configuration,
                        properties.getElectronConfiguration()),
                new Property<String>(R.string.property_electrons_per_shell,
                        properties.getElectronsPerShell()),
                new Property<String>(R.string.properties_header_physical, null),
                new Property<String>(R.string.property_appearance, properties.getAppearance()),
                new Property<String>(R.string.property_phase, properties.getPhase()),
                new Property<String>(R.string.property_density, properties.getDensity()),
                new Property<String>(R.string.property_liquid_density_at_mp,
                        properties.getLiquidDensityAtMeltingPoint()),
                new Property<String>(R.string.property_liquid_density_at_bp,
                        properties.getLiquidDensityAtBoilingPoint()),
                new Property<String>(R.string.property_melting_point, properties.getMeltingPoint()),
                new Property<String>(R.string.property_sublimation_point,
                        properties.getSublimationPoint()),
                new Property<String>(R.string.property_boiling_point, properties.getBoilingPoint()),
                new Property<String>(R.string.property_triple_point, properties.getTriplePoint()),
                new Property<String>(R.string.property_critical_point, properties.getCriticalPoint()),
                new Property<String>(R.string.property_heat_of_fusion, properties.getHeatOfFusion()),
                new Property<String>(R.string.property_heat_of_vaporization,
                        properties.getHeatOfVaporization()),
                new Property<String>(R.string.property_molar_heat_capacity,
                        properties.getMolarHeatCapacity()),
                new Property<String>(R.string.properties_header_atomic, null),
                new Property<String>(R.string.property_oxidation_states,
                        properties.getOxidationStates()),
                new Property<String>(R.string.property_electronegativity,
                        properties.getElectronegativity()),
                new Property<String>(R.string.property_molar_ionization_energies,
                        properties.getMolarIonizationEnergies()),
                new Property<String>(R.string.property_atomic_radius, properties.getAtomicRadius()),
                new Property<String>(R.string.property_covalent_radius,
                        properties.getCovalentRadius()),
                new Property<String>(R.string.property_van_der_waals_radius,
                        properties.getVanDerWaalsRadius()),
                new Property<String>(R.string.properties_header_miscellanea, null),
                new Property<String>(R.string.property_crystal_structure,
                        properties.getCrystalStructure()),
                new Property<String>(R.string.property_magnetic_ordering,
                        properties.getMagneticOrdering()),
                new Property<String>(R.string.property_thermal_conductivity,
                        properties.getThermalConductivity()),
                new Property<String>(R.string.property_thermal_expansion,
                        properties.getThermalExpansion()),
                new Property<String>(R.string.property_thermal_diffusivity,
                        properties.getThermalDiffusivity()),
                new Property<String>(R.string.property_electrical_resistivity,
                        properties.getElectricalResistivity()),
                new Property<String>(R.string.property_band_gap, properties.getBandGap()),
                new Property<String>(R.string.property_curie_point, properties.getCuriePoint()),
                new Property<String>(R.string.property_tensile_strength,
                        properties.getTensileStrength()),
                new Property<String>(R.string.property_speed_of_sound, properties.getSpeedOfSound()),
                new Property<String>(R.string.property_poisson_ratio, properties.getPoissonRatio()),
                new Property<String>(R.string.property_youngs_modulus, properties.getYoungsModulus()),
                new Property<String>(R.string.property_shear_modulus, properties.getShearModulus()),
                new Property<String>(R.string.property_bulk_modulus, properties.getBulkModulus()),
                new Property<String>(R.string.property_mohs_hardness, properties.getMohsHardness()),
                new Property<String>(R.string.property_vickers_hardness,
                        properties.getVickersHardness()),
                new Property<String>(R.string.property_brinell_hardness,
                        properties.getBrinellHardness()),
                new Property<String>(R.string.property_cas_number, properties.getCasNumber())
        };
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).getType() != ViewType.HEADER;
    }

    @Override
    public int getCount() {
        return mProperties.length;
    }

    @Override
    public Property getItem(int position) {
        return mProperties[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Property property = getItem(position);

        switch (property.getType()) {
            case SUMMARY:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.properties_summary_item, parent, false);

                    View tileView = convertView.findViewById(R.id.tile_view);
                    mTableAdapter.getView(mTableItem, tileView, (ViewGroup) convertView);
                    tileView.setClickable(false);
                    tileView.setDuplicateParentStateEnabled(true);

                    String[] properties = (String[]) property.getValue();

                    TextView configuration = (TextView) convertView.findViewById(
                            R.id.element_electron_configuration);
                    configuration.setText(properties[0]);
                    configuration.setTypeface(mTypeface);

                    TextView shells = (TextView) convertView.findViewById(
                            R.id.element_electrons_per_shell);
                    shells.setText(properties[1]);
                    shells.setTypeface(mTypeface);

                    TextView electronegativity = (TextView) convertView.findViewById(
                            R.id.element_electronegativity);
                    electronegativity.setText(properties[2]);
                    electronegativity.setTypeface(mTypeface);

                    TextView oxidationStates = (TextView) convertView.findViewById(
                            R.id.element_oxidation_states);
                    oxidationStates.setText(properties[3]);
                    oxidationStates.setTypeface(mTypeface);
                }
                break;

            case ITEM:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.properties_list_item, parent, false);
                }

                ViewHolder viewHolder = (ViewHolder) convertView.getTag();

                if (viewHolder == null) {
                    viewHolder = new ViewHolder();

                    viewHolder.indicator = (ImageView) convertView.findViewById(R.id.group_indicator);
                    viewHolder.name = (TextView) convertView.findViewById(R.id.property_name);
                    viewHolder.value = (TextView) convertView.findViewById(R.id.property_value);
                    viewHolder.value.setTypeface(mTypeface);

                    convertView.setTag(viewHolder);
                }

                viewHolder.name.setText(property.getName());

                String[] lines = ((String) property.getValue()).split("\\n");
                if (lines.length > 3) {
                    mGroupIndicator.setState(property.isExpanded() ?
                            new int[]{android.R.attr.state_expanded} : null);
                    viewHolder.indicator.setImageDrawable(mGroupIndicator.getCurrent());
                    viewHolder.indicator.setVisibility(View.VISIBLE);

                    viewHolder.value.setText((String) property.getValue());
                    if (property.isExpanded()) {
                        viewHolder.value.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        viewHolder.value.setMaxLines(1);
                    }
                } else {
                    viewHolder.indicator.setVisibility(View.GONE);

                    viewHolder.value.setText((String) property.getValue());
                }
                break;

            case HEADER:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.properties_list_header, parent, false);
                }

                ((TextView) convertView).setText(property.getName());
                break;
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Property property = getItem(position);

        switch (property.getType()) {
            case SUMMARY:
                AlertDialog legendDialog = new AlertDialog.Builder(mContext).create();
                legendDialog.setTitle(R.string.context_title_legend);

                View summaryView = LayoutInflater.from(mContext).inflate(
                        R.layout.properties_summary_item, parent, false);

                int padding = mContext.getResources().getDimensionPixelOffset(
                        R.dimen.listPreferredItemPaddingTop);
                summaryView.setPadding(padding, padding, padding, padding);

                View tileView = summaryView.findViewById(R.id.tile_view);
                tileView.setBackgroundColor(mTableAdapter.getBackgroundColor(mTableItem));
                tileView.setClickable(false);

                legendDialog.setView(summaryView);

                legendDialog.show();
                break;

            case ITEM:
                property.setExpanded(!property.isExpanded());

                notifyDataSetChanged();
                break;
        }
    }
}
