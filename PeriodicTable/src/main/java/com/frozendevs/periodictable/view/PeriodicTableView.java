package com.frozendevs.periodictable.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
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
    private int mPeriodsCount = 0;
    private AsyncTask<Void, Void, Void> mOnChangedTask;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        public boolean onItemClick(int position);
    }

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mOnChangedTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    updateEmptyStatus(true);
                }

                @Override
                protected Void doInBackground(Void... params) {
                    mPeriodsCount = mAdapter.getCount() / GROUPS_COUNT;

                    if (!mAdapter.isEmpty()) {
                        mBitmaps = new Bitmap[GROUPS_COUNT * mPeriodsCount];

                        View convertView = null;
                        int previousViewType = 0;

                        for (int row = 0; row < mPeriodsCount; row++) {
                            for (int column = 0; column < GROUPS_COUNT; column++) {
                                int position = (row * GROUPS_COUNT) + column;

                                int viewType = mAdapter.getItemViewType(position);
                                if (viewType != previousViewType) {
                                    convertView = null;
                                }
                                previousViewType = viewType;

                                convertView = mAdapter.getView(position, convertView,
                                        PeriodicTableView.this);

                                if (convertView != null) {
                                    convertView.measure(
                                            MeasureSpec.makeMeasureSpec(getDefaultTileSize(),
                                                    MeasureSpec.EXACTLY),
                                            MeasureSpec.makeMeasureSpec(getDefaultTileSize(),
                                                    MeasureSpec.EXACTLY));
                                    convertView.layout(0, 0, convertView.getMeasuredWidth(),
                                            convertView.getMeasuredHeight());

                                    convertView.buildDrawingCache();

                                    if (convertView.getDrawingCache() != null) {
                                        mBitmaps[position] = Bitmap.createBitmap(
                                                convertView.getDrawingCache());
                                    }

                                    convertView.destroyDrawingCache();
                                }
                            }
                        }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (!mAdapter.isEmpty()) {
                        invalidate();

                        updateEmptyStatus(false);
                    }
                }
            }.execute();
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
        return Math.round((getScaledTileSize() * mPeriodsCount) +
                ((mPeriodsCount - 1) * DEFAULT_SPACING));
    }

    public void setAdapter(Adapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;

        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public float getMinimalZoom() {
        return Math.min((getWidth() - ((GROUPS_COUNT - 1) * DEFAULT_SPACING)) / GROUPS_COUNT,
                (getHeight() - ((mPeriodsCount - 1) * DEFAULT_SPACING)) / mPeriodsCount) / getDefaultTileSize();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        if (mOnChangedTask != null) {
            mOnChangedTask.cancel(true);
        }

        if (mBitmaps != null) {
            for (Bitmap bitmap : mBitmaps) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
