package com.frozendevs.periodictable.model.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.helper.OnClickOnElement;
import com.frozendevs.periodictable.model.ElementListItem;

import java.util.ArrayList;
import java.util.List;

public class ElementsAdapter extends BaseAdapter {

    private List<ElementListItem> elements, filteredElements;
    private Activity activity;

    private class LoadItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            elements = Database.getElementListItems(activity);
            filteredElements = new ArrayList<ElementListItem>(elements);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyDataSetChanged();

            ListView listView = (ListView)activity.findViewById(R.id.elementList);
            listView.setEmptyView(activity.findViewById(R.id.emptyElementList));
        }
    }

    public ElementsAdapter(Activity activity) {
        this.activity = activity;

        elements = new ArrayList<ElementListItem>();
        filteredElements = new ArrayList<ElementListItem>();

        new LoadItems().execute();
    }

    @Override
    public int getCount() {
        return filteredElements.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ElementListItem element = filteredElements.get(position);

        View view = LayoutInflater.from(activity).inflate(R.layout.elements_list_item, parent, false);

        TextView symbol = (TextView)view.findViewById(R.id.symbol);
        symbol.setText(element.getSymbol());

        TextView atomicNumber = (TextView)view.findViewById(R.id.atomicNumber);
        atomicNumber.setText(String.valueOf(element.getAtomicNumber()));

        TextView name = (TextView)view.findViewById(R.id.name);
        name.setText(element.getName());

        view.setOnClickListener(new OnClickOnElement(activity, element.getAtomicNumber()));

        return view;
    }

    public void filter(String filter) {
        List<ElementListItem> items = new ArrayList<ElementListItem>();

        for(ElementListItem element : elements) {
            if(element.getSymbol().toLowerCase().equals(filter.toLowerCase()) ||
                    String.valueOf(element.getAtomicNumber()).equals(filter)) {
                items.add(element);
                break;
            }
        }

        if(items.isEmpty()) {
            for(ElementListItem element : elements) {
                if(element.getName().toLowerCase().contains(filter.toLowerCase()))
                    items.add(element);
            }
        }

        filteredElements = items;

        notifyDataSetChanged();
    }

    public void clearFilter() {
        filteredElements = new ArrayList<ElementListItem>(elements);

        notifyDataSetChanged();
    }
}
