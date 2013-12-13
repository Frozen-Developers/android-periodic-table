package com.frozendevs.periodic.table.model.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.helper.Database;
import com.frozendevs.periodic.table.model.TableItem;

import java.util.ArrayList;
import java.util.List;

public class TableAdapter extends BaseAdapter {

    private Activity activity;
    private List<TableItem> items = null;

    private class LoadItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            items = Database.getTableItems(activity);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyDataSetChanged();
        }
    }

    public TableAdapter(Activity activity) {
        this.activity = activity;

        new LoadItems().execute();
    }

    @Override
    public int getCount() {
        return items == null ? 0 : 162;
    }

    @Override
    public Object getItem(int position) {
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
        View view;

        TableItem item = (TableItem) getItem(position);

        if (item != null) {
            view = LayoutInflater.from(activity).inflate(R.layout.table_item, parent, false);

            int color = 0;

            if (item.getCategory().equals("actinide"))
                color = R.color.actinide_bg;
            else if (item.getCategory().equals("alkali metal"))
                color = R.color.alkali_metal_bg;
            else if (item.getCategory().equals("alkaline earth metal") ||
                    item.getCategory().equals("alkaline earth metals"))
                color = R.color.alkaline_earth_metal_bg;
            else if (item.getCategory().equals("diatomic nonmetal"))
                color = R.color.diatomic_nonmetal_bg;
            else if (item.getCategory().equals("lanthanide"))
                color = R.color.lanthanide_bg;
            else if (item.getCategory().equals("metalloid"))
                color = R.color.metalloid_bg;
            else if (item.getCategory().equals("noble gas") ||
                    item.getCategory().equals("noble gases"))
                color = R.color.noble_gas_bg;
            else if (item.getCategory().equals("polyatomic nonmetal"))
                color = R.color.polyatomic_nonmetal_bg;
            else if (item.getCategory().equals("poor metal"))
                color = R.color.poor_metal_bg;
            else if (item.getCategory().equals("transition metal"))
                color = R.color.transition_metal_bg;

            view.setBackgroundColor(activity.getResources().getColor(color));

            TextView symbol = (TextView) view.findViewById(R.id.symbol);
            symbol.setText(item.getSymbol());

            TextView number = (TextView) view.findViewById(R.id.number);
            number.setText(String.valueOf(item.getAtomicNumber()));

            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(item.getName());

            TextView weight = (TextView) view.findViewById(R.id.weight);
            weight.setText(item.getStandardAtomicWeight());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } else {
            view = LayoutInflater.from(activity).inflate(R.layout.table_item_text, parent, false);

            if (position == 92) {
                ((TextView) view).setText("57 - 71");
                view.setBackgroundColor(activity.getResources().getColor(R.color.lanthanide_bg));
            } else if (position == 110) {
                ((TextView) view).setText("89 - 103");
                view.setBackgroundColor(activity.getResources().getColor(R.color.actinide_bg));
            } else
                view.setVisibility(View.INVISIBLE);
        }

        return view;
    }
}
