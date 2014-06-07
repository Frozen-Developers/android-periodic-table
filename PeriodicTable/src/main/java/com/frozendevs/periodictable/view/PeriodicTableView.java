package com.frozendevs.periodictable.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.OverScroller;

import com.frozendevs.periodictable.R;

public class PeriodicTableView extends ViewGroup implements GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private static final int COLUMNS_COUNT = 18;
    private static final int ROWS_COUNT = 9;
    private static final int HORIZONTAL_SPACERS_COUNT = COLUMNS_COUNT - 1;
    private static final int VERTICAL_SPACERS_COUNT = ROWS_COUNT - 1;

    private final float DEFAULT_SPACING = 1f;

    private static final float DEFAULT_MAX_ZOOM = 1f;

    private View mEmptyView = null;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private float mMinZoom = 0f;
    private float mZoom = 0f;
    private float mMaxZoom = 1f;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private boolean mIsScrolling = false;
    private OverScroller mOverScroller;
    private Adapter mAdapter;
    private View[] mViews;
    private Bitmap[] mBitmaps;
    private Matrix mMatrix = new Matrix();

    public PeriodicTableView(Context context) {
        super(context);
        initPeriodicTableView(context);
    }

    public PeriodicTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPeriodicTableView(context);
    }

    public PeriodicTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPeriodicTableView(context);
    }

    private void initPeriodicTableView(Context context) {
        setWillNotDraw(false);

        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(true);

        TypedArray styledAttributes = context.obtainStyledAttributes(R.styleable.PeriodicTableView);
        initializeScrollbars(styledAttributes);
        styledAttributes.recycle();

        mOverScroller = new OverScroller(context);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        boolean isMinZoom = mMinZoom == mZoom;

        mMinZoom = Math.min((getWidth() - (HORIZONTAL_SPACERS_COUNT * DEFAULT_SPACING)) / COLUMNS_COUNT,
                (getHeight() - (VERTICAL_SPACERS_COUNT * DEFAULT_SPACING)) / ROWS_COUNT) / getDefaultTileSize();

        if(mMinZoom > DEFAULT_MAX_ZOOM)
            mMaxZoom = mMinZoom;
        else
            mMaxZoom = DEFAULT_MAX_ZOOM;

        if(!isMinZoom)
            mZoom = clamp(mMinZoom, mZoom, mMaxZoom);
        else
            mZoom = mMinZoom;

        if(mZoom > 0f) {
            if(getScrollX() < getMinimalScrollX())
                scrollTo(getMinimalScrollX(), getScrollY());
            else if(getScrollX() > getMaximalScrollX())
                scrollTo(getMaximalScrollX(), getScrollY());

            if(getScrollY() < getMinimalScrollY())
                scrollTo(getScrollX(), getMinimalScrollY());
            else if(getScrollY() > getMaximalScrollY())
                scrollTo(getScrollX(), getMaximalScrollY());
        }

        invalidate();
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
                        mMatrix.postScale(mZoom, mZoom);
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
        return mZoom * getResources().getDimension(R.dimen.table_item_size);
    }

    private float clamp(float min, float val, float max) {
        if(Float.isNaN(min)) min = 0f;
        if(Float.isNaN(val)) val = 0f;
        if(Float.isNaN(max)) max = 0f;

        return Math.max(min, Math.min(val, max));
    }

    private int clamp(int min, int val, int max) {
        return Math.max(min, Math.min(val, max));
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        zoomTo((int) detector.getFocusX(), (int) detector.getFocusY(),
                clamp(mMinZoom, mZoom * detector.getScaleFactor(), mMaxZoom));

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    private void zoomTo(int x, int y, float zoom) {
        if(mZoom != zoom) {
            float zoomRatio = zoom / mZoom;
            int oldX = getScrollX() - getMinimalScrollX() + x;
            int oldY = getScrollY() - getMinimalScrollY() + y;

            mZoom = zoom;

            scrollTo(getMinimalScrollX() + Math.round(oldX * zoomRatio) - x,
                    getMinimalScrollY() + Math.round(oldY * zoomRatio) - y);

            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mScaleDetector.onTouchEvent(event) && !mScaleDetector.isInProgress())
            if(!mGestureDetector.onTouchEvent(event) && !mIsScrolling)
                super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsScrolling = false;
                break;
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mOverScroller.forceFinished(true);

        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if(mAdapter != null && !mAdapter.isEmpty()) {
            float rawX = e.getX() + getScrollX();
            float rawY = e.getY() + getScrollY();
            float tileSize = getScaledTileSize();
            float y = (getHeight() - getScaledHeight()) / 2f;

            for(int row = 0; row < ROWS_COUNT; row++) {
                float x = (getWidth() - getScaledWidth()) / 2f;

                for(int column = 0; column < COLUMNS_COUNT; column++) {
                    if(x <= rawX && x + tileSize >= rawX && y <= rawY && y + tileSize >= rawY) {
                        View view = mViews[(row * COLUMNS_COUNT) + column];

                        if(view != null) {
                            view.performClick();
                        }
                    }

                    x += getScaledTileSize() + DEFAULT_SPACING;
                }

                y += getScaledTileSize() + DEFAULT_SPACING;
            }
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mIsScrolling = true;

        scrollTo(getScrollX() + (int) distanceX, getScrollY() + (int) distanceY);

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        mOverScroller.forceFinished(true);
        mOverScroller.fling(getScrollX(), getScrollY(), (int) -velocityX, (int) -velocityY,
                getMinimalScrollX(), getMaximalScrollX(), getMinimalScrollY(), getMaximalScrollY(),
                getWidth() / 2, getHeight() / 2);
        ViewCompat.postInvalidateOnAnimation(this);

        return true;
    }

    private int getScaledWidth() {
        return Math.round((getScaledTileSize() * COLUMNS_COUNT) +
                (HORIZONTAL_SPACERS_COUNT * DEFAULT_SPACING));
    }

    private int getScaledHeight() {
        return Math.round((getScaledTileSize() * ROWS_COUNT) +
                (VERTICAL_SPACERS_COUNT * DEFAULT_SPACING));
    }

    private int getMinimalScrollX() {
        return Math.min((getWidth() - getScaledWidth()) / 2, 0);
    }

    private int getMinimalScrollY() {
        return Math.min((getHeight() - getScaledHeight()) / 2, 0);
    }

    private int getMaximalScrollX() {
        return getMinimalScrollX() + getScaledWidth() - getWidth();
    }

    private int getMaximalScrollY() {
        return getMinimalScrollY() + getScaledHeight() - getHeight();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(clamp(getMinimalScrollX(), x, getMaximalScrollX()),
                clamp(getMinimalScrollY(), y, getMaximalScrollY()));
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        return getWidth();
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return getScrollX() - getMinimalScrollX();
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return getScaledWidth();
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return getHeight();
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return getScrollY() - getMinimalScrollY();
    }

    @Override
    protected int computeVerticalScrollRange() {
        return getScaledHeight();
    }

    @Override
    public void computeScroll() {
        if(mOverScroller.computeScrollOffset())
            scrollTo(mOverScroller.getCurrX(), mOverScroller.getCurrY());
    }

    public void setAdapter(Adapter adapter) {
        if(adapter != null) {
            mAdapter = adapter;

            mAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    if(!mAdapter.isEmpty()) {
                        mViews = new View[COLUMNS_COUNT * ROWS_COUNT];
                        mBitmaps = new Bitmap[COLUMNS_COUNT * ROWS_COUNT];

                        for(int row = 0; row < ROWS_COUNT; row++) {
                            for(int column = 0; column < COLUMNS_COUNT; column++) {
                                int position = (row * COLUMNS_COUNT) + column;

                                View view = mAdapter.getView(position, null, PeriodicTableView.this);

                                if(view != null) {
                                    mViews[position] = view;

                                    view.measure(MeasureSpec.makeMeasureSpec(getDefaultTileSize(), MeasureSpec.EXACTLY),
                                            MeasureSpec.makeMeasureSpec(getDefaultTileSize(), MeasureSpec.EXACTLY));
                                    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

                                    view.buildDrawingCache();

                                    if(view.getDrawingCache() != null) {
                                        mBitmaps[position] = Bitmap.createBitmap(view.getDrawingCache());
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
}
