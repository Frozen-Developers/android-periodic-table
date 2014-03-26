package com.frozendevs.periodictable.model;

import java.io.Serializable;

public class ElementProperties extends TableItem implements Serializable {

    private String block, electronConfiguration, wikipediaLink, appearance, phase, density,
            liquidDensityAtMeltingPoint, liquidDensityAtBoilingPoint, meltingPoint, boilingPoint,
            triplePoint, criticalPoint, heatOfFusion, heatOfVaporization, molarHeatCapacity,
            oxidationStates, electronegativity, ionizationEnergies, atomicRadius, covalentRadius,
            vanDerWaalsRadius, crystalStructure, magneticOrdering, thermalConductivity,
            thermalExpansion;
    private Isotope[] isotopes;

    public class Isotope implements Serializable {

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

    public Isotope[] getIsotopes() {
        return isotopes;
    }

    public String getIonizationEnergies() {
        return ionizationEnergies;
    }

    public String getAtomicRadius() {
        return atomicRadius;
    }

    public String getCovalentRadius() {
        return covalentRadius;
    }

    public String getVanDerWaalsRadius() {
        return vanDerWaalsRadius;
    }

    public String getCrystalStructure() {
        return crystalStructure;
    }

    public String getMagneticOrdering() {
        return magneticOrdering;
    }

    public String getThermalConductivity() {
        return thermalConductivity;
    }

    public String getThermalExpansion() {
        return thermalExpansion;
    }
}
