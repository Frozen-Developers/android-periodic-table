package com.frozendevs.periodictable.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ElementProperties extends TableElementItem {

    private String block, electronConfiguration, wikipediaLink, appearance, phase, density,
            liquidDensityAtMeltingPoint, liquidDensityAtBoilingPoint, meltingPoint,
            sublimationPoint, boilingPoint, triplePoint, criticalPoint, heatOfFusion,
            heatOfVaporization, molarHeatCapacity, oxidationStates, electronegativity,
            molarIonizationEnergies, atomicRadius, covalentRadius, vanDerWaalsRadius,
            crystalStructure, magneticOrdering, thermalConductivity, thermalExpansion,
            speedOfSound, youngsModulus, shearModulus, bulkModulus, mohsHardness, brinellHardness,
            electronsPerShell, thermalDiffusivity, electricalResistivity, bandGap, curiePoint,
            tensileStrength, poissonRatio, vickersHardness, casNumber, imageUrl;
    private Isotope[] isotopes;

    public static final Parcelable.Creator<ElementProperties> CREATOR
            = new Parcelable.Creator<ElementProperties>() {
        public ElementProperties createFromParcel(Parcel in) {
            return new ElementProperties(in);
        }

        public ElementProperties[] newArray(int size) {
            return new ElementProperties[size];
        }
    };

    protected ElementProperties(Parcel in) {
        super(in);

        block = in.readString();
        electronConfiguration = in.readString();
        wikipediaLink = in.readString();
        appearance = in.readString();
        phase = in.readString();
        density = in.readString();
        liquidDensityAtMeltingPoint = in.readString();
        liquidDensityAtBoilingPoint = in.readString();
        meltingPoint = in.readString();
        sublimationPoint = in.readString();
        boilingPoint = in.readString();
        triplePoint = in.readString();
        criticalPoint = in.readString();
        heatOfFusion = in.readString();
        heatOfVaporization = in.readString();
        molarHeatCapacity = in.readString();
        oxidationStates = in.readString();
        electronegativity = in.readString();
        molarIonizationEnergies = in.readString();
        atomicRadius = in.readString();
        covalentRadius = in.readString();
        vanDerWaalsRadius = in.readString();
        crystalStructure = in.readString();
        magneticOrdering = in.readString();
        thermalConductivity = in.readString();
        thermalExpansion = in.readString();
        speedOfSound = in.readString();
        youngsModulus = in.readString();
        shearModulus = in.readString();
        bulkModulus = in.readString();
        mohsHardness = in.readString();
        brinellHardness = in.readString();
        electronsPerShell = in.readString();
        thermalDiffusivity = in.readString();
        electricalResistivity = in.readString();
        bandGap = in.readString();
        curiePoint = in.readString();
        tensileStrength = in.readString();
        poissonRatio = in.readString();
        vickersHardness = in.readString();
        casNumber = in.readString();
        imageUrl = in.readString();
        isotopes = (Isotope[]) in.readParcelableArray(Isotope.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(block);
        dest.writeString(electronConfiguration);
        dest.writeString(wikipediaLink);
        dest.writeString(appearance);
        dest.writeString(phase);
        dest.writeString(density);
        dest.writeString(liquidDensityAtMeltingPoint);
        dest.writeString(liquidDensityAtBoilingPoint);
        dest.writeString(meltingPoint);
        dest.writeString(sublimationPoint);
        dest.writeString(boilingPoint);
        dest.writeString(triplePoint);
        dest.writeString(criticalPoint);
        dest.writeString(heatOfFusion);
        dest.writeString(heatOfVaporization);
        dest.writeString(molarHeatCapacity);
        dest.writeString(oxidationStates);
        dest.writeString(electronegativity);
        dest.writeString(molarIonizationEnergies);
        dest.writeString(atomicRadius);
        dest.writeString(covalentRadius);
        dest.writeString(vanDerWaalsRadius);
        dest.writeString(crystalStructure);
        dest.writeString(magneticOrdering);
        dest.writeString(thermalConductivity);
        dest.writeString(thermalExpansion);
        dest.writeString(speedOfSound);
        dest.writeString(youngsModulus);
        dest.writeString(shearModulus);
        dest.writeString(bulkModulus);
        dest.writeString(mohsHardness);
        dest.writeString(brinellHardness);
        dest.writeString(electronsPerShell);
        dest.writeString(thermalDiffusivity);
        dest.writeString(electricalResistivity);
        dest.writeString(bandGap);
        dest.writeString(curiePoint);
        dest.writeString(tensileStrength);
        dest.writeString(poissonRatio);
        dest.writeString(vickersHardness);
        dest.writeString(casNumber);
        dest.writeString(imageUrl);
        dest.writeParcelableArray(isotopes, 0);
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

    public String getImageUrl() {
        return imageUrl;
    }
}
