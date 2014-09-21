package com.frozendevs.periodictable.helper;

import android.content.Context;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.model.ElementProperties;
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

    private String mInput;
    private JsonArray mJsonArray;
    private Gson mGson;

    private static Database mInstance;

    private Database(Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.database);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            mInput = new String(outputStream.toByteArray());

            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mJsonArray = new JsonParser().parse(mInput).getAsJsonArray();
        mGson = new Gson();
    }

    public static synchronized Database getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new Database(context);
        }

        return mInstance;
    }

    public ElementListItem[] getElementListItems() {
        List<ElementListItem> itemsList = new ArrayList<ElementListItem>(Arrays.asList(
                mGson.fromJson(mInput, ElementListItem[].class)));

        Collections.sort(itemsList, new Comparator<ElementListItem>() {
            @Override
            public int compare(ElementListItem lhs, ElementListItem rhs) {
                return lhs.getNumber() - rhs.getNumber();
            }
        });

        return itemsList.toArray(new ElementListItem[itemsList.size()]);
    }

    public TableItem[] getTableItems() {
        return mGson.fromJson(mInput, TableItem[].class);
    }

    public ElementProperties getElementProperties(int element) {
        for(JsonElement jsonElement : mJsonArray) {
            JsonObject object = jsonElement.getAsJsonObject();

            if(object.get("number").getAsInt() == element) {
                return mGson.fromJson(object, ElementProperties.class);
            }
        }

        return null;
    }
}
