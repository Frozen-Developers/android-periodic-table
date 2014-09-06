package com.frozendevs.periodictable.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;

import com.frozendevs.periodictable.R;

public class PeriodicTableView extends ZoomableScrollView {

    private static final int COLUMNS_COUNT = 18;
    private static final int ROWS_COUNT = 9;
    private static final int HORIZONTAL_SPACERS_COUNT = COLUMNS_COUNT - 1;
    private static final int VERTICAL_SPACERS_COUNT = ROWS_COUNT - 1;
    private final float DEFAULT_SPACING = 1f;

    private View mEmptyView = null;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Adapter mAdapter;
    private Bitmap[] mBitmaps;
    private Matrix mMatrix = new Matrix();
    private View mConvertView;

    public PeriodicTableView(Context context) {
        super(context);
    }

    public PeriodicTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PeriodicTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setEmptyView(View view) {
        mEmptyView = view;

        updateEmptyStatus(mAdapter == null || mAdapter.isEmpty());
    }

    private void updateEmptyStatus(boolean empty) {
        if(mEmptyView != null)
            mEmptyView.setVisibility(empty ? VISIBLE : GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mAdapter != null && !mAdapter.isEmpty() && mBitmaps != null) {
            float y = (getHeight() - getScaledHeight()) / 2f;

            for(int row = 0; row < ROWS_COUNT; row++) {
                float x = (getWidth() - getScaledWidth()) / 2f;

                for(int column = 0; column < COLUMNS_COUNT; column++) {
                    int position = (row * COLUMNS_COUNT) + column;

                    if(mBitmaps[position] != null) {
                        mMatrix.reset();
                        mMatrix.postScale(getZoom(), getZoom());
                        mMatrix.postTranslate(x, y);
                        canvas.drawBitmap(mBitmaps[position], mMatrix, mPaint);
                    }

                    x += getScaledTileSize() + DEFAULT_SPACING;
                }

                y += getScaledTileSize() + DEFAULT_SPACING;
            }
        }
    }

    private int getDefaultTileSize() {
        return Math.round(getResources().getDimension(R.dimen.table_item_size));
    }

    private float getScaledTileSize() {
        return getZoom() * getResources().getDimension(R.dimen.table_item_size);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if(mAdapter != null && !mAdapter.isEmpty()) {
            float rawX = e.getX() + getScrollX();
            float rawY = e.getY() + getScrollY();
            float tileSize = getScaledTileSize() + DEFAULT_SPACING;
            float startY = (getHeight() - getScaledHeight()) / 2f;
            float startX = (getWidth() - getScaledWidth()) / 2f;

            View view = mAdapter.getView(((int)((rawY - startY) / tileSize) * COLUMNS_COUNT) +
                    (int)((rawX - startX) / tileSize), mConvertView, this);

            if(view != null) {
                view.performClick();
            }
        }

        return true;
    }

    @Override
    protected int getScaledWidth() {
        return Math.round((getScaledTileSize() * COLUMNS_COUNT) +
                (HORIZONTAL_SPACERS_COUNT * DEFAULT_SPACING));
    }

    @Override
    protected int getScaledHeight() {
        return Math.round((getScaledTileSize() * ROWS_COUNT) +
                (VERTICAL_SPACERS_COUNT * DEFAULT_SPACING));
    }

    public void setAdapter(Adapter adapter) {
        if(adapter != null) {
            mAdapter = adapter;

            mAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    if(!mAdapter.isEmpty()) {
                        mBitmaps = new Bitmap[COLUMNS_COUNT * ROWS_COUNT];

                        for(int row = 0; row < ROWS_COUNT; row++) {
                            for(int column = 0; column < COLUMNS_COUNT; column++) {
                                int position = (row * COLUMNS_COUNT) + column;

                                View view = mAdapter.getView(position, mConvertView,
                                        PeriodicTableView.this);

                                if(view != null) {
                                    view.measure(MeasureSpec.makeMeasureSpec(getDefaultTileSize(),
                                                    MeasureSpec.EXACTLY),
                                            MeasureSpec.makeMeasureSpec(getDefaultTileSize(),
                                                    MeasureSpec.EXACTLY));
                                    view.layout(0, 0, view.getMeasuredWidth(),
                                            view.getMeasuredHeight());

                                    view.buildDrawingCache();

                                    if(view.getDrawingCache() != null) {
                                        mBitmaps[position] = Bitmap.createBitmap(
                                                view.getDrawingCache());
                                    }

                                    view.destroyDrawingCache();
                                }
                            }
                        }

                        invalidate();

                        updateEmptyStatus(false);
                    }
                }
            });
        }
    }

    @Override
    public float getMinimalZoom() {
        return Math.min((getWidth() - (HORIZONTAL_SPACERS_COUNT * DEFAULT_SPACING)) / COLUMNS_COUNT,
                (getHeight() - (VERTICAL_SPACERS_COUNT * DEFAULT_SPACING)) / ROWS_COUNT) / getDefaultTileSize();
    }
}
