package com.frozendevs.periodictable.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ElementListItem implements Parcelable {

    private String name, symbol;
    private int number;

    public static final Parcelable.Creator<ElementListItem> CREATOR
            = new Parcelable.Creator<ElementListItem>() {
        public ElementListItem createFromParcel(Parcel in) {
            return new ElementListItem(in);
        }

        public ElementListItem[] newArray(int size) {
            return new ElementListItem[size];
        }
    };

    protected ElementListItem(Parcel in) {
        name = in.readString();
        symbol = in.readString();
        number = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(symbol);
        dest.writeInt(number);
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getNumber() {
        return number;
    }
}
