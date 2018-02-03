package com.unitslink.acde;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by gugia on 2018/2/1.
 */

public class DigitalTextView extends android.support.v7.widget.AppCompatTextView {

    public DigitalTextView(Context context) {
        super(context);
        init(context);
    }

    public DigitalTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DigitalTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/lgdr.ttf");
        setTypeface(font);
    }
}
