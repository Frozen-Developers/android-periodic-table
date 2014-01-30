package com.frozendevs.periodic.table.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.frozendevs.periodic.table.R;

public class ZoomView extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private float mMaxZoom = 1.0f;
    private float mMinZoom = 0;
    private float mZoom;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;
    private boolean mIsScrolling = false;

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
        mScaleDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, this);
    }

    @Override
    public void onGlobalLayout() {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        mZoom = mMinZoom = Math.min(Math.min((float)getWidth() / getMeasuredWidth(),
                (float)getHeight() / getMeasuredHeight()), mMaxZoom);

        for(int i = 0; i < getChildCount(); i++)
            scaleView(getChildAt(i));

        scrollTo(getMinimalScrollX(), getMinimalScrollY());
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        zoomTo((int) detector.getFocusX(), (int) detector.getFocusY(),
                clamp(mMinZoom, mZoom * detector.getScaleFactor(), mMaxZoom));

        return mZoom == mMinZoom || mZoom == mMaxZoom;
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
            case MotionEvent.ACTION_UP:
                mIsScrolling = false;
                break;
        }

        return true;
    }

    private void scaleView(View view) {
        view.setScaleX(mZoom);
        view.setScaleY(mZoom);
        int left = view.getLeft();
        int top = view.getTop();
        view.layout(left, top, left + view.getMeasuredWidth(), top + view.getMeasuredHeight());
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

        scrollTo(getScrollX() + (int) distanceX, getScrollY() + (int) distanceY);

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
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

    public float getMinimalZoom() {
        return mMinZoom;
    }

    public float getMaximalZoom() {
        return mMaxZoom;
    }

    public float getZoom() {
        return mZoom;
    }

    private int getScaledWidth(float zoom) {
        return Math.round((float) getMeasuredWidth() * zoom);
    }

    private int getScaledWidth() {
        return getScaledWidth(mZoom);
    }

    private int getScaledHeight(float zoom) {
        return Math.round((float) getMeasuredHeight() * zoom);
    }

    private int getScaledHeight() {
        return getScaledHeight(mZoom);
    }

    public void zoomTo(int x, int y, float zoom) {
        if(mZoom != zoom) {
            // TODO add scroll to [x, y]

            mZoom = zoom;

            scrollTo(getScrollX(), getScrollY());

            for(int i = 0; i < getChildCount(); i++)
                scaleView(getChildAt(i));
        }
    }

    @Override
    public void scrollTo (int x, int y) {
        super.scrollTo(clamp(getMinimalScrollX(), x, getMaximalScrollX()),
                clamp(getMinimalScrollY(), y, getMaximalScrollY()));
    }
}
