package com.frozendevs.periodic.table.model;

public class ElementDetails extends TableItem {

    private String wikiLink;
    private Isotope[] isotopes;

    public ElementDetails(String name, String symbol, int atomicNumber, String weight, int group,
                          int period, String category, String wikiLink, Isotope[] isotopes) {
        super(name, symbol, atomicNumber, weight, group, period, category);

        this.wikiLink = wikiLink;
        this.isotopes = isotopes;
    }

    public String getWikiLink() {
        return wikiLink;
    }

    public Isotope[] getIsotopes() {
        return isotopes;
    }
}
