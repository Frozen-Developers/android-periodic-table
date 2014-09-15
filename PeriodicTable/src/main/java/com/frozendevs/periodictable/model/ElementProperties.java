package com.frozendevs.periodictable.model;

import java.io.Serializable;

public class ElementProperties extends TableItem implements Serializable {

    private String block, electronConfiguration, wikipediaLink, appearance, phase, density,
            liquidDensityAtMeltingPoint, liquidDensityAtBoilingPoint, meltingPoint, sublimationPoint,
            boilingPoint, triplePoint, criticalPoint, heatOfFusion, heatOfVaporization, molarHeatCapacity,
            oxidationStates, electronegativity, molarIonizationEnergies, atomicRadius, covalentRadius,
            vanDerWaalsRadius, crystalStructure, magneticOrdering, thermalConductivity,
            thermalExpansion, speedOfSound, youngsModulus, shearModulus, bulkModulus, mohsHardness,
            brinellHardness, electronsPerShell, thermalDiffusivity, electricalResistivity,
            bandGap, curiePoint, tensileStrength, poissonRatio, vickersHardness, casNumber;
    private Isotope[] isotopes;

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

    public String getSublimationPoint() {
        return sublimationPoint;
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

    public String getMolarIonizationEnergies() {
        return molarIonizationEnergies;
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

    public String getThermalDiffusivity() {
        return thermalDiffusivity;
    }

    public String getSpeedOfSound() {
        return speedOfSound;
    }

    public String getYoungsModulus() {
        return youngsModulus;
    }

    public String getShearModulus() {
        return shearModulus;
    }

    public String getBulkModulus() {
        return bulkModulus;
    }

    public String getMohsHardness() {
        return mohsHardness;
    }

    public String getBrinellHardness() {
        return brinellHardness;
    }

    public String getElectronsPerShell() {
        return electronsPerShell;
    }

    public String getElectricalResistivity() {
        return electricalResistivity;
    }

    public String getBandGap() {
        return bandGap;
    }

    public String getCuriePoint() {
        return curiePoint;
    }

    public String getTensileStrength() {
        return tensileStrength;
    }

    public String getPoissonRatio() {
        return poissonRatio;
    }

    public String getVickersHardness() {
        return vickersHardness;
    }

    public String getCasNumber() {
        return casNumber;
    }
}
