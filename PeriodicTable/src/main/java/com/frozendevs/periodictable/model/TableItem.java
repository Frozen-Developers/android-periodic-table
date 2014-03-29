package com.frozendevs.periodictable.model;

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
}
