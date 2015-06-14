package com.frozendevs.periodictable.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

public class RecyclerView extends android.support.v7.widget.RecyclerView {
    private View mEmptyView;
    private RecyclerContextMenuInfo mContextMenuInfo;

    public class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {
        public View targetView;
        public int position;
        public long id;

        public RecyclerContextMenuInfo(View targetView, int position, long id) {
            this.targetView = targetView;
            this.position = position;
            this.id = id;
        }
    }

    private final android.support.v7.widget.RecyclerView.AdapterDataObserver observer =
            new android.support.v7.widget.RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    checkIfEmpty();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    checkIfEmpty();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    checkIfEmpty();
                }
            };

    public RecyclerView(Context context) {
        super(context);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void checkIfEmpty() {
        if (mEmptyView != null && getAdapter() != null) {
            final boolean emptyViewVisible = getAdapter().getItemCount() == 0;

            mEmptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);

            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(android.support.v7.widget.RecyclerView.Adapter adapter) {
        final RecyclerView.Adapter oldAdapter = getAdapter();

        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }

        super.setAdapter(adapter);

        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;

        checkIfEmpty();
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int position = getChildAdapterPosition(originalView);

        if (position > NO_POSITION) {
            mContextMenuInfo = new RecyclerContextMenuInfo(originalView, position,
                    getAdapter().getItemId(position));

            return super.showContextMenuForChild(originalView);
        }

        return false;
    }
}
