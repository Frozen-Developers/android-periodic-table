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

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.adapter.TableAdapter;

public class PeriodicTableView extends ZoomableScrollView {

    private static final int GROUPS_COUNT = 18;
    private final float DEFAULT_SPACING = 1f;

    private View mEmptyView = null;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private TableAdapter mAdapter;
    private Matrix mMatrix = new Matrix();
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        public boolean onItemClick(int position);
    }

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            updateEmptyStatus(true);

            if (!mAdapter.isEmpty()) {
                invalidate();

                updateEmptyStatus(false);
            }
        }
    };

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
        if (mEmptyView != null) {
            mEmptyView.setVisibility(empty ? VISIBLE : GONE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mAdapter != null && !mAdapter.isEmpty()) {
            float tileSize = getScaledTileSize();

            float y = (getHeight() - getScaledHeight()) / 2f;

            for (int row = 0; row < mAdapter.getPeriodsCount(); row++) {
                float x = (getWidth() - getScaledWidth()) / 2f;

                for (int column = 0; column < GROUPS_COUNT; column++) {
                    if (x + tileSize > getScrollX() && x < getScrollX() + getWidth() &&
                            y + tileSize > getScrollY() && y < getScrollY() + getHeight()) {
                        Bitmap bitmap = mAdapter.getDrawingCache((row * GROUPS_COUNT) + column);

                        if (bitmap != null) {
                            mMatrix.reset();
                            mMatrix.postScale(getZoom(), getZoom());
                            mMatrix.postTranslate(x, y);
                            canvas.drawBitmap(bitmap, mMatrix, mPaint);
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
        return getResources().getDimensionPixelSize(R.dimen.table_item_size);
    }

    private float getScaledTileSize() {
        return getZoom() * getDefaultTileSize();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mOnItemClickListener != null && mAdapter != null && !mAdapter.isEmpty()) {
            float rawX = e.getX() + getScrollX();
            float rawY = e.getY() + getScrollY();
            float tileSize = getScaledTileSize() + DEFAULT_SPACING;
            int scaledWidth = getScaledWidth();
            int scaledHeight = getScaledHeight();
            float startY = (getHeight() - scaledHeight) / 2f;
            float startX = (getWidth() - scaledWidth) / 2f;

            if (rawX >= startX && rawX <= startX + scaledWidth &&
                    rawY >= startY && rawY <= startY + scaledHeight) {
                int position = ((int) ((rawY - startY) / tileSize) * GROUPS_COUNT) +
                        (int) ((rawX - startX) / tileSize);

                if (position >= 0 && position < mAdapter.getCount()) {
                    if (mOnItemClickListener.onItemClick(position)) {
                        playSoundEffect(SoundEffectConstants.CLICK);
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
        int periods = mAdapter != null ? mAdapter.getPeriodsCount() : 0;

        return Math.round((getScaledTileSize() * periods) +
                ((periods - 1) * DEFAULT_SPACING));
    }

    public void setAdapter(TableAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;

        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);

            updateEmptyStatus(mAdapter.isEmpty());
        }
    }

    @Override
    public float getMinimalZoom() {
        int periods = mAdapter != null ? mAdapter.getPeriodsCount() : 0;

        return Math.min((getWidth() - ((GROUPS_COUNT - 1) * DEFAULT_SPACING)) / GROUPS_COUNT,
                (getHeight() - ((periods - 1) * DEFAULT_SPACING)) / periods) / getDefaultTileSize();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
