package com.frozendevs.periodictable.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.frozendevs.periodictable.R;

/**
 * ZoomView is designed for dynamic zooming of all kinds of views.
 */
public class ZoomView extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private float mMaxZoom = 1.0f;
    private float mMinZoom = 0f;
    private float mZoom = 0f;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private boolean mIsScrolling = false;
    private OverScroller mOverScroller;

    public ZoomView(Context context) {
        super(context);
        initZoomView(context);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initZoomView(context);
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initZoomView(context);
    }

    private void initZoomView(Context context) {
        setWillNotDraw(false);

        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(true);
        
        TypedArray styledAttributes = context.obtainStyledAttributes(R.styleable.ZoomView);
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

        mMinZoom = Math.min(Math.min((float)getWidth() / (float)getMeasuredWidth(),
                (float)getHeight() / (float)getMeasuredHeight()), mMaxZoom);

        mZoom = clamp(mMinZoom, mZoom, mMaxZoom);

        if(mZoom > 0f) {
            scrollTo(getMinimalScrollX(), getMinimalScrollY());

            scaleChildren();
        }
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

    private void scaleChildren() {
        for(int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);

            view.setScaleX(mZoom);
            view.setScaleY(mZoom);
            int left = view.getLeft();
            int top = view.getTop();
            view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
        }
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

    private int getMinimalScrollX() {
        return ((getMeasuredWidth() - getScaledWidth()) / 2) -
                (getScaledWidth() < getWidth() ? (getWidth() - getScaledWidth()) / 2 : 0);
    }

    private int getMinimalScrollY() {
        return ((getMeasuredHeight() - getScaledHeight()) / 2) -
                (getScaledHeight() < getHeight() ? (getHeight() - getScaledHeight()) / 2 : 0);
    }

    private int getMaximalScrollX() {
        return getScaledWidth() - getWidth() + getMinimalScrollX();
    }

    private int getMaximalScrollY() {
        return getScaledHeight() - getHeight() + getMinimalScrollY();
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

    /**
     * Returns the minimal scale factor that will be used.
     *
     * @return the minimal scale factor
     *
     * @see #getZoom()
     * @see #getMaximalZoom()
     */
    public float getMinimalZoom() {
        return mMinZoom;
    }

    /**
     * Returns the maximal scale factor that will be used.
     *
     * @return the maximal scale factor
     *
     * @see #setMaximalZoom(float)
     * @see #getMinimalZoom()
     * @see #getZoom()
     */
    public float getMaximalZoom() {
        return mMaxZoom;
    }

    /**
     * @return the current scale factor
     *
     * @see #getMinimalZoom()
     * @see #getMaximalZoom()
     */
    public float getZoom() {
        return mZoom;
    }

    private int getScaledWidth() {
        return Math.round((float) getMeasuredWidth() * mZoom);
    }

    private int getScaledHeight() {
        return Math.round((float) getMeasuredHeight() * mZoom);
    }

    /**
     * Zoom to specified point on the screen.
     *
     * @param x the x coordinate of the zoom point
     * @param y the y coordinate of the zoom point
     * @param zoom the new scale factor
     */
    public void zoomTo(int x, int y, float zoom) {
        if(mZoom != zoom && zoom >= mMinZoom && zoom <= mMaxZoom) {
            float zoomRatio = zoom / mZoom;
            int oldX = getScrollX() - getMinimalScrollX() + x;
            int oldY = getScrollY() - getMinimalScrollY() + y;

            mZoom = zoom;

            scrollTo(getMinimalScrollX() + Math.round(oldX * zoomRatio) - x,
                    getMinimalScrollY() + Math.round(oldY * zoomRatio) - y);

            scaleChildren();
        }
    }

    @Override
    public void scrollTo (int x, int y) {
        super.scrollTo(clamp(getMinimalScrollX(), x, getMaximalScrollX()),
                clamp(getMinimalScrollY(), y, getMaximalScrollY()));
    }

    /**
     * Sets the maximal scale factor that will be used.
     *
     * @param zoom the new maximal scale factor
     *
     * @see #getMaximalZoom()
     */
    public void setMaximalZoom(float zoom) {
        if(zoom > mMinZoom)
            mMaxZoom = zoom;
    }

    @Override
    public void computeScroll() {
        if(mOverScroller.computeScrollOffset())
            scrollTo(mOverScroller.getCurrX(), mOverScroller.getCurrY());
    }
}
