package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.ElementListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ElementsAdapter extends DynamicItemsAdapter<ElementListItem> {

    private List<ElementListItem> mAllItems = new ArrayList<ElementListItem>();

    private Context mContext;

    public ElementsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ElementListItem element = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.elements_list_item, parent, false);
        }

        TextView symbol = (TextView)convertView.findViewById(R.id.element_symbol);
        symbol.setText(element.getSymbol());

        TextView atomicNumber = (TextView)convertView.findViewById(R.id.element_number);
        atomicNumber.setText(String.valueOf(element.getAtomicNumber()));

        TextView name = (TextView)convertView.findViewById(R.id.element_name);
        name.setText(element.getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PropertiesActivity.class);
                intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER, element.getAtomicNumber());
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }

    public void filter(String filter) {
        List<ElementListItem> filteredItems = new ArrayList<ElementListItem>();

        for(ElementListItem element : mAllItems) {
            if(element.getSymbol().equalsIgnoreCase(filter) ||
                    String.valueOf(element.getAtomicNumber()).equals(filter)) {
                filteredItems.add(element);
                break;
            }
        }

        Locale locale = mContext.getResources().getConfiguration().locale;

        if(filteredItems.isEmpty()) {
            for(ElementListItem element : mAllItems) {
                if(element.getName().toLowerCase(locale).contains(filter.toLowerCase(locale))) {
                    filteredItems.add(element);
                }
            }
        }

        super.setItems(filteredItems);
    }

    public void clearFilter() {
        super.setItems(new ArrayList<ElementListItem>(mAllItems));
    }

    @Override
    public void setItems(List<ElementListItem> items) {
        mAllItems = new ArrayList<ElementListItem>(items);

        super.setItems(items);
    }
}
