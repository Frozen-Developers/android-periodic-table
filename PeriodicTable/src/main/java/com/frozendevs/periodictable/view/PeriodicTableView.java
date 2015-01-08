package com.frozendevs.periodictable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.adapter.TableAdapter;

public class PeriodicTableView extends ZoomableScrollView {

    private final float DEFAULT_SPACING = 1f;

    private View mEmptyView = null;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private TableAdapter mAdapter;
    private Matrix mMatrix = new Matrix();
    private OnItemClickListener mOnItemClickListener;
    private View mActiveView;

    public interface OnItemClickListener {

        public void onItemClick(PeriodicTableView parent, View view, int position);
    }

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            updateEmptyStatus(true);

            if (!mAdapter.isEmpty()) {
                invalidate();
            }
        }
    };

    private class SavedState extends BaseSavedState {

        int activeViewPosition = -1;

        public SavedState(Parcel source) {
            super(source);

            activeViewPosition = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

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

    private int getDefaultTileSize() {
        return getResources().getDimensionPixelSize(R.dimen.table_item_size);
    }

    private float getScaledTileSize() {
        return getZoom() * getDefaultTileSize();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mOnItemClickListener != null && mAdapter != null &&
                !mAdapter.isEmpty() && isEnabled()) {
            float rawX = e.getX() + getScrollX();
            float rawY = e.getY() + getScrollY();
            float tileSize = getScaledTileSize();
            int scaledWidth = getScaledWidth();
            int scaledHeight = getScaledHeight();
            float startY = (getHeight() - scaledHeight) / 2f;
            float startX = (getWidth() - scaledWidth) / 2f;

            if (rawX >= startX && rawX <= startX + scaledWidth &&
                    rawY >= startY && rawY <= startY + scaledHeight) {
                int position = ((int) ((rawY - startY) / (tileSize + DEFAULT_SPACING)) *
                        mAdapter.getGroupsCount()) +
                        (int) ((rawX - startX) / (tileSize + DEFAULT_SPACING));

                if (position >= 0 && position < mAdapter.getCount() &&
                        mAdapter.isEnabled(position)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        addActiveView(position);
                    }

                    playSoundEffect(SoundEffectConstants.CLICK);

                    mOnItemClickListener.onItemClick(this, mActiveView, position);
                }
            }
        }

        return true;
    }

    @Override
    protected int getScaledWidth() {
        int groups = mAdapter != null ? mAdapter.getGroupsCount() : 0;

        return Math.round((getScaledTileSize() * groups) + ((groups - 1) * DEFAULT_SPACING));
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
        }
    }

    @Override
    public float getMinimalZoom() {
        int groups = mAdapter != null ? mAdapter.getGroupsCount() : 0;
        int periods = mAdapter != null ? mAdapter.getPeriodsCount() : 0;

        return Math.min((getWidth() - ((groups - 1) * DEFAULT_SPACING)) / groups,
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

    @Override
    public void onDraw(Canvas canvas) {
        adjustActiveView();

        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mAdapter != null && !mAdapter.isEmpty()) {
            float tileSize = getScaledTileSize();

            float y = (getHeight() - getScaledHeight()) / 2f;

            for (int row = 0; row < mAdapter.getPeriodsCount(); row++) {
                float x = (getWidth() - getScaledWidth()) / 2f;

                for (int column = 0; column < mAdapter.getGroupsCount(); column++) {
                    if (x + tileSize > getScrollX() && x < getScrollX() + getWidth() &&
                            y + tileSize > getScrollY() && y < getScrollY() + getHeight()) {
                        int position = (row * mAdapter.getGroupsCount()) + column;

                        if (mActiveView != null &&
                                (int) mActiveView.getTag(R.id.active_view_position) == position) {
                            adjustActiveView();
                        } else {
                            Bitmap bitmap = mAdapter.getDrawingCache(position);

                            if (bitmap != null && !bitmap.isRecycled()) {
                                mMatrix.reset();
                                mMatrix.postScale(getZoom(), getZoom());
                                mMatrix.postTranslate(x, y);

                                canvas.drawBitmap(bitmap, mMatrix, mPaint);
                            }
                        }
                    }

                    x += tileSize + DEFAULT_SPACING;
                }

                y += tileSize + DEFAULT_SPACING;
            }

            updateEmptyStatus(false);
        }

        super.dispatchDraw(canvas);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void adjustActiveView() {
        if (mActiveView != null) {
            int position = (int) mActiveView.getTag(R.id.active_view_position);

            float tileSize = getScaledTileSize();

            float left = ((getWidth() - getScaledWidth()) / 2f) +
                    ((position % mAdapter.getGroupsCount()) * (tileSize + DEFAULT_SPACING));
            float top = ((getHeight() - getScaledHeight()) / 2f) +
                    ((position / mAdapter.getGroupsCount()) * (tileSize + DEFAULT_SPACING));

            mActiveView.setScaleX(getZoom());
            mActiveView.setScaleY(getZoom());
            mActiveView.setTranslationX(left);
            mActiveView.setTranslationY(top);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void addActiveView(int position) {
        if (mActiveView != null &&
                (int) mActiveView.getTag(R.id.active_view_position) == position) {
            adjustActiveView();

            return;
        }

        mActiveView = mAdapter.getView(position, mActiveView, this);

        if (mActiveView != null) {
            super.removeAllViews();

            mActiveView.setTag(R.id.active_view_position, position);
            mActiveView.measure(MeasureSpec.makeMeasureSpec(getDefaultTileSize(),
                    MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                    getDefaultTileSize(), MeasureSpec.EXACTLY));
            mActiveView.layout(0, 0, mActiveView.getMeasuredWidth(),
                    mActiveView.getMeasuredHeight());
            mActiveView.setPivotX(0f);
            mActiveView.setPivotY(0f);

            adjustActiveView();

            addView(mActiveView);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());

        if (mActiveView != null) {
            savedState.activeViewPosition = (int) mActiveView.getTag(R.id.active_view_position);
        }

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;

            if (savedState.activeViewPosition > -1) {
                addActiveView(savedState.activeViewPosition);
            }

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        adjustActiveView();
    }

    @Override
    public void removeAllViews() {
        mActiveView = null;

        super.removeAllViews();
    }
}
