package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.TableItem;

public class TableAdapter extends BaseAdapter {

    private TableItem[] items = new TableItem[0];

    private class LoadItems extends AsyncTask<Context, Void, Void> {

        @Override
        protected Void doInBackground(Context... params) {
            items = Database.getInstance(params[0]).getTableItems();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyDataSetChanged();
        }
    }

    public TableAdapter(Context context) {
        new LoadItems().execute(context);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public TableItem getItem(int position) {
        for (TableItem item : items) {
            if (((item.getPeriod() - 1) * 18) + item.getGroup() - 1 == position)
                return item;
            else if (position >= 128 && position <= 142) {
                if (item.getAtomicNumber() + 71 == position)
                    return item;
            } else if (position >= 146 && position <= 160) {
                if (item.getAtomicNumber() + 57 == position)
                    return item;
            }
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
