package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.TableItem;

public class TableAdapter extends BaseAdapter {

    private TableItem[] mItems = new TableItem[0];

    private Context mContext;

    private class LoadItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mItems = Database.getInstance(mContext).getTableItems();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyDataSetChanged();
        }
    }

    public TableAdapter(Context context) {
        mContext = context;

        new LoadItems().execute();
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public TableItem getItem(int position) {
        for (TableItem item : mItems) {
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
        switch (position) {
            case 92:
            case 110:
                if(convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.table_item, parent, false);
                }

                convertView.setBackgroundColor(mContext.getResources().getColor(position == 92 ?
                        R.color.lanthanide_bg : R.color.actinide_bg));

                TextView elementName = (TextView)convertView.findViewById(R.id.element_name);
                elementName.setTextSize(14f);
                elementName.setText(position == 92 ? "57 - 71" : "89 - 103");

                return convertView;

            default:
                final TableItem item = getItem(position);

                if(item != null) {
                    if(convertView == null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.table_item, parent, false);
                    }

                    convertView.setBackgroundColor(item.getBackgroundColor(mContext));

                    ((TextView)convertView.findViewById(R.id.element_symbol)).setText(item.getSymbol());
                    ((TextView)convertView.findViewById(R.id.element_number)).setText(String.valueOf(item.getAtomicNumber()));
                    ((TextView)convertView.findViewById(R.id.element_name)).setText(item.getName());
                    ((TextView)convertView.findViewById(R.id.element_weight)).setText(item.getStandardAtomicWeight());

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, PropertiesActivity.class);
                            intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER, item.getAtomicNumber());
                            mContext.startActivity(intent);
                        }
                    });

                    return convertView;
                }
                break;
        }

        return null;
    }
}
