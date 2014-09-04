package com.frozendevs.periodictable.model;

import java.io.Serializable;

public class Isotope implements Serializable {

    private String symbol, halfLife, spin, abundance, decayModes;

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
