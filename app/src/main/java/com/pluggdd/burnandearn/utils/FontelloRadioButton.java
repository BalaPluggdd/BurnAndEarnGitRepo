package com.pluggdd.burnandearn.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * Created by User on 18-Apr-16.
 */
public class FontelloRadioButton extends RadioButton {

    public FontelloRadioButton(Context context) {
        super(context);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "fonts/fontello.ttf");
        this.setTypeface(face);
    }

    public FontelloRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "fonts/fontello.ttf");
        this.setTypeface(face);
    }

    public FontelloRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "fonts/fontello.ttf");
        this.setTypeface(face);
    }
}
