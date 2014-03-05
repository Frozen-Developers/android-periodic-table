package com.frozendevs.periodictable.model;

public class ElementProperties extends TableItem {

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

    private String block, electronConfiguration, wikipediaLink, appearance, phase, density,
            liquidDensityAtMeltingPoint, liquidDensityAtBoilingPoint, meltingPoint, boilingPoint,
            triplePoint, criticalPoint, heatOfFusion, heatOfVaporization, molarHeatCapacity,
            oxidationStates, electronegativity;
    private Isotope[] isotopes;

    public Isotope[] getIsotopes() {
        return isotopes;
    }

    public String getBlock() {
        return block;
    }

    public String getElectronConfiguration() {
        return electronConfiguration;
    }

    public String getWikipediaLink() {
        return wikipediaLink;
    }

    public String getAppearance() {
        return appearance;
    }

    public String getPhase() {
        return phase;
    }

    public String getDensity() {
        return density;
    }

    public String getLiquidDensityAtMeltingPoint() {
        return liquidDensityAtMeltingPoint;
    }

    public String getLiquidDensityAtBoilingPoint() {
        return liquidDensityAtBoilingPoint;
    }

    public String getMeltingPoint() {
        return meltingPoint;
    }

    public String getBoilingPoint() {
        return boilingPoint;
    }

    public String getTriplePoint() {
        return triplePoint;
    }

    public String getCriticalPoint() {
        return criticalPoint;
    }

    public String getHeatOfFusion() {
        return heatOfFusion;
    }

    public String getHeatOfVaporization() {
        return heatOfVaporization;
    }

    public String getMolarHeatCapacity() {
        return molarHeatCapacity;
    }

    public String getOxidationStates() {
        return oxidationStates;
    }

    public String getElectronegativity() {
        return electronegativity;
    }
}
