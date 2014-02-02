package com.frozendevs.periodictable.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * NonSwipeableViewPager is a ViewPager with disabled swipe touch events.
 */
public class NonSwipeableViewPager extends ViewPager {

    public NonSwipeableViewPager(Context context) {
        super(context);
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent (MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {
        return false;
    }
}
