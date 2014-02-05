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

/**
 * Replacement for Android's GridView. We use our own implementation because Android's GridView is
 * not suitable for usage with ZoomView.
 */
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

    private int mNumColumns = DEFAULT_COLUMNS_COUNT;
    private int mNumRows = DEFAULT_ROWS_COUNT;
    private int mColumnWidth = 0;
    private int mRowHeight = 0;
    private int mHorizontalSpacing = DEFAULT_HORIZONTAL_SPACING;
    private int mVerticalSpacing = DEFAULT_VERTICAL_SPACING;
    private Adapter mAdapter = null;
    private View mEmptyView = null;
    private DataSetObserver mDataSetObserver;

    public GridView(Context context) {
        super(context);

        initGridView();

        mColumnWidth = computeDefaultColumnWidth();
        mRowHeight = computeDefaultRowHeight();
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initGridView();

        initFromAttrs(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initGridView();

        initFromAttrs(attrs);
    }

    private void initGridView() {
        setOrientation(VERTICAL);

        mDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                updateEmptyStatus(true);

                removeAllViews();

                if(!mAdapter.isEmpty()) {
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

                    updateEmptyStatus(false);
                }
            }
        };
    }

    private void initFromAttrs(AttributeSet attrs) {
        TypedArray style = getContext().getTheme().obtainStyledAttributes(attrs,
                R.styleable.GridView, 0, 0);

        try {
            mNumColumns = style.getInteger(R.styleable.GridView_numColumns, DEFAULT_COLUMNS_COUNT);
            mNumRows = style.getInteger(R.styleable.GridView_numRows, DEFAULT_ROWS_COUNT);
            mColumnWidth = style.getDimensionPixelSize(R.styleable.GridView_columnWidth,
                    computeDefaultColumnWidth());
            mRowHeight = style.getDimensionPixelSize(R.styleable.GridView_rowHeight,
                    computeDefaultRowHeight());
            mHorizontalSpacing = style.getDimensionPixelSize(R.styleable.GridView_horizontalSpacing,
                    DEFAULT_HORIZONTAL_SPACING);
            mVerticalSpacing = style.getDimensionPixelSize(R.styleable.GridView_verticalSpacing,
                    DEFAULT_VERTICAL_SPACING);
        } finally {
            style.recycle();
        }
    }

    private int computeDefaultColumnWidth() {
        return (getWidth() - (mHorizontalSpacing * (mNumColumns - 1))) / mNumColumns;
    }

    private int computeDefaultRowHeight() {
        return (getHeight() - (mVerticalSpacing * (mNumRows - 1))) / mNumRows;
    }

    /**
     * Get the number of columns in the grid.
     */
    public int getNumColumns() {
        return mNumColumns;
    }

    /**
     * Get the number of rows in the grid.
     */
    public int getNumRows() {
        return mNumRows;
    }

    /**
     * Return the width of a column in the grid.
     *
     * @return The column width in pixels
     */
    public int getColumnWidth() {
        return mColumnWidth;
    }

    /**
     * Return the height of a row in the grid.
     *
     * @return The row height in pixels
     */
    public int getRowHeight() {
        return mRowHeight;
    }

    /**
     * Returns the amount of horizontal spacing currently used between each item in the grid.
     *
     * @return Current horizontal spacing between each item in pixels
     */
    public int getHorizontalSpacing() {
        return mHorizontalSpacing;
    }

    /**
     * Returns the amount of vertical spacing between each item in the grid.
     *
     * @return The vertical spacing between items in pixels
     */
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
        if(adapter != null) {
            if(mAdapter != null)
                mAdapter.unregisterDataSetObserver(mDataSetObserver);

            mAdapter = adapter;

            mAdapter.registerDataSetObserver(mDataSetObserver);

            mDataSetObserver.onChanged();
        }
    }

    /**
     * Sets the view to show if the adapter is empty
     */
    public void setEmptyView(View view) {
        mEmptyView = view;

        updateEmptyStatus(mAdapter == null || mAdapter.isEmpty());
    }

    /**
     * Update the status of the list based on the empty parameter.  If empty is true and
     * we have an empty view, display it.  In all the other cases, make sure that the listview
     * is VISIBLE and that the empty view is GONE (if it's not null).
     */
    private void updateEmptyStatus(boolean empty) {
        if(mEmptyView != null) {
            if(empty)
                mEmptyView.setVisibility(VISIBLE);
            else
                mEmptyView.setVisibility(GONE);
        }
    }
}
