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

public class ElementsAdapter extends DynamicAdapter<ElementListItem> {

    private ElementListItem[] mAllItems;
    private Context mContext;

    private class ViewHolder {
        TextView symbol, atomicNumber, name;
        int number;
    }

    public ElementsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ElementListItem element = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.elements_list_item,
                    parent, false);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.symbol = (TextView)convertView.findViewById(R.id.element_symbol);
            viewHolder.atomicNumber = (TextView)convertView.findViewById(R.id.element_number);
            viewHolder.name = (TextView)convertView.findViewById(R.id.element_name);

            convertView.setTag(viewHolder);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PropertiesActivity.class);
                    intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER,
                            ((ViewHolder)v.getTag()).number);
                    mContext.startActivity(intent);
                }
            });
        }

        ViewHolder viewHolder = (ViewHolder)convertView.getTag();

        viewHolder.symbol.setText(element.getSymbol());
        viewHolder.atomicNumber.setText(String.valueOf(element.getNumber()));
        viewHolder.name.setText(element.getName());
        viewHolder.number = element.getNumber();

        return convertView;
    }

    public void filter(String filter) {
        if (mAllItems != null) {
            List<ElementListItem> filteredItems = new ArrayList<ElementListItem>();

            for (ElementListItem element : mAllItems) {
                if (element.getSymbol().equalsIgnoreCase(filter) ||
                        String.valueOf(element.getNumber()).equals(filter)) {
                    filteredItems.add(element);
                    break;
                }
            }

            Locale locale = mContext.getResources().getConfiguration().locale;

            if (filteredItems.isEmpty()) {
                for (ElementListItem element : mAllItems) {
                    if (element.getName().toLowerCase(locale).contains(filter.toLowerCase(locale))) {
                        filteredItems.add(element);
                    }
                }
            }

            super.setItems(filteredItems.toArray(new ElementListItem[filteredItems.size()]));
        }
    }

    public void clearFilter() {
        super.setItems(mAllItems);
    }

    @Override
    public void setItems(ElementListItem[] items) {
        mAllItems = (ElementListItem[]) items.clone();

        super.setItems(items);
    }
}
