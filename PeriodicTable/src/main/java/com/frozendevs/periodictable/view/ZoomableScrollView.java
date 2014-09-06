package com.frozendevs.periodictable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.frozendevs.periodictable.R;

public class ZoomableScrollView extends FrameLayout implements GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private static final float DEFAULT_MAX_ZOOM = 1f;

    private OverScroller mOverScroller;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private EdgeEffectCompat mEdgeEffectTop;
    private EdgeEffectCompat mEdgeEffectBottom;
    private EdgeEffectCompat mEdgeEffectLeft;
    private EdgeEffectCompat mEdgeEffectRight;
    private boolean mIsScrolling = false;
    private float mMinZoom = 0f;
    private float mZoom = 0f;
    private float mMaxZoom = 1f;

    public ZoomableScrollView(Context context) {
        super(context);
        initPeriodicTableView(context);
    }

    public ZoomableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPeriodicTableView(context);
    }

    public ZoomableScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPeriodicTableView(context);
    }

    private void initPeriodicTableView(Context context) {
        setWillNotDraw(false);

        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(true);

        TypedArray styledAttributes = context.obtainStyledAttributes(R.styleable.ZoomableScrollView);
        initializeScrollbars(styledAttributes);
        styledAttributes.recycle();

        mOverScroller = new OverScroller(context);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, this);

        mEdgeEffectTop = new EdgeEffectCompat(context);
        mEdgeEffectBottom = new EdgeEffectCompat(context);
        mEdgeEffectLeft = new EdgeEffectCompat(context);
        mEdgeEffectRight = new EdgeEffectCompat(context);
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

        if(getScaledWidth() > getWidth()) {
            if (getScrollX() == getMinimalScrollX()) {
                mEdgeEffectLeft.onPull(Math.abs(distanceX) / getWidth());
            } else if (getScrollX() == getMaximalScrollX()) {
                mEdgeEffectRight.onPull(Math.abs(distanceX) / getWidth());
            }
        }

        if(getScaledHeight() > getHeight()) {
            if (getScrollY() == getMinimalScrollY()) {
                mEdgeEffectTop.onPull(Math.abs(distanceY) / getHeight());
            } else if (getScrollY() == getMaximalScrollY()) {
                mEdgeEffectBottom.onPull(Math.abs(distanceY) / getHeight());
            }
        }

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

    public void zoomTo(int x, int y, float zoom) {
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
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        boolean isMinZoom = mMinZoom == mZoom;

        mMinZoom = getMinimalZoom();

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

    @Override
    protected void onDraw(Canvas canvas) {
        final int overScrollMode = ViewCompat.getOverScrollMode(this);
        if (overScrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
                overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS) {
            if (!mEdgeEffectLeft.isFinished()) {
                final int restoreCount = canvas.save();
                final int height = getHeight() - getPaddingTop() - getPaddingBottom();
                final int width = getWidth();

                canvas.rotate(270);
                canvas.translate(-height + getPaddingTop() - getScrollY(), getMinimalScrollX());
                mEdgeEffectLeft.setSize(height, width);
                mEdgeEffectLeft.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (!mEdgeEffectRight.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth();
                final int height = getHeight() - getPaddingTop() - getPaddingBottom();

                canvas.rotate(90);
                canvas.translate(-getPaddingTop() + getScrollY(), -getMaximalScrollX() - getWidth());
                mEdgeEffectRight.setSize(height, width);
                mEdgeEffectRight.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (!mEdgeEffectTop.isFinished()) {
                final int restoreCount = canvas.save();
                final int height = getWidth() - getPaddingLeft() - getPaddingRight();
                final int width = getHeight();

                canvas.translate(getPaddingLeft() + getScrollX(), getMinimalScrollY());
                mEdgeEffectTop.setSize(height, width);
                mEdgeEffectTop.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (!mEdgeEffectBottom.isFinished()) {
                final int restoreCount = canvas.save();
                final int height = getWidth() - getPaddingLeft() - getPaddingRight();
                final int width = getHeight();

                canvas.rotate(180);
                canvas.translate(-getWidth() + getPaddingLeft() - getScrollX(),
                        -getMaximalScrollY() - getHeight());
                mEdgeEffectBottom.setSize(height, width);
                mEdgeEffectBottom.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
        } else {
            mEdgeEffectLeft.finish();
            mEdgeEffectRight.finish();
            mEdgeEffectTop.finish();
            mEdgeEffectBottom.finish();
        }

        if (!mEdgeEffectLeft.isFinished() || !mEdgeEffectRight.isFinished() ||
                !mEdgeEffectTop.isFinished() || !mEdgeEffectBottom.isFinished()) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
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

    protected int getScaledWidth() {
        return Math.round(getMeasuredWidth() * mZoom);
    }

    protected int getScaledHeight() {
        return Math.round(getMeasuredHeight() * mZoom);
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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void computeScroll() {
        if(mOverScroller.computeScrollOffset()) {
            scrollTo(mOverScroller.getCurrX(), mOverScroller.getCurrY());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                boolean needsInvalidate = false;

                if(getScaledWidth() > getWidth()) {
                    if (getScrollX() == getMinimalScrollX() && mEdgeEffectLeft.isFinished()) {
                        mEdgeEffectLeft.onAbsorb((int)mOverScroller.getCurrVelocity());
                        needsInvalidate = true;
                    } else if (getScrollX() == getMaximalScrollX() &&
                            mEdgeEffectRight.isFinished()) {
                        mEdgeEffectRight.onAbsorb((int)mOverScroller.getCurrVelocity());
                        needsInvalidate = true;
                    }
                }

                if(getScaledHeight() > getHeight()) {
                    if(getScrollY() == getMinimalScrollY() && mEdgeEffectTop.isFinished()) {
                        mEdgeEffectTop.onAbsorb((int)mOverScroller.getCurrVelocity());
                        needsInvalidate = true;
                    }
                    else if(getScrollY() == getMaximalScrollY() && mEdgeEffectBottom.isFinished()) {
                        mEdgeEffectBottom.onAbsorb((int)mOverScroller.getCurrVelocity());
                        needsInvalidate = true;
                    }
                }

                if (needsInvalidate) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            }
        }
    }

    public float getMinimalZoom() {
        return Math.min(getWidth() / getMeasuredWidth(), getHeight() / getMeasuredHeight());
    }

    public float getZoom() {
        return mZoom;
    }

    public float getMaximalZoom() {
        return mMaxZoom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mScaleDetector.onTouchEvent(event) && !mScaleDetector.isInProgress()) {
            if (!mGestureDetector.onTouchEvent(event) && !mIsScrolling) {
                super.onTouchEvent(event);
            }
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsScrolling = false;
                if(mEdgeEffectLeft.onRelease() || mEdgeEffectRight.onRelease() ||
                        mEdgeEffectTop.onRelease() || mEdgeEffectBottom.onRelease()) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                break;
        }

        return true;
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
}
