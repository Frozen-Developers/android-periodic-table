package com.frozendevs.periodictable.helper;

import android.content.Context;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.TableItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Database {
    private final JsonArray mJsonArray;
    private final Gson mGson = new Gson();

    private static Database mInstance;

    private Database(Context context) {
        final StringBuilder json = new StringBuilder();

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            inputStream = context.getResources().openRawResource(R.raw.database);
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            for (String line; (line = bufferedReader.readLine()) != null; ) {
                json.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mJsonArray = new JsonParser().parse(json.toString()).getAsJsonArray();
    }

    public static synchronized Database getInstance(Context context) {
        if (mInstance == null) {
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
