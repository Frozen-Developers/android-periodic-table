package com.frozendevs.periodictable.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TableElementItem extends ElementListItem implements TableItem {

    private String weight;
    private int group, period, category;

    public static final Parcelable.Creator<TableElementItem> CREATOR
            = new Parcelable.Creator<TableElementItem>() {
        public TableElementItem createFromParcel(Parcel in) {
            return new TableElementItem(in);
        }

        public TableElementItem[] newArray(int size) {
            return new TableElementItem[size];
        }
    };

    protected TableElementItem(Parcel in) {
        super(in);

        group = in.readInt();
        period = in.readInt();
        category = in.readInt();
        weight = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeInt(group);
        dest.writeInt(period);
        dest.writeInt(category);
        dest.writeString(weight);
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

    @Override
    public int getCategory() {
        return category;
    }
}
