package com.frozendevs.periodictable.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Adapter;

import com.frozendevs.periodictable.R;

public class PeriodicTableView extends ZoomableScrollView {

    private static final int GROUPS_COUNT = 18;
    private final float DEFAULT_SPACING = 1f;

    private View mEmptyView = null;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Adapter mAdapter;
    private Bitmap[] mBitmaps;
    private Matrix mMatrix = new Matrix();
    private View mConvertView;
    private int mPeriodsCount = 0;

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
        if (mEmptyView != null)
            mEmptyView.setVisibility(empty ? VISIBLE : GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mAdapter != null && !mAdapter.isEmpty() && mBitmaps != null) {
            float tileSize = getScaledTileSize();

            float y = (getHeight() - getScaledHeight()) / 2f;

            for (int row = 0; row < mPeriodsCount; row++) {
                float x = (getWidth() - getScaledWidth()) / 2f;

                for (int column = 0; column < GROUPS_COUNT; column++) {
                    if (x + tileSize > getScrollX() && x < getScrollX() + getWidth() &&
                            y + tileSize > getScrollY() && y < getScrollY() + getHeight()) {
                        int position = (row * GROUPS_COUNT) + column;

                        if (mBitmaps[position] != null) {
                            mMatrix.reset();
                            mMatrix.postScale(getZoom(), getZoom());
                            mMatrix.postTranslate(x, y);
                            canvas.drawBitmap(mBitmaps[position], mMatrix, mPaint);
                        }
                    }

                    x += tileSize + DEFAULT_SPACING;
                }

                y += tileSize + DEFAULT_SPACING;
            }

            super.onDraw(canvas);
        }
    }

    private int getDefaultTileSize() {
        return Math.round(getResources().getDimension(R.dimen.table_item_size));
    }

    private float getScaledTileSize() {
        return getZoom() * getResources().getDimension(R.dimen.table_item_size);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mAdapter != null && !mAdapter.isEmpty()) {
            float rawX = e.getX() + getScrollX();
            float rawY = e.getY() + getScrollY();
            float tileSize = getScaledTileSize() + DEFAULT_SPACING;
            float startY = (getHeight() - getScaledHeight()) / 2f;
            float startX = (getWidth() - getScaledWidth()) / 2f;

            int position = ((int) ((rawY - startY) / tileSize) * GROUPS_COUNT) +
                    (int) ((rawX - startX) / tileSize);

            if (position < mAdapter.getCount()) {
                View view = mAdapter.getView(position, mConvertView, this);

                if (view != null) {
                    if (view.isClickable()) {
                        playSoundEffect(SoundEffectConstants.CLICK);

                        view.performClick();
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected int getScaledWidth() {
        return Math.round((getScaledTileSize() * GROUPS_COUNT) +
                ((GROUPS_COUNT - 1) * DEFAULT_SPACING));
    }

    @Override
    protected int getScaledHeight() {
        return Math.round((getScaledTileSize() * mPeriodsCount) +
                ((mPeriodsCount - 1) * DEFAULT_SPACING));
    }

    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            mAdapter = adapter;

            mAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    mPeriodsCount = mAdapter.getCount() / GROUPS_COUNT;

                    if (!mAdapter.isEmpty()) {
                        mBitmaps = new Bitmap[GROUPS_COUNT * mPeriodsCount];

                        for (int row = 0; row < mPeriodsCount; row++) {
                            for (int column = 0; column < GROUPS_COUNT; column++) {
                                int position = (row * GROUPS_COUNT) + column;

                                View view = mAdapter.getView(position, mConvertView,
                                        PeriodicTableView.this);

                                if (view != null) {
                                    view.measure(MeasureSpec.makeMeasureSpec(getDefaultTileSize(),
                                                    MeasureSpec.EXACTLY),
                                            MeasureSpec.makeMeasureSpec(getDefaultTileSize(),
                                                    MeasureSpec.EXACTLY));
                                    view.layout(0, 0, view.getMeasuredWidth(),
                                            view.getMeasuredHeight());

                                    view.buildDrawingCache();

                                    if (view.getDrawingCache() != null) {
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
        return Math.min((getWidth() - ((GROUPS_COUNT - 1) * DEFAULT_SPACING)) / GROUPS_COUNT,
                (getHeight() - ((mPeriodsCount - 1) * DEFAULT_SPACING)) / mPeriodsCount) / getDefaultTileSize();
    }
}
