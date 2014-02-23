package com.frozendevs.periodictable.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.TableItem;

public class PeriodicTableView extends View implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final int COLUMNS_COUNT = 18;
    private static final int ROWS_COUNT = 9;
    private static final int HORIZONTAL_SPACERS_COUNT = COLUMNS_COUNT - 1;
    private static final int VERTICAL_SPACERS_COUNT = ROWS_COUNT - 1;

    private View mEmptyView = null;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private TableItem[] mItems = new TableItem[0];
    private float mTileSize = 0f;

    private class LoadItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mItems = Database.getTableItems(getContext());

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            invalidate();

            updateEmptyStatus(false);
        }
    }

    public PeriodicTableView(Context context) {
        super(context);
        initPeriodicTableView(context);
    }

    public PeriodicTableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPeriodicTableView(context);
    }

    public PeriodicTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPeriodicTableView(context);
    }

    private void initPeriodicTableView(Context context) {
        getViewTreeObserver().addOnGlobalLayoutListener(this);

        new LoadItems().execute();
    }


    @Override
    public void onGlobalLayout() {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        mTileSize = Math.min((getWidth() - (HORIZONTAL_SPACERS_COUNT * getSpacing())) / COLUMNS_COUNT,
                (getHeight() - (VERTICAL_SPACERS_COUNT * getSpacing())) / ROWS_COUNT);

        invalidate();
    }

    public void setEmptyView(View view) {
        mEmptyView = view;

        updateEmptyStatus(mItems.length == 0);
    }

    private void updateEmptyStatus(boolean empty) {
        if(mEmptyView != null)
            mEmptyView.setVisibility(empty ? VISIBLE : GONE);
    }

    private TableItem getItem(int position) {
        for (TableItem item : mItems) {
            if (((item.getPeriod() - 1) * 18) + item.getGroup() - 1 == position)
                return item;
            else if (position >= 128 && position <= 142) {
                if (item.getAtomicNumber() + 71 == position)
                    return item;
            } else if (position >= 146 && position <= 160) {
                if (item.getAtomicNumber() + 57 == position)
                    return item;
            }
        }

        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float y = (getHeight() - (mTileSize * ROWS_COUNT) -
                (VERTICAL_SPACERS_COUNT * getSpacing())) / 2f;

        for(int row = 0; row < ROWS_COUNT; row++) {
            float x = (getWidth() - (mTileSize * COLUMNS_COUNT) -
                    (HORIZONTAL_SPACERS_COUNT * getSpacing())) / 2f;

            for(int column = 0; column < COLUMNS_COUNT; column++) {
                int position = (row * COLUMNS_COUNT) + column;

                TableItem item = getItem(position);

                if(item != null) {
                    drawTile(canvas, x, y, item.getName(), String.valueOf(item.getAtomicNumber()),
                            item.getStandardAtomicWeight(), item.getColor(getContext()));
                }
                else if(position == 92) {
                    drawTile(canvas, x, y, "57 - 71", "", "", getContext().getResources().getColor(
                            R.color.lanthanide_bg));
                }
                else if(position == 110) {
                    drawTile(canvas, x, y, "89 - 103", "", "", getContext().getResources().getColor(
                            R.color.actinide_bg));
                }

                x += mTileSize + getSpacing();
            }

            y += mTileSize + getSpacing();
        }
    }

    private void drawTile(Canvas canvas, float x, float y, String name, String number, String weight,
                          int color) {
        mPaint.setColor(color);
        canvas.drawRect(x, y, x + mTileSize, y + mTileSize, mPaint);
    }

    private float dpToPx(float dp) {
        return dp * (getContext().getResources().getDisplayMetrics().densityDpi / 160f);
    }

    private float getSpacing() {
        return 1f;
    }
}
