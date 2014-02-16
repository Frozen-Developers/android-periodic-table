package com.frozendevs.periodictable.model;

public class ElementProperties extends TableItem {

    private String block, electronConfiguration, wikiLink, appearance, phase, density,
            liquidDensityAtMeltingPoint, liquidDensityAtBoilingPoint, meltingPoint, boilingPoint,
            triplePoint, criticalPoint, heatOfFusion, heatOfVaporization, molarHeatCapacacity;

    public ElementProperties(String name, String symbol, int atomicNumber, String weight, int group,
                             int period, String block, String category, String electronConfiguration,
                             String wikiLink, String appearance, String phase, String density,
                             String liquidDensityAtMeltingPoint, String liquidDensityAtBoilingPoint,
                             String meltingPoint, String boilingPoint, String triplePoint,
                             String criticalPoint, String heatOfFusion, String heatOfVaporization,
                             String molarHeatCapacacity) {
        super(name, symbol, atomicNumber, weight, group, period, category);

        this.block = block;
        this.electronConfiguration = electronConfiguration;
        this.wikiLink = wikiLink;
        this.appearance = appearance;
        this.phase = phase;
        this.density = density;
        this.liquidDensityAtMeltingPoint = liquidDensityAtMeltingPoint;
        this.liquidDensityAtBoilingPoint = liquidDensityAtBoilingPoint;
        this.meltingPoint = meltingPoint;
        this.boilingPoint = boilingPoint;
        this.triplePoint = triplePoint;
        this.criticalPoint = criticalPoint;
        this.heatOfFusion = heatOfFusion;
        this.heatOfVaporization = heatOfVaporization;
        this.molarHeatCapacacity = molarHeatCapacacity;
    }

    public String getBlock() {
        return block;
    }

    public String getElectronConfiguration() {
        return electronConfiguration;
    }

    public String getWikiLink() {
        return wikiLink;
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

    public String getMolarHeatCapacacity() {
        return molarHeatCapacacity;
    }
}
