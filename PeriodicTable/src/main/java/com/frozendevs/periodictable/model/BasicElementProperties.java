package com.frozendevs.periodictable.model;

public class BasicElementProperties {

    private String name, wikiLink;

    public BasicElementProperties(String name, String wikiLink) {
        this.name = name;
        this.wikiLink = wikiLink;
    }

    public String getName() {
        return name;
    }

    public String getWikiLink() {
        return wikiLink;
    }
}
