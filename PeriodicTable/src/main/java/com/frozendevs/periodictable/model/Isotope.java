package com.frozendevs.periodictable.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Isotope implements Parcelable {

    private String symbol, halfLife, spin, abundance, decayModes;

    public static final Parcelable.Creator<Isotope> CREATOR
            = new Parcelable.Creator<Isotope>() {
        public Isotope createFromParcel(Parcel in) {
            return new Isotope(in);
        }

        public Isotope[] newArray(int size) {
            return new Isotope[size];
        }
    };

    protected Isotope(Parcel in) {
        symbol = in.readString();
        halfLife = in.readString();
        spin = in.readString();
        abundance = in.readString();
        decayModes = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(symbol);
        dest.writeString(halfLife);
        dest.writeString(spin);
        dest.writeString(abundance);
        dest.writeString(decayModes);
    }

    public String getSymbol() {
        return symbol;
    }

    public String getHalfLife() {
        return halfLife;
    }

    public String getSpin() {
        return spin;
    }

    public String getAbundance() {
        return abundance;
    }

    public String getDecayModes() {
        return decayModes;
    }
}
