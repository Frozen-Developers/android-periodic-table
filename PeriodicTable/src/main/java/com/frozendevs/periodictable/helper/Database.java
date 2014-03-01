package com.frozendevs.periodictable.helper;

import android.content.Context;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.BasicElementProperties;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.model.Isotope;
import com.frozendevs.periodictable.model.TableItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Database {

    private Reader databaseReader;
    private Gson gson;

    public Database(Context context) {
        databaseReader = new InputStreamReader(context.getResources().openRawResource(R.raw.elements));

        gson = new Gson();
    }

    public List<ElementListItem> getElementListItems() {
        List<ElementListItem> itemsList = new ArrayList<ElementListItem>(Arrays.asList(
            gson.fromJson(databaseReader, ElementListItem[].class)));

        Collections.sort(itemsList, new Comparator<ElementListItem>() {
            @Override
            public int compare(ElementListItem lhs, ElementListItem rhs) {
                return lhs.getAtomicNumber() - rhs.getAtomicNumber();
            }
        });

        return itemsList;
    }

    public BasicElementProperties getBasicElementProperties(int element) {
        JsonArray jsonArray = new JsonParser().parse(databaseReader).getAsJsonArray();

        for(int i = 0; i < jsonArray.size(); i++) {
            JsonObject object = jsonArray.get(i).getAsJsonObject();

            if(object.get("atomicNumber").getAsInt() == element)
                return gson.fromJson(object, BasicElementProperties.class);
        }

        return null;
    }

    public TableItem[] getTableItems() {
        return gson.fromJson(databaseReader, TableItem[].class);
    }

    public ElementProperties getElementProperties(int element) {
        JsonArray jsonArray = new JsonParser().parse(databaseReader).getAsJsonArray();

        for(int i = 0; i < jsonArray.size(); i++) {
            JsonObject object = jsonArray.get(i).getAsJsonObject();

            if(object.get("atomicNumber").getAsInt() == element)
                return gson.fromJson(object, ElementProperties.class);
        }

        return null;
    }

    public Isotope[] getIsotopes(int element) {
        JsonArray jsonArray = new JsonParser().parse(databaseReader).getAsJsonArray();

        for(int i = 0; i < jsonArray.size(); i++) {
            JsonObject object = jsonArray.get(i).getAsJsonObject();

            if(object.get("atomicNumber").getAsInt() == element)
                return gson.fromJson(object.get("isotopes").getAsJsonArray(), Isotope[].class);
        }

        return null;
    }
}
