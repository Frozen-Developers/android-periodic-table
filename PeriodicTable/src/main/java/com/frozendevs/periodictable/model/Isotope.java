package com.frozendevs.periodictable.model;

public class Isotope {

    private String symbol, halfLife, spin, abundance;
    private String[] decayModes, daughterIsotopes;

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

    public String[] getDecayModes() {
        return decayModes;
    }

    public String[] getDaughterIsotopes() {
        return daughterIsotopes;
    }
}
