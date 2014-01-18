package com.frozendevs.periodic.table.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class ZoomView extends FrameLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    public ZoomView(Context context) {
        super(context);
        init();
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {

    }
}
