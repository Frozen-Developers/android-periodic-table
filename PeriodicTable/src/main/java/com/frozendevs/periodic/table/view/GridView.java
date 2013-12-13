package com.frozendevs.periodic.table.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.frozendevs.periodic.table.R;

public class GridView extends android.widget.GridView {

    private static final int SENSITIVITY = 3;

    public GridView(Context context) {
        super(context);
    }

    public GridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            int size = event.getHistorySize();

            if(size > 1) {
                int deltaX = SENSITIVITY * (int)(event.getHistoricalX(size - 2) - event.getHistoricalX(size - 1));

                if((getScrollX() > 0 && deltaX < 0) ||
                        (deltaX > 0 && getScrollX() + ((View)getParent()).getWidth() < getContentWidth())) {
                    if(getScrollX() + deltaX < 0)
                        scrollBy(-1 * getScrollX(), 0);
                    else if(getScrollX() + ((View)getParent()).getWidth() + deltaX > getContentWidth())
                        scrollBy(getContentWidth() - getScrollX() - ((View)getParent()).getWidth(), 0);
                    else
                        scrollBy(deltaX, 0);
                }
            }
        }

        return super.onTouchEvent(event);
    }

    public int getContentWidth() {
        int numColumns = getResources().getInteger(R.integer.table_columns_count);

        return (numColumns * (int)getResources().getDimension(R.dimen.table_tile_size)) +
                ((numColumns - 1) * (int)getResources().getDimension(R.dimen.table_spacing));
    }
}
