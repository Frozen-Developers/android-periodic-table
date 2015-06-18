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
import android.view.accessibility.AccessibilityEvent;

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

    private abstract class OnClickConfirmedListener {

        abstract void onClickConfirmed(int position);
    }

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            updateEmptyStatus(true);

            if (!mAdapter.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mActiveView == null) {
                    for (int i = 0; i < mAdapter.getCount(); i++) {
                        if (mAdapter.getItem(i) != null && mAdapter.isEnabled(i)) {
                            addActiveView(i);

                            break;
                        }
                    }
                }

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

    private OnClickConfirmedListener mOnSingleTapConfirmed = new OnClickConfirmedListener() {
        @Override
        void onClickConfirmed(int position) {
            playSoundEffect(SoundEffectConstants.CLICK);

            mOnItemClickListener.onItemClick(PeriodicTableView.this, mActiveView, position);

            if (mActiveView != null) {
                mActiveView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
            }
        }
    };

    private OnClickConfirmedListener mOnDownConfirmed;

    public PeriodicTableView(Context context) {
        super(context);

        initPeriodicTableView();
    }

    public PeriodicTableView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initPeriodicTableView();
    }

    public PeriodicTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initPeriodicTableView();
    }

    private void initPeriodicTableView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mOnDownConfirmed = new OnClickConfirmedListener() {
                @Override
                void onClickConfirmed(int position) {
                    addActiveView(position);
                }
            };
        }
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

    private float getScaledTileSize() {
        return getZoom() * mAdapter.getTileSize();
    }

    private float getStartX() {
        return getMinimalScrollX();
    }

    private float getStartY() {
        return getMinimalScrollY() + Math.max((getHeight() - getScaledHeight()) / 2f, 0);
    }

    private float getTileX(int position) {
        return ((position % mAdapter.getGroupsCount()) * (getScaledTileSize() + DEFAULT_SPACING)) +
                getStartX();
    }

    private float getTileY(int position) {
        return ((position / mAdapter.getGroupsCount()) * (getScaledTileSize() + DEFAULT_SPACING)) +
                getStartY();
    }

    private int getPosition(float x, float y) {
        final float tileSize = getScaledTileSize() + DEFAULT_SPACING;

        return ((int) ((y - getStartY()) / tileSize) * mAdapter.getGroupsCount()) +
                (int) ((x - getStartX()) / tileSize);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return mOnItemClickListener != null && processClick(event, mOnSingleTapConfirmed);
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
        final int groups = mAdapter.getGroupsCount();
        final int periods = mAdapter.getPeriodsCount();
        final int tileSize = mAdapter.getTileSize();

        return Math.min((getWidth() - ((groups - 1) * DEFAULT_SPACING)) / groups,
                (getHeight() - ((periods - 1) * DEFAULT_SPACING)) / periods) / tileSize;
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

            float y = getStartY();

            for (int row = 0; row < mAdapter.getPeriodsCount(); row++) {
                float x = getStartX();

                for (int column = 0; column < mAdapter.getGroupsCount(); column++) {
                    if (x + tileSize > getScrollX() && x < getScrollX() + getWidth() &&
                            y + tileSize > getScrollY() && y < getScrollY() + getHeight()) {
                        int position = (row * mAdapter.getGroupsCount()) + column;

                        if (mActiveView != null && indexOfChild(mActiveView) >= 0 &&
                                position == (int) mActiveView.getTag(R.id.active_view_position)) {
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

            mActiveView.setScaleX(getZoom());
            mActiveView.setScaleY(getZoom());
            mActiveView.setTranslationX(getTileX(position));
            mActiveView.setTranslationY(getTileY(position));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void addActiveView(int position) {
        if (mActiveView != null) {
            if (position == (int) mActiveView.getTag(R.id.active_view_position)) {
                adjustActiveView();

                return;
            }

            removeView(mActiveView);
        }

        mActiveView = mAdapter.getActiveView(mAdapter.getDrawingCache(position), mActiveView, this);

        if (mActiveView != null) {
            mActiveView.setTag(R.id.active_view_position, position);
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

    public View getActiveView() {
        return mActiveView;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        super.onDown(event);

        return processClick(event, mOnDownConfirmed);
    }

    private boolean processClick(MotionEvent event, OnClickConfirmedListener listener) {
        if (listener != null && mAdapter != null && !mAdapter.isEmpty()) {
            final float rawX = event.getX() + getScrollX();
            final float rawY = event.getY() + getScrollY();
            final float startX = getStartX();
            final float startY = getStartY();

            if (rawX >= startX && rawX <= startX + getScaledWidth() &&
                    rawY >= startY && rawY <= startY + getScaledHeight()) {
                final int position = getPosition(rawX, rawY);

                if (position >= 0 && position < mAdapter.getCount() &&
                        mAdapter.isEnabled(position)) {
                    listener.onClickConfirmed(position);

                    return true;
                }
            }
        }

        return false;
    }
}
