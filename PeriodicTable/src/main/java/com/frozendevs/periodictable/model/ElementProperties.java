package com.frozendevs.periodictable.model;

public class ElementProperties extends TableItem {

    private String block, wikiLink;

    public ElementProperties(String name, String symbol, int atomicNumber, String weight, int group,
                             int period, String block, String category, String wikiLink) {
        super(name, symbol, atomicNumber, weight, group, period, category);

        this.block = block;
        this.wikiLink = wikiLink;
    }

    public String getBlock() {
        return block;
    }

    public String getWikiLink() {
        return wikiLink;
    }
}
