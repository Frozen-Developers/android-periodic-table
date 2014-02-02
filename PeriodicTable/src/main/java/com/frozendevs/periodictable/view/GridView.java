package com.frozendevs.periodictable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

import com.frozendevs.periodictable.R;

public class GridView extends LinearLayout {

    /**
     * Default columns count.
     */
    public static final int DEFAULT_COLUMNS_COUNT = 2;

    /**
     * Default rows count.
     */
    public static final int DEFAULT_ROWS_COUNT = 2;

    /**
     * Default number of pixels for horizontal spacing.
     */
    public static final int DEFAULT_HORIZONTAL_SPACING = 0;

    /**
     * Default number of pixels for vertical spacing.
     */
    public static final int DEFAULT_VERTICAL_SPACING = 0;

    private int mNumColumns, mNumRows;
    private int mColumnWidth, mRowHeight;
    private int mHorizontalSpacing, mVerticalSpacing;
    private Adapter mAdapter = null;
    private View mEmptyView = null;

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGridView(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initGridView(attrs);
    }

    private void initGridView(AttributeSet attrs) {
        TypedArray style = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.GridView, 0, 0);

        try {
            mNumColumns = style.getInteger(R.styleable.GridView_numColumns, DEFAULT_COLUMNS_COUNT);
            mNumRows = style.getInteger(R.styleable.GridView_numRows, DEFAULT_ROWS_COUNT);
            mColumnWidth = style.getDimensionPixelSize(R.styleable.GridView_columnWidth,
                    getWidth() / DEFAULT_COLUMNS_COUNT);
            mRowHeight = style.getDimensionPixelSize(R.styleable.GridView_rowHeight,
                    getHeight() / DEFAULT_ROWS_COUNT);
            mHorizontalSpacing = style.getDimensionPixelSize(R.styleable.GridView_horizontalSpacing,
                    DEFAULT_HORIZONTAL_SPACING);
            mVerticalSpacing = style.getDimensionPixelSize(R.styleable.GridView_verticalSpacing,
                    DEFAULT_VERTICAL_SPACING);
        } finally {
            style.recycle();
        }

        setOrientation(VERTICAL);
    }

    private void fillWithData() {
        if(mAdapter.getCount() > 0) {
            for(int i = 0; i < mNumRows; i ++) {
                LinearLayout row = new LinearLayout(getContext());
                LayoutParams rowParams = new LayoutParams((mNumColumns * mColumnWidth) +
                        ((mNumColumns - 2) * mHorizontalSpacing), mRowHeight);
                rowParams.setMargins(0, i > 0 ? mVerticalSpacing : 0, 0, 0);
                row.setLayoutParams(rowParams);
                row.setOrientation(HORIZONTAL);

                for(int n = 0; n < mNumColumns; n++) {
                    LinearLayout cell = new LinearLayout(getContext());
                    LayoutParams cellParams = new LayoutParams(mColumnWidth, mRowHeight);
                    cellParams.setMargins(n > 0 ? mHorizontalSpacing : 0, 0, 0, 0);
                    cell.setLayoutParams(cellParams);
                    cell.addView(mAdapter.getView((i * mNumColumns) + n, null, cell));

                    row.addView(cell);
                }

                addView(row);
            }

            if(mEmptyView != null)
                mEmptyView.setVisibility(GONE);
        }
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    public int getNumRows() {
        return mNumRows;
    }

    public int getColumnWidth() {
        return mColumnWidth;
    }

    public int getRowHeight() {
        return mRowHeight;
    }

    public int getHorizontalSpacing() {
        return mHorizontalSpacing;
    }

    public int getVerticalSpacing() {
        return mVerticalSpacing;
    }

    /**
     * Returns the adapter currently associated with this widget.
     *
     * @return The adapter used to provide this view's content.
     *
     * @see #setAdapter(android.widget.Adapter)
     */
    public Adapter getAdapter() {
        return mAdapter;
    }

    /**
     * Sets the data behind this GridView.
     *
     * @param adapter the adapter providing the grid's data
     *
     * @see #getAdapter()
     */
    public void setAdapter(Adapter adapter) {
        this.mAdapter = adapter;

        fillWithData();

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if(mEmptyView != null)
                    mEmptyView.setVisibility(VISIBLE);

                removeAllViews();
                fillWithData();
            }
        });
    }

    /**
     * Sets the view to show if the adapter is empty
     */
    public void setEmptyView(View view) {
        mEmptyView = view;

        if(mAdapter == null || mAdapter.getCount() == 0)
            mEmptyView.setVisibility(VISIBLE);
        else
            mEmptyView.setVisibility(GONE);
    }
}
