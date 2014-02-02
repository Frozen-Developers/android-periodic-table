package com.frozendevs.periodictable.model;

public class ElementListItem {

    private String name, symbol;
    private int atomicNumber;

    public ElementListItem(String name, String symbol, int atomicNumber) {
        this.name = name;
        this.symbol = symbol;
        this.atomicNumber = atomicNumber;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getAtomicNumber() {
        return atomicNumber;
    }
}
