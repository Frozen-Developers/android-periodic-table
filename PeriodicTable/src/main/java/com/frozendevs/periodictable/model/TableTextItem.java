package com.frozendevs.periodictable.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TableTextItem implements TableItem, Parcelable {
    private String text;
    private int category;

    public static final Creator<TableTextItem> CREATOR = new Creator<TableTextItem>() {
        @Override
        public TableTextItem createFromParcel(Parcel in) {
            return new TableTextItem(in);
        }

        @Override
        public TableTextItem[] newArray(int size) {
            return new TableTextItem[size];
        }
    };

    public TableTextItem(String text, int category) {
        this.text = text;
        this.category = category;
    }

    private TableTextItem(Parcel in) {
        text = in.readString();
        category = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeInt(category);
    }

    public String getText() {
        return text;
    }

    @Override
    public int getCategory() {
        return category;
    }
}
