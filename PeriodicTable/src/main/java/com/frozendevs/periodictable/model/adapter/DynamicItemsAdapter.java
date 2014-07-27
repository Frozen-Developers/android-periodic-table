package com.frozendevs.periodictable.model.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class DynamicItemsAdapter<T> extends BaseAdapter {

    private List<T> mItems = new ArrayList<T>();

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    public void setItems(List<T> items) {
        mItems = new ArrayList<T>(items);

        notifyDataSetChanged();
    }

    public List<T> getAllItems() {
        return new ArrayList<T>(mItems);
    }
}
