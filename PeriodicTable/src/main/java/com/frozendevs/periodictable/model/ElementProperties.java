package com.frozendevs.periodictable.model;

public class ElementProperties extends TableItem {

    private String block, electronConfiguration, wikiLink, appearance;

    public ElementProperties(String name, String symbol, int atomicNumber, String weight, int group,
                             int period, String block, String category, String electronConfiguration,
                             String wikiLink, String appearance) {
        super(name, symbol, atomicNumber, weight, group, period, category);

        this.block = block;
        this.electronConfiguration = electronConfiguration;
        this.wikiLink = wikiLink;
        this.appearance = appearance;
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
}
