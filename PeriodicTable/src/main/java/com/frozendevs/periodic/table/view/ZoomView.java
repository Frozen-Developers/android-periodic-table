package com.frozendevs.periodic.table.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class ZoomView extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private float MAX_ZOOM = 1.0f;
    private float MIN_ZOOM = 0;
    private float zoom;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private boolean isScrolling = false;

    public ZoomView(Context context) {
        super(context);
        init(context);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        scaleDetector = new ScaleGestureDetector(context, this);
        gestureDetector = new GestureDetector(context, this);
    }

    @Override
    public void onGlobalLayout() {
        for(int i = 0; i < getChildCount(); i++)
            getChildAt(i).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        MIN_ZOOM = Math.min((float)getWidth() / getMeasuredWidth(),
                (float)getHeight() / getMeasuredHeight());

        zoom = MIN_ZOOM = Math.min(MIN_ZOOM, MAX_ZOOM);

        for(int i = 0; i < getChildCount(); i++)
            scaleView(getChildAt(i));

        scrollTo(getLeftOffset(), getTopOffset());
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float oldZoom = zoom;

        zoom = Math.min(MAX_ZOOM, Math.max(zoom * detector.getScaleFactor(), MIN_ZOOM));

        if(oldZoom > zoom) {
            int left = getScrollX(), top = getScrollY();

            if(getScrollX() < getLeftOffset())
                left = getLeftOffset();
            else if(getScrollX() > getMaximalScrollX())
                left = getMaximalScrollX();

            if(getScrollY() < getTopOffset())
                top = getTopOffset();
            else if(getScrollY() > getMaximalScrollY())
                top = getMaximalScrollY();

            if(left != getScrollX() || top != getScrollY())
                scrollTo(left, top);
        }

        for(int i = 0; i < getChildCount(); i++)
            scaleView(getChildAt(i));

        return zoom == MIN_ZOOM || zoom == MAX_ZOOM;
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
        if(scaleDetector.onTouchEvent(event) && !scaleDetector.isInProgress())
            if(!gestureDetector.onTouchEvent(event) && !isScrolling)
                super.dispatchTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                isScrolling = false;
                break;
        }

        return true;
    }

    private void scaleView(View view) {
        view.setScaleX(zoom);
        view.setScaleY(zoom);
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
        isScrolling = true;

        int x = (int)Math.max(getLeftOffset(), Math.min(getScrollX() + distanceX, getMaximalScrollX()));
        int y = (int)Math.max(getTopOffset(), Math.min(getScrollY() + distanceY, getMaximalScrollY()));

        scrollTo(x, y);

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private int getLeftOffset() {
        return getLeftOffset(zoom);
    }

    private int getLeftOffset(float zoom) {
        return Math.round(((float)getMeasuredWidth() * (1f - zoom)) / 2);
    }

    private int getTopOffset() {
        return getTopOffset(zoom);
    }

    private int getTopOffset(float zoom) {
        return Math.round(((float)getMeasuredHeight() * (1f - zoom)) / 2);
    }

    private int getMaximalScrollX() {
        return Math.round((float)getMeasuredWidth() * zoom) - getWidth() + getLeftOffset();
    }

    private int getMaximalScrollY() {
        return Math.round((float)getMeasuredHeight() * zoom) - getHeight() + getTopOffset();
    }
}
