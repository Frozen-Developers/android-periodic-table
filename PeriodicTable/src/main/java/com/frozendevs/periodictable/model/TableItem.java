package com.frozendevs.periodictable.model;

public class TableItem extends ElementListItem {

    private String weight;
    private int group, period, category;

    public int getGroup() {
        return group;
    }

    public int getPeriod() {
        return period;
    }

    public String getStandardAtomicWeight() {
        return weight;
    }

    public int getCategory() {
        return category;
    }
}
