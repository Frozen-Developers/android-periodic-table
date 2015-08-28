package com.frozendevs.periodictable.content;

import android.content.Context;

public abstract class AsyncTaskLoader<Result> extends
        android.support.v4.content.AsyncTaskLoader<Result> {
    private Result mResult;

    public AsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(Result result) {
        if (isReset() && mResult != null) {
            releaseResources(result);

            return;
        }

        Result oldResult = mResult;
        mResult = result;

        if (isStarted()) {
            super.deliverResult(result);
        }

        if (oldResult != null && oldResult != result) {
            releaseResources(oldResult);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }

        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mResult != null) {
            releaseResources(mResult);

            mResult = null;
        }
    }

    @Override
    public void onCanceled(Result result) {
        super.onCanceled(result);

        releaseResources(result);
    }

    public void releaseResources(Result result) {
    }
}
