package com.frozendevs.periodictable;

import android.app.Application;
import android.support.v4.app.SharedElementCallback;
import android.view.View;

public class PeriodicTableApplication extends Application {
    private SharedElementCallback mSharedElementCallback;
    private View.OnAttachStateChangeListener mOnAttachStateChangeListener;

    public SharedElementCallback getSharedElementCallback() {
        return mSharedElementCallback;
    }

    public void setSharedElementCallback(SharedElementCallback callback) {
        mSharedElementCallback = callback;
    }

    public View.OnAttachStateChangeListener getOnAttachStateChangeListener() {
        return mOnAttachStateChangeListener;
    }

    public void setOnAttachStateChangeListener(View.OnAttachStateChangeListener listener) {
        this.mOnAttachStateChangeListener = listener;
    }
}
