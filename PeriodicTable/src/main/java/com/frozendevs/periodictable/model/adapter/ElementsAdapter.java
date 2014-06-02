package com.frozendevs.periodictable.model.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.ElementListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ElementsAdapter extends BaseAdapter {

    private List<ElementListItem> mElements, mFilteredElements;
    private Activity mActivity;

    private class LoadItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mElements = Database.getInstance(mActivity).getElementListItems();
            mFilteredElements = new ArrayList<ElementListItem>(mElements);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyDataSetChanged();

            ListView listView = (ListView)mActivity.findViewById(R.id.elements_list);
            listView.setEmptyView(mActivity.findViewById(R.id.empty_elements_list));
        }
    }

    public ElementsAdapter(Activity activity) {
        mActivity = activity;

        mElements = new ArrayList<ElementListItem>();
        mFilteredElements = new ArrayList<ElementListItem>();

        new LoadItems().execute();
    }

    @Override
    public int getCount() {
        return mFilteredElements.size();
    }

    @Override
    public Object getItem(int position) {
        return mFilteredElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ElementListItem element = mFilteredElements.get(position);

        View view = LayoutInflater.from(mActivity).inflate(R.layout.elements_list_item, parent, false);

        TextView symbol = (TextView)view.findViewById(R.id.element_symbol);
        symbol.setText(element.getSymbol());

        TextView atomicNumber = (TextView)view.findViewById(R.id.element_number);
        atomicNumber.setText(String.valueOf(element.getAtomicNumber()));

        TextView name = (TextView)view.findViewById(R.id.element_name);
        name.setText(element.getName());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, PropertiesActivity.class);
                intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER, element.getAtomicNumber());
                mActivity.startActivity(intent);
            }
        });

        return view;
    }

    public void filter(String filter) {
        List<ElementListItem> items = new ArrayList<ElementListItem>();

        for(ElementListItem element : mElements) {
            if(element.getSymbol().equalsIgnoreCase(filter) ||
                    String.valueOf(element.getAtomicNumber()).equals(filter)) {
                items.add(element);
                break;
            }
        }

        Locale locale = mActivity.getResources().getConfiguration().locale;

        if(items.isEmpty()) {
            for(ElementListItem element : mElements) {
                if(element.getName().toLowerCase(locale).contains(filter.toLowerCase(locale)))
                    items.add(element);
            }
        }

        mFilteredElements = items;

        notifyDataSetChanged();
    }

    public void clearFilter() {
        mFilteredElements = new ArrayList<ElementListItem>(mElements);

        notifyDataSetChanged();
    }
}
