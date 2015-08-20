package com.frozendevs.periodictable.helper;

import android.content.Context;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.TableItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Database {
    private final JsonArray mJsonArray;
    private final Gson mGson = new Gson();

    private static Database mInstance;

    private Database(Context context) {
        String input = "";

        try {
            final InputStream inputStream = context.getResources().openRawResource(R.raw.database);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

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

        mJsonArray = new JsonParser().parse(input).getAsJsonArray();
    }

    public static synchronized Database getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new Database(context);
        }

        return mInstance;
    }

    public ElementListItem[] getElementListItems() {
        final ElementListItem[] items = new ElementListItem[mJsonArray.size()];

        for (int i = 0; i < items.length; i++) {
            items[i] = mGson.fromJson(mJsonArray.get(i).getAsJsonObject(), ElementListItem.class);
        }

        return items;
    }

    public TableItem[] getTableItems() {
        final TableItem[] items = new TableItem[mJsonArray.size()];

        for (int i = 0; i < items.length; i++) {
            items[i] = mGson.fromJson(mJsonArray.get(i).getAsJsonObject(), TableItem.class);
        }

        return items;
    }

    public ElementProperties getElementProperties(int element) {
        return mGson.fromJson(mJsonArray.get(element - 1).getAsJsonObject(),
                ElementProperties.class);
    }
}
