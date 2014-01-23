package com.frozendevs.periodic.table.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.frozendevs.periodic.table.R;

public class ZoomView extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener,
        ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    private float MAX_ZOOM = 1.0f;
    private float MIN_ZOOM = 0;
    private float zoom;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private boolean isScrolling = false;
    private int gravity = Gravity.NO_GRAVITY;

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
        setWillNotDraw(false);
        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(true);
        TypedArray a = context.obtainStyledAttributes(R.styleable.ZoomView);
        initializeScrollbars(a);
        a.recycle();

        getViewTreeObserver().addOnGlobalLayoutListener(this);
        scaleDetector = new ScaleGestureDetector(context, this);
        gestureDetector = new GestureDetector(context, this);
    }

    @Override
    public void onGlobalLayout() {
        LayoutParams layoutParams = (LayoutParams)getLayoutParams();
        if(layoutParams != null)
            gravity = layoutParams.gravity;

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

        zoom = clamp(MIN_ZOOM, zoom * detector.getScaleFactor(), MAX_ZOOM);

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

        int x = (int)clamp(getLeftOffset(), getScrollX() + distanceX, getMaximalScrollX());
        int y = (int)clamp(getTopOffset(), getScrollY() + distanceY, getMaximalScrollY());

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
        if(gravity == Gravity.CENTER_HORIZONTAL || gravity == Gravity.CENTER) {
            int scaledWidth = Math.round((float)getMeasuredWidth() * zoom);

            return ((getMeasuredWidth() - scaledWidth) / 2) -
                    (scaledWidth < getWidth() ? (getWidth() - scaledWidth) / 2 : 0);
        }

        return 0;
    }

    private int getTopOffset() {
        if(gravity == Gravity.CENTER_VERTICAL || gravity == Gravity.CENTER) {
            int scaledHeight = Math.round((float)getMeasuredHeight() * zoom);

            return ((getMeasuredHeight() - scaledHeight) / 2) -
                    (scaledHeight < getHeight() ? (getHeight() - scaledHeight) / 2 : 0);
        }

        return 0;
    }

    private int getMaximalScrollX() {
        return Math.round((float)getMeasuredWidth() * zoom) - getWidth() + getLeftOffset();
    }

    private int getMaximalScrollY() {
        return Math.round((float)getMeasuredHeight() * zoom) - getHeight() + getTopOffset();
    }

    private float clamp(float min, float val, float max) {
        return Math.max(min, Math.min(val, max));
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        return getWidth();
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return getScrollX() - getLeftOffset();
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return Math.round((float)getMeasuredWidth() * zoom);
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return getHeight();
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return getScrollY() - getTopOffset();
    }

    @Override
    protected int computeVerticalScrollRange() {
        return Math.round((float)getMeasuredHeight() * zoom);
    }
}
