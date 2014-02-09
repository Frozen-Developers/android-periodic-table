package com.frozendevs.periodictable.model;

public class Isotope {

    private String symbol, halfLife, spin, abundance, decayModes, daughterIsotopes;

    public Isotope(String symbol, String halfLife, String decayModes, String daughterIsotopes,
                   String spin, String abundance) {
        this.symbol = symbol;
        this.halfLife = halfLife;
        this.decayModes = decayModes;
        this.daughterIsotopes = daughterIsotopes;
        this.spin = spin;
        this.abundance = abundance;
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

    public String getDaughterIsotopes() {
        return daughterIsotopes;
    }
}
