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

public class TableAdapter extends DynamicItemsAdapter<TableItem> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_TEXT = 1;

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
    public TableItem getItem(int position) {
        for (TableItem item : getAllItems()) {
            if (((item.getPeriod() - 1) * 18) + item.getGroup() - 1 == position)
                return item;
            else if (position >= 128 && position <= 142) {
                if (item.getNumber() + 71 == position)
                    return item;
            } else if (position >= 146 && position <= 160) {
                if (item.getNumber() + 57 == position)
                    return item;
            }
        }

        return null;
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
                        ((TextView)convertView).setText(R.string.category_actinide);
                        break;

                    case 5:
                        ((TextView)convertView).setText(R.string.category_alkali_metal);
                        break;

                    case 6:
                        ((TextView)convertView).setText(R.string.category_alkaline_earth_metal);
                        break;

                    case 7:
                        ((TextView)convertView).setText(R.string.category_diatomic_nonmetal);
                        break;

                    case 8:
                        ((TextView)convertView).setText(R.string.category_lanthanide);
                        break;

                    case 9:
                        ((TextView)convertView).setText(R.string.category_metalloid);
                        break;

                    case 22:
                        ((TextView)convertView).setText(R.string.category_noble_gas);
                        break;

                    case 23:
                        ((TextView)convertView).setText(R.string.category_polyatomic_nonmetal);
                        break;

                    case 24:
                        ((TextView)convertView).setText(R.string.category_other_metal);
                        break;

                    case 25:
                        ((TextView)convertView).setText(R.string.category_transition_metal);
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

    public int getItemPosition(TableItem item) {
        for (TableItem tableItem : getAllItems()) {
            if(tableItem.equals(item)) {
                if (item.getNumber() + 71 >= 128 && item.getNumber() + 71 <= 142) {
                    return item.getNumber() + 71;
                }
                else if (item.getNumber() + 57 >= 146 && item.getNumber() + 57 <= 160) {
                    return item.getNumber() + 57;
                }
                else {
                    return ((item.getPeriod() - 1) * 18) + item.getGroup() - 1;
                }
            }
        }

        return -1;
    }

    private int getBackgroundColor(int position) {
        int color = R.color.category_unknown_bg;

        switch (position) {
            case 4:
            case 110:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
                color = R.color.category_actinides_bg;
                break;

            case 5:
            case 18:
            case 36:
            case 54:
            case 72:
            case 90:
            case 108:
                color = R.color.category_alkali_metals_bg;
                break;

            case 6:
            case 19:
            case 37:
            case 55:
            case 73:
            case 91:
            case 109:
                color = R.color.category_alkaline_earth_metals_bg;
                break;

            case 0:
            case 7:
            case 32:
            case 33:
            case 34:
            case 52:
            case 70:
            case 88:
                color = R.color.category_diatomic_nonmetals_bg;
                break;

            case 8:
            case 92:
            case 128:
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
                color = R.color.category_lanthanides_bg;
                break;

            case 9:
            case 30:
            case 49:
            case 67:
            case 68:
            case 86:
            case 87:
            case 106:
                color = R.color.category_metalloids_bg;
                break;

            case 22:
            case 17:
            case 35:
            case 53:
            case 71:
            case 89:
            case 107:
                color = R.color.category_noble_gases_bg;
                break;

            case 23:
            case 31:
            case 50:
            case 51:
            case 69:
                color = R.color.category_polyatomic_nonmetals_bg;
                break;

            case 24:
            case 48:
            case 66:
            case 84:
            case 85:
            case 102:
            case 103:
            case 104:
            case 105:
                color = R.color.category_other_metals_bg;
                break;

            case 25:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 98:
            case 99:
            case 100:
            case 101:
            case 111:
            case 112:
            case 113:
            case 114:
            case 115:
            case 119:
                color = R.color.category_transition_metals_bg;
                break;
        }

        return mContext.getResources().getColor(color);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 92 || position == 110 || (position >= 4 && position <= 9) ||
                (position >= 22 && position <= 26)) {
            return VIEW_TYPE_TEXT;
        }

        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
