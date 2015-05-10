package com.frozendevs.periodictable.model.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class DynamicAdapter<T> extends BaseAdapter {

    private T[] mItems;

    @Override
    public int getCount() {
        return mItems != null ? mItems.length : 0;
    }

    @Override
    public T getItem(int position) {
        return mItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    public void setItems(T[] items) {
        mItems = items.clone();
    }
}
