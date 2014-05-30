package com.frozendevs.periodictable.helper;

import android.content.Context;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.model.TableItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Database {

    private String input;
    private JsonArray jsonArray;
    private Gson gson;

    private static Database instance;

    private Database(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.database);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            input = new String(outputStream.toByteArray());

            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonArray = new JsonParser().parse(input).getAsJsonArray();
        gson = new Gson();
    }

    public static synchronized Database getInstance(Context context) {
        if(instance == null)
            instance = new Database(context);

        return instance;
    }

    public List<ElementListItem> getElementListItems() {
        List<ElementListItem> itemsList = new ArrayList<ElementListItem>(Arrays.asList(
            gson.fromJson(input, ElementListItem[].class)));

        Collections.sort(itemsList, new Comparator<ElementListItem>() {
            @Override
            public int compare(ElementListItem lhs, ElementListItem rhs) {
                return lhs.getAtomicNumber() - rhs.getAtomicNumber();
            }
        });

        return itemsList;
    }

    public TableItem[] getTableItems() {
        return gson.fromJson(input, TableItem[].class);
    }

    public ElementProperties getElementProperties(int element) {
        for(JsonElement jsonElement : jsonArray) {
            JsonObject object = jsonElement.getAsJsonObject();

            if(object.get("atomicNumber").getAsInt() == element)
                return gson.fromJson(object, ElementProperties.class);
        }

        return null;
    }
}
