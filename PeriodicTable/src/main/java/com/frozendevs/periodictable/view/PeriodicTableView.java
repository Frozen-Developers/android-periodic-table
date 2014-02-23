package com.frozendevs.periodictable.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.OverScroller;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.TableItem;

public class PeriodicTableView extends View implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private static final int COLUMNS_COUNT = 18;
    private static final int ROWS_COUNT = 9;
    private static final int HORIZONTAL_SPACERS_COUNT = COLUMNS_COUNT - 1;
    private static final int VERTICAL_SPACERS_COUNT = ROWS_COUNT - 1;

    private final float DEFAULT_TILE_SIZE = dpToPx(80);
    private final float DEFAULT_SPACING = 1;

    private static final float DEFAULT_MAX_ZOOM = 1f;

    private View mEmptyView = null;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TableItem[] mItems = new TableItem[0];
    private float mMinZoom = 0f;
    private float mZoom = 0f;
    private float mMaxZoom = 1f;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private boolean mIsScrolling = false;
    private OverScroller mOverScroller;

    private class LoadItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mItems = Database.getTableItems(getContext());

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            invalidate();

            updateEmptyStatus(false);
        }
    }

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

        getViewTreeObserver().addOnGlobalLayoutListener(this);
        mOverScroller = new OverScroller(context);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, this);

        new LoadItems().execute();
    }


    @Override
    public void onGlobalLayout() {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        mMinZoom = Math.min((getWidth() - (HORIZONTAL_SPACERS_COUNT * DEFAULT_SPACING)) / COLUMNS_COUNT,
                (getHeight() - (VERTICAL_SPACERS_COUNT * DEFAULT_SPACING)) / ROWS_COUNT) / DEFAULT_TILE_SIZE;

        if(mMinZoom > DEFAULT_MAX_ZOOM)
            mMaxZoom = mMinZoom;
        else
            mMaxZoom = DEFAULT_MAX_ZOOM;

        mZoom = clamp(mMinZoom, mZoom, mMaxZoom);

        if(mZoom > 0f) {
            scrollTo(getMinimalScrollX(), getMinimalScrollY());
        }

        invalidate();
    }

    public void setEmptyView(View view) {
        mEmptyView = view;

        updateEmptyStatus(mItems.length == 0);
    }

    private void updateEmptyStatus(boolean empty) {
        if(mEmptyView != null)
            mEmptyView.setVisibility(empty ? VISIBLE : GONE);
    }

    private TableItem getItem(int position) {
        for (TableItem item : mItems) {
            if (((item.getPeriod() - 1) * 18) + item.getGroup() - 1 == position)
                return item;
            else if (position >= 128 && position <= 142) {
                if (item.getAtomicNumber() + 71 == position)
                    return item;
            } else if (position >= 146 && position <= 160) {
                if (item.getAtomicNumber() + 57 == position)
                    return item;
            }
        }

        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float y = (getHeight() - getScaledHeight()) / 2f;

        for(int row = 0; row < ROWS_COUNT; row++) {
            float x = (getWidth() - getScaledWidth()) / 2f;

            for(int column = 0; column < COLUMNS_COUNT; column++) {
                int position = (row * COLUMNS_COUNT) + column;

                TableItem item = getItem(position);

                if(item != null) {
                    drawTile(canvas, x, y, item.getName(), String.valueOf(item.getAtomicNumber()),
                            item.getStandardAtomicWeight(), item.getColor(getContext()));
                }
                else if(position == 92) {
                    drawTile(canvas, x, y, "57 - 71", "", "", getContext().getResources().getColor(
                            R.color.lanthanide_bg));
                }
                else if(position == 110) {
                    drawTile(canvas, x, y, "89 - 103", "", "", getContext().getResources().getColor(
                            R.color.actinide_bg));
                }

                x += getScaledTileSize() + DEFAULT_SPACING;
            }

            y += getScaledTileSize() + DEFAULT_SPACING;
        }
    }

    private void drawTile(Canvas canvas, float x, float y, String name, String number, String weight,
                          int color) {
        mPaint.setColor(color);
        canvas.drawRect(x, y, x + getScaledTileSize(), y + getScaledTileSize(), mPaint);
    }

    private float dpToPx(float dp) {
        return dp * (getContext().getResources().getDisplayMetrics().densityDpi / 160f);
    }

    private float getScaledTileSize() {
        return mZoom * DEFAULT_TILE_SIZE;
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
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(mScaleDetector.onTouchEvent(event) && !mScaleDetector.isInProgress())
            if(!mGestureDetector.onTouchEvent(event) && !mIsScrolling)
                super.dispatchTouchEvent(event);

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
        return false;
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
}
