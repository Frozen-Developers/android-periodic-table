package com.frozendevs.periodictable.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.OverScroller;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.activity.PropertiesActivity;
import com.frozendevs.periodictable.model.TableItem;

public class PeriodicTableView extends View implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private static final int COLUMNS_COUNT = 18;
    private static final int ROWS_COUNT = 9;
    private static final int HORIZONTAL_SPACERS_COUNT = COLUMNS_COUNT - 1;
    private static final int VERTICAL_SPACERS_COUNT = ROWS_COUNT - 1;

    private final float DEFAULT_TILE_SIZE = dpToPx(80);
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

        getViewTreeObserver().addOnGlobalLayoutListener(this);
        mOverScroller = new OverScroller(context);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, this);
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

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
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
                        TableItem item = (TableItem)mAdapter.getItem((row * COLUMNS_COUNT) + column);

                        if(item != null) {
                            playSoundEffect(SoundEffectConstants.CLICK);

                            Intent intent = new Intent(getContext(), PropertiesActivity.class);
                            intent.putExtra(PropertiesActivity.EXTRA_ATOMIC_NUMBER, item.getAtomicNumber());
                            getContext().startActivity(intent);
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
                    buildCache();

                    invalidate();

                    updateEmptyStatus(mAdapter.isEmpty());
                }
            });
        }
    }

    private void buildCache() {
        if(mAdapter != null && !mAdapter.isEmpty()) {
            mBitmaps = new Bitmap[COLUMNS_COUNT * ROWS_COUNT];

            for(int row = 0; row < ROWS_COUNT; row++) {
                for(int column = 0; column < COLUMNS_COUNT; column++) {
                    int position = (row * COLUMNS_COUNT) + column;

                    TableItem item = (TableItem)mAdapter.getItem(position);

                    Bitmap bitmap = Bitmap.createBitmap(Math.round(DEFAULT_TILE_SIZE),
                            Math.round(DEFAULT_TILE_SIZE), Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bitmap);

                    if(item != null) {
                        drawTile(canvas, item.getName(), String.valueOf(item.getAtomicNumber()),
                                item.getStandardAtomicWeight(), item.getSymbol(),
                                item.getColor(getContext()));
                    }
                    else if(position == 92) {
                        drawTile(canvas, "57 - 71", "", "", "",
                                getContext().getResources().getColor(R.color.lanthanide_bg));
                    }
                    else if(position == 110) {
                        drawTile(canvas, "89 - 103", "", "", "",
                                getContext().getResources().getColor(R.color.actinide_bg));
                    }

                    mBitmaps[position] = Bitmap.createBitmap(bitmap);
                }
            }
        }
    }

    private void drawTile(Canvas canvas, String name, String number, String weight, String symbol, int color) {
        float padding = dpToPx(5);

        mPaint.setColor(color);
        canvas.drawRect(0, 0, DEFAULT_TILE_SIZE, DEFAULT_TILE_SIZE, mPaint);

        mPaint.setColor(Color.BLACK);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setTextSize(spToPx(14));
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();

        canvas.drawText(symbol, padding, padding + mPaint.getTextSize(), mPaint);

        mPaint.setTextAlign(Paint.Align.RIGHT);

        canvas.drawText(number, DEFAULT_TILE_SIZE - padding, padding + mPaint.getTextSize(), mPaint);

        canvas.drawText(weight, DEFAULT_TILE_SIZE - padding, DEFAULT_TILE_SIZE - padding -
                fontMetrics.descent, mPaint);

        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(spToPx(12));

        canvas.drawText(name, DEFAULT_TILE_SIZE / 2, (DEFAULT_TILE_SIZE / 2) + (mPaint.getTextSize() / 2), mPaint);
    }
}
