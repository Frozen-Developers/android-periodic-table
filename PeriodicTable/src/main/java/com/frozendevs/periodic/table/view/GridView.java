package com.frozendevs.periodic.table.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.LinearLayout;

import com.frozendevs.periodic.table.R;

public class GridView extends LinearLayout {

    private int numColumns, numRows;
    private int columnWidth, rowHeight;
    private int horizontalSpacing, verticalSpacing;

    private Adapter adapter = null;

    private View emptyView;

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray style = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.GridView, 0, 0);

        try {
            numColumns = style.getInteger(R.styleable.GridView_numColumns, 0);
            numRows = style.getInteger(R.styleable.GridView_numRows, 0);
            columnWidth = style.getDimensionPixelSize(R.styleable.GridView_columnWidth, 0);
            rowHeight = style.getDimensionPixelSize(R.styleable.GridView_rowHeight, 0);
            horizontalSpacing = style.getDimensionPixelSize(R.styleable.GridView_horizontalSpacing, 0);
            verticalSpacing = style.getDimensionPixelSize(R.styleable.GridView_verticalSpacing, 0);
        } finally {
            style.recycle();
        }

        setOrientation(VERTICAL);
    }

    private void fillWithData() {
        if(adapter.getCount() > 0) {
            for(int i = 0; i < numRows; i ++) {
                if(i > 0)
                    addView(getSpacer(true));

                LinearLayout row = new LinearLayout(getContext());
                row.setLayoutParams(new LayoutParams((numColumns * columnWidth) +
                        ((numColumns - 2) * horizontalSpacing), rowHeight));
                row.setOrientation(HORIZONTAL);

                for(int n = 0; n < numColumns; n++) {
                    if(n > 0)
                        row.addView(getSpacer(false));

                    LinearLayout cell = new LinearLayout(getContext());
                    cell.setLayoutParams(new LayoutParams(columnWidth, rowHeight));
                    cell.addView(adapter.getView((i * numColumns) + n, null, cell));

                    row.addView(cell);
                }

                addView(row);
            }

            if(emptyView != null)
                emptyView.setVisibility(GONE);
        }
    }

    private LinearLayout getSpacer(boolean vertical) {
        LinearLayout view = new LinearLayout(getContext());

        if(!vertical)
            view.setLayoutParams(new LayoutParams(horizontalSpacing, rowHeight));
        else
            view.setLayoutParams(new LayoutParams(columnWidth, verticalSpacing));

        return view;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;

        fillWithData();

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if(emptyView != null)
                    emptyView.setVisibility(VISIBLE);

                removeAllViews();
                fillWithData();
            }
        });
    }

    public void setEmptyView(View view) {
        emptyView = view;

        if(adapter == null || adapter.getCount() == 0)
            emptyView.setVisibility(VISIBLE);
        else
            emptyView.setVisibility(GONE);
    }
}
