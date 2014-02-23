package com.frozendevs.periodictable.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

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
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, this);

        new LoadItems().execute();
    }


    @Override
    public void onGlobalLayout() {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        mMinZoom = Math.min((getWidth() - (HORIZONTAL_SPACERS_COUNT * getSpacing())) / COLUMNS_COUNT,
                (getHeight() - (VERTICAL_SPACERS_COUNT * getSpacing())) / ROWS_COUNT) / DEFAULT_TILE_SIZE;

        if(mMinZoom > DEFAULT_MAX_ZOOM)
            mMaxZoom = mMinZoom;
        else
            mMaxZoom = DEFAULT_MAX_ZOOM;

        mZoom = clamp(mMinZoom, mZoom, mMaxZoom);

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
        float y = (getHeight() - (getScaledTileSize() * ROWS_COUNT) -
                (VERTICAL_SPACERS_COUNT * getSpacing())) / 2f;

        for(int row = 0; row < ROWS_COUNT; row++) {
            float x = (getWidth() - (getScaledTileSize() * COLUMNS_COUNT) -
                    (HORIZONTAL_SPACERS_COUNT * getSpacing())) / 2f;

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

                x += getScaledTileSize() + getSpacing();
            }

            y += getScaledTileSize() + getSpacing();
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

    private float getSpacing() {
        return 1f;
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
            /*float zoomRatio = zoom / mZoom;
            int oldX = getScrollX() - getMinimalScrollX() + x;
            int oldY = getScrollY() - getMinimalScrollY() + y;*/

            mZoom = zoom;

            /*scrollTo(getMinimalScrollX() + Math.round(oldX * zoomRatio) - x,
                    getMinimalScrollY() + Math.round(oldY * zoomRatio) - y);*/

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

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
