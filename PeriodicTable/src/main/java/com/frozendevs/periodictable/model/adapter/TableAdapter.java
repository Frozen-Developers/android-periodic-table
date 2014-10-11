package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.TableItem;

public class TableAdapter extends DynamicAdapter<TableItem> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_TEXT = 1;

    private static final int GROUPS_COUNT = 18;

    private Context mContext;
    private Typeface mTypeface;

    private class ViewHolder {
        TextView symbol, number, name, weight;
        int integerNumber;
    }

    public TableAdapter(Context context) {
        mContext = context;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_TEXT:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.table_text,
                            parent, false);
                }

                convertView.setBackgroundColor(getBackgroundColor(position));

                switch (position) {
                    case 4:
                        ((TextView)convertView).setText(R.string.category_actinides);
                        break;

                    case 5:
                        ((TextView)convertView).setText(R.string.category_alkali_metals);
                        break;

                    case 6:
                        ((TextView)convertView).setText(R.string.category_alkaline_earth_metals);
                        break;

                    case 7:
                        ((TextView)convertView).setText(R.string.category_diatomic_nonmetals);
                        break;

                    case 8:
                        ((TextView)convertView).setText(R.string.category_lanthanides);
                        break;

                    case 9:
                        ((TextView)convertView).setText(R.string.category_metalloids);
                        break;

                    case 22:
                        ((TextView)convertView).setText(R.string.category_noble_gases);
                        break;

                    case 23:
                        ((TextView)convertView).setText(R.string.category_polyatomic_nonmetals);
                        break;

                    case 24:
                        ((TextView)convertView).setText(R.string.category_other_metals);
                        break;

                    case 25:
                        ((TextView)convertView).setText(R.string.category_transition_metals);
                        break;

                    case 26:
                        ((TextView)convertView).setText(R.string.category_unknown);
                        break;

                    case 92:
                        ((TextView)convertView).setText("57 - 71");
                        break;

                    case 110:
                        ((TextView)convertView).setText("89 - 103");
                        break;
                }

                return convertView;

            case VIEW_TYPE_ITEM:
                TableItem item = getItem(position);

                if(item != null) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(mContext).inflate(R.layout.table_item,
                                parent, false);
                    }

                    ViewHolder viewHolder = (ViewHolder) convertView.getTag();

                    if (viewHolder == null) {
                        viewHolder = new ViewHolder();

                        viewHolder.symbol = (TextView) convertView.findViewById(R.id.element_symbol);
                        viewHolder.symbol.setTypeface(mTypeface);
                        viewHolder.number = (TextView) convertView.findViewById(R.id.element_number);
                        viewHolder.number.setTypeface(mTypeface);
                        viewHolder.name = (TextView) convertView.findViewById(R.id.element_name);
                        viewHolder.name.setTypeface(mTypeface);
                        viewHolder.weight = (TextView) convertView.findViewById(R.id.element_weight);
                        viewHolder.weight.setTypeface(mTypeface);

                        convertView.setTag(viewHolder);

                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ViewHolder viewHolder = (ViewHolder) v.getTag();

                                if (viewHolder.integerNumber > 0) {
                                    Intent intent = new Intent(mContext, PropertiesActivity.class);
                                    intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER,
                                            viewHolder.integerNumber);
                                    mContext.startActivity(intent);
                                }
                            }
                        });
                    }

                    convertView.setBackgroundColor(getBackgroundColor(position));

                    viewHolder.symbol.setText(item.getSymbol());
                    viewHolder.number.setText(String.valueOf(item.getNumber()));
                    viewHolder.name.setTextSize(12f);
                    viewHolder.name.setText(item.getName());
                    viewHolder.weight.setText(item.getStandardAtomicWeight());
                    viewHolder.integerNumber = item.getNumber();

                    convertView.setClickable(true);

                    return convertView;
                }
        }

        return null;
    }

    private int getBackgroundColor(int position) {
        int color = R.color.category_unknown_bg;

        TableItem item = getItem(position);

        if(item != null) {
            switch (item.getCategory()) {
                case 0:
                    color = R.color.category_diatomic_nonmetals_bg;
                    break;

                case 1:
                    color = R.color.category_noble_gases_bg;
                    break;

                case 2:
                    color = R.color.category_alkali_metals_bg;
                    break;

                case 3:
                    color = R.color.category_alkaline_earth_metals_bg;
                    break;

                case 4:
                    color = R.color.category_metalloids_bg;
                    break;

                case 5:
                    color = R.color.category_polyatomic_nonmetals_bg;
                    break;

                case 6:
                    color = R.color.category_other_metals_bg;
                    break;

                case 7:
                    color = R.color.category_transition_metals_bg;
                    break;

                case 8:
                    color = R.color.category_lanthanides_bg;
                    break;

                case 9:
                    color = R.color.category_actinides_bg;
                    break;
            }
        }
        else {
            switch (position) {
                case 4:
                case 110:
                    color = R.color.category_actinides_bg;
                    break;

                case 5:
                    color = R.color.category_alkali_metals_bg;
                    break;

                case 6:
                    color = R.color.category_alkaline_earth_metals_bg;
                    break;

                case 7:
                    color = R.color.category_diatomic_nonmetals_bg;
                    break;

                case 8:
                case 92:
                    color = R.color.category_lanthanides_bg;
                    break;

                case 9:
                    color = R.color.category_metalloids_bg;
                    break;

                case 22:
                    color = R.color.category_noble_gases_bg;
                    break;

                case 23:
                    color = R.color.category_polyatomic_nonmetals_bg;
                    break;

                case 24:
                    color = R.color.category_other_metals_bg;
                    break;

                case 25:
                    color = R.color.category_transition_metals_bg;
                    break;
            }
        }

        return mContext.getResources().getColor(color);
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position) == null && (position == 92 || position == 110 ||
                (position >= 4 && position <= 9) || (position >= 22 && position <= 26))) {
            return VIEW_TYPE_TEXT;
        }

        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public void setItems(TableItem... items) {
        int periods = 0;

        for(TableItem item : items) {
            periods = Math.max(item.getPeriod(), periods);
        }

        TableItem[] sortedItems = new TableItem[GROUPS_COUNT * (periods + 2)];

        for(TableItem item : items) {
            if (item.getNumber() >= 57 && item.getNumber() <= 71) {
                sortedItems[(GROUPS_COUNT * periods) + 2 + item.getNumber() - 57] = item;
            }
            else if (item.getNumber() >= 89 && item.getNumber() <= 103) {
                sortedItems[(GROUPS_COUNT * periods) + 20 + item.getNumber() - 89] = item;
            }
            else {
                sortedItems[((item.getPeriod() - 1) * GROUPS_COUNT) + item.getGroup() - 1] = item;
            }
        }

        super.setItems(sortedItems);
    }
}
