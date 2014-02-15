package com.frozendevs.periodictable.model;

public class ElementProperties extends TableItem {

    private String block, electronConfiguration, wikiLink, appearance, phase, density,
            liquidDensityAtMeltingPoint, liquidDensityAtBoilingPoint;

    public ElementProperties(String name, String symbol, int atomicNumber, String weight, int group,
                             int period, String block, String category, String electronConfiguration,
                             String wikiLink, String appearance, String phase, String density,
                             String liquidDensityAtMeltingPoint, String liquidDensityAtBoilingPoint) {
        super(name, symbol, atomicNumber, weight, group, period, category);

        this.block = block;
        this.electronConfiguration = electronConfiguration;
        this.wikiLink = wikiLink;
        this.appearance = appearance;
        this.phase = phase;
        this.density = density;
        this.liquidDensityAtMeltingPoint = liquidDensityAtMeltingPoint;
        this.liquidDensityAtBoilingPoint = liquidDensityAtBoilingPoint;
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
}
