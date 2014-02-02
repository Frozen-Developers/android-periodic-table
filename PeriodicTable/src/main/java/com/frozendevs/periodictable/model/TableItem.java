package com.frozendevs.periodictable.model;

public class TableItem extends ElementListItem {

    private String weight, category;
    private int group, period;

    public TableItem(String name, String symbol, int atomicNumber, String weight, int group,
                     int period, String category) {
        super(name, symbol, atomicNumber);

        this.weight = weight;
        this.group = group;
        this.period = period;
        this.category = category;
    }

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
