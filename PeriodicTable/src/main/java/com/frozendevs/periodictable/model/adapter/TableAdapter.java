package com.frozendevs.periodictable.model.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.TableItem;

public class TableAdapter extends DynamicAdapter<TableItem> {

    private static enum ViewType {
        ITEM,
        TEXT
    }

    private Context mContext;
    private Typeface mTypeface;
    private Bitmap[] mBitmaps;
    private int mGroupsCount;
    private int mPeriodsCount;

    private class ViewHolder {
        TextView symbol, number, name, weight;
    }

    public TableAdapter(Context context) {
        mContext = context;

        mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/NotoSans-Regular.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (ViewType.values()[getItemViewType(position)]) {
            case TEXT:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.table_text,
                            parent, false);
                }

                convertView.setBackgroundColor(getBackgroundColor(position));

                switch (position) {
                    case 4:
                        ((TextView) convertView).setText(R.string.category_actinides);
                        break;

                    case 5:
                        ((TextView) convertView).setText(R.string.category_alkali_metals);
                        break;

                    case 6:
                        ((TextView) convertView).setText(R.string.category_alkaline_earth_metals);
                        break;

                    case 7:
                        ((TextView) convertView).setText(R.string.category_diatomic_nonmetals);
                        break;

                    case 8:
                        ((TextView) convertView).setText(R.string.category_lanthanides);
                        break;

                    case 9:
                        ((TextView) convertView).setText(R.string.category_metalloids);
                        break;

                    case 22:
                        ((TextView) convertView).setText(R.string.category_noble_gases);
                        break;

                    case 23:
                        ((TextView) convertView).setText(R.string.category_polyatomic_nonmetals);
                        break;

                    case 24:
                        ((TextView) convertView).setText(R.string.category_other_metals);
                        break;

                    case 25:
                        ((TextView) convertView).setText(R.string.category_transition_metals);
                        break;

                    case 26:
                        ((TextView) convertView).setText(R.string.category_unknown);
                        break;

                    case 92:
                        ((TextView) convertView).setText("57 - 71");
                        break;

                    case 110:
                        ((TextView) convertView).setText("89 - 103");
                        break;
                }

                return convertView;

            case ITEM:
                TableItem item = getItem(position);

                if (item != null) {
                    return getView(item, convertView, parent);
                }
                break;
        }

        return null;
    }

    public View getView(TableItem item, View convertView, ViewGroup parent) {
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
        }

        convertView.setBackgroundColor(getBackgroundColor(item));

        viewHolder.symbol.setText(item.getSymbol());
        viewHolder.number.setText(String.valueOf(item.getNumber()));
        viewHolder.name.setTextSize(12f);
        viewHolder.name.setText(item.getName());
        viewHolder.weight.setText(item.getStandardAtomicWeight());

        return convertView;
    }

    public int getBackgroundColor(TableItem item) {
        return mContext.getResources().getColor(new int[]{
                R.color.category_diatomic_nonmetals_bg,
                R.color.category_noble_gases_bg,
                R.color.category_alkali_metals_bg,
                R.color.category_alkaline_earth_metals_bg,
                R.color.category_metalloids_bg,
                R.color.category_polyatomic_nonmetals_bg,
                R.color.category_other_metals_bg,
                R.color.category_transition_metals_bg,
                R.color.category_lanthanides_bg,
                R.color.category_actinides_bg,
                R.color.category_unknown_bg
        }[item.getCategory()]);
    }

    public int getBackgroundColor(int position) {
        TableItem item = getItem(position);

        if (item == null) {
            int color = R.color.category_unknown_bg;

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

            return mContext.getResources().getColor(color);
        }

        return getBackgroundColor(item);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) == null && (position == 92 || position == 110 ||
                (position >= 4 && position <= 9) || (position >= 22 && position <= 26))) {
            return ViewType.TEXT.ordinal();
        }

        return ViewType.ITEM.ordinal();
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @Override
    public void setItems(TableItem... items) {
        int groups = 0, periods = 0;

        for (TableItem item : items) {
            groups = Math.max(item.getGroup(), groups);
            periods = Math.max(item.getPeriod(), periods);
        }

        mGroupsCount = groups;
        mPeriodsCount = periods + 2;

        TableItem[] sortedItems = new TableItem[mGroupsCount * mPeriodsCount];

        for (TableItem item : items) {
            if (item.getNumber() >= 57 && item.getNumber() <= 71) {
                sortedItems[(mGroupsCount * periods) + 2 + item.getNumber() - 57] = item;
            } else if (item.getNumber() >= 89 && item.getNumber() <= 103) {
                sortedItems[(mGroupsCount * periods) + 20 + item.getNumber() - 89] = item;
            } else {
                sortedItems[((item.getPeriod() - 1) * mGroupsCount) + item.getGroup() - 1] = item;
            }
        }

        super.setItems(sortedItems);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || mBitmaps == null;
    }

    public int getGroupsCount() {
        return mGroupsCount;
    }

    public int getPeriodsCount() {
        return mPeriodsCount;
    }

    public void buildDrawingCache(ViewGroup parent) {
        Bitmap[] bitmaps = new Bitmap[mGroupsCount * mPeriodsCount];

        View convertView = null;
        int previousViewType = 0;

        int size = mContext.getResources().getDimensionPixelSize(R.dimen.table_item_size);

        for (int row = 0; row < mPeriodsCount; row++) {
            for (int column = 0; column < mGroupsCount; column++) {
                int position = (row * mGroupsCount) + column;

                int viewType = getItemViewType(position);
                if (viewType != previousViewType) {
                    convertView = null;
                }
                previousViewType = viewType;

                convertView = getView(position, convertView, parent);

                if (convertView != null) {
                    convertView.measure(View.MeasureSpec.makeMeasureSpec(size,
                                    View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY));
                    convertView.layout(0, 0, convertView.getMeasuredWidth(),
                            convertView.getMeasuredHeight());

                    convertView.buildDrawingCache();

                    if (convertView.getDrawingCache() != null) {
                        bitmaps[position] = Bitmap.createBitmap(convertView.getDrawingCache());
                    }

                    convertView.destroyDrawingCache();
                }
            }
        }

        mBitmaps = bitmaps;
    }

    public Bitmap getDrawingCache(int position) {
        return mBitmaps[position];
    }

    public void destroyDrawingCache() {
        if (mBitmaps != null) {
            for (Bitmap bitmap : mBitmaps) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }

        mBitmaps = null;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position) != null;
    }
}
