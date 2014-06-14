package com.frozendevs.periodictable.model;

import android.content.Context;

import com.frozendevs.periodictable.R;

public class TableItem extends ElementListItem {

    private String weight, category;
    private int group, period;

    public int getGroup() {
        return group;
    }

    public int getPeriod() {
        return period;
    }

    public String getStandardAtomicWeight() {
        return weight;
    }

    public String getCategory() {
        return category;
    }

    public int getBackgroundColor(Context context) {
        int color = 0;

        if (getCategory().equalsIgnoreCase("Actinide"))
            color = R.color.actinide_bg;
        else if (getCategory().equalsIgnoreCase("Alkali metal"))
            color = R.color.alkali_metal_bg;
        else if (getCategory().equalsIgnoreCase("Alkaline earth metal") ||
                getCategory().equalsIgnoreCase("Alkaline earth metals"))
            color = R.color.alkaline_earth_metal_bg;
        else if (getCategory().equalsIgnoreCase("Diatomic nonmetal"))
            color = R.color.diatomic_nonmetal_bg;
        else if (getCategory().equalsIgnoreCase("Lanthanide"))
            color = R.color.lanthanide_bg;
        else if (getCategory().equalsIgnoreCase("Metalloid"))
            color = R.color.metalloid_bg;
        else if (getCategory().equalsIgnoreCase("Noble gas") ||
                getCategory().equalsIgnoreCase("Noble gases"))
            color = R.color.noble_gas_bg;
        else if (getCategory().equalsIgnoreCase("Polyatomic nonmetal"))
            color = R.color.polyatomic_nonmetal_bg;
        else if (getCategory().equalsIgnoreCase("Other metal") ||
                getCategory().equalsIgnoreCase("Poor metal"))
            color = R.color.other_metal_bg;
        else if (getCategory().equalsIgnoreCase("Transition metal"))
            color = R.color.transition_metal_bg;

        return context.getResources().getColor(color);
    }
}
