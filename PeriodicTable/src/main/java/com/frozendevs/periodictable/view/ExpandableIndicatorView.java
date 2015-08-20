package com.frozendevs.periodictable.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.frozendevs.periodictable.R;

public class ExpandableIndicatorView extends ImageView {
    private StateListDrawable mGroupIndicator;

    public ExpandableIndicatorView(Context context) {
        super(context);

        initExpandableIndicatorView(context);
    }

    public ExpandableIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initExpandableIndicatorView(context);
    }

    public ExpandableIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initExpandableIndicatorView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExpandableIndicatorView(Context context, AttributeSet attrs, int defStyleAttr,
                                   int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initExpandableIndicatorView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initExpandableIndicatorView(Context context) {
        Resources.Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();

        theme.resolveAttribute(android.R.attr.expandableListViewStyle, typedValue, true);

        TypedArray typedArray = theme.obtainStyledAttributes(typedValue.resourceId,
                new int[]{android.R.attr.groupIndicator, R.attr.colorControlHighlight});

        mGroupIndicator = (StateListDrawable) typedArray.getDrawable(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int tintColor = typedArray.getColor(1, 0);

            if (tintColor != 0) {
                mGroupIndicator.setTint(tintColor);
            }
        }

        typedArray.recycle();

        setStateExpanded(false);
    }

    public void setStateExpanded(boolean expanded) {
        mGroupIndicator.setState(expanded ? new int[]{android.R.attr.state_expanded} : null);

        setImageDrawable(mGroupIndicator.getCurrent());
    }
}
