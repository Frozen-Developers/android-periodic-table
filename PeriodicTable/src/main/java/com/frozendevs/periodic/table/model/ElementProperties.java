package com.frozendevs.periodic.table.model;

public class ElementProperties extends TableItem {

    private String wikiLink;

    public ElementProperties(String name, String symbol, int atomicNumber, String weight, int group,
                             int period, String category, String wikiLink) {
        super(name, symbol, atomicNumber, weight, group, period, category);

        this.wikiLink = wikiLink;
    }

    public String getWikiLink() {
        return wikiLink;
    }
}
