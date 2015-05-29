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
import com.frozendevs.periodictable.view.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ElementsAdapter extends RecyclerView.Adapter<ElementsAdapter.ViewHolder> {

    private ElementListItem[] mItems;
    private List<ElementListItem> mFilteredItems = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        TextView mSymbolView, mNumberView, mNameView;
        int mNumber;

        public ViewHolder(View itemView) {
            super(itemView);

            mSymbolView = (TextView) itemView.findViewById(R.id.element_symbol);
            mNumberView = (TextView) itemView.findViewById(R.id.element_number);
            mNameView = (TextView) itemView.findViewById(R.id.element_name);

            itemView.setOnClickListener(this);
        }

        public void setName(String name) {
            mNameView.setText(name);
        }

        public void setNumber(int number) {
            mNumberView.setText(Integer.toString(mNumber = number));
        }

        public void setSymbol(String symbol) {
            mSymbolView.setText(symbol);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), PropertiesActivity.class);
            intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER, mNumber);

            view.getContext().startActivity(intent);
        }
    }

    public ElementsAdapter() {
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.elements_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ElementListItem item = mFilteredItems.get(position);

        holder.setName(item.getName());
        holder.setNumber(item.getNumber());
        holder.setSymbol(item.getSymbol());
    }

    @Override
    public int getItemCount() {
        return mFilteredItems.size();
    }

    @Override
    public long getItemId(int i) {
        return mFilteredItems.get(i).hashCode();
    }

    public void filter(Context context, String filter) {
        if (mItems != null) {
            List<ElementListItem> filteredItems = new ArrayList<>();

            Locale locale = context.getResources().getConfiguration().locale;

            int nextPos = 0;
            for (ElementListItem element : mItems) {
                if (element.getSymbol().toLowerCase(locale).equalsIgnoreCase(filter)) {
                    filteredItems.add(0, element);

                    nextPos += 1;
                } else if (element.getSymbol().toLowerCase(locale).startsWith(filter.toLowerCase(
                        locale)) || String.valueOf(element.getNumber()).startsWith(filter)) {
                    filteredItems.add(nextPos, element);

                    nextPos += 1;
                } else if (element.getName().toLowerCase(locale).startsWith(
                        filter.toLowerCase(locale))) {
                    filteredItems.add(element);
                }
            }

            mFilteredItems = new ArrayList<>(filteredItems);

            notifyDataSetChanged();
        }
    }

    public void clearFilter() {
        mFilteredItems = new ArrayList<>(Arrays.asList(mItems));

        notifyDataSetChanged();
    }

    public void setItems(ElementListItem[] items) {
        mItems = items.clone();

        mFilteredItems = new ArrayList<>(Arrays.asList(mItems));

        notifyDataSetChanged();
    }
}
