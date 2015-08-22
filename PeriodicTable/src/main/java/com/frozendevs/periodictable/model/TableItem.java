package com.frozendevs.periodictable.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TableItem extends ElementListItem {

    private String weight;
    private int group, period, category;

    public static final Parcelable.Creator<TableItem> CREATOR
            = new Parcelable.Creator<TableItem>() {
        public TableItem createFromParcel(Parcel in) {
            return new TableItem(in);
        }

        public TableItem[] newArray(int size) {
            return new TableItem[size];
        }
    };

    protected TableItem(Parcel in) {
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

    public int getCategory() {
        return category;
    }
}
