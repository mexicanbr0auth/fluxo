package com.fluxo.tarefas.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MeterView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int fillColor = 0xFF6750A4;
    private int trackColor = 0x22000000;
    private float value = 0f;

    public MeterView(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public MeterView(Context c) {
        super(c);
    }

    public void setColors(int fill, int track) {
        fillColor = fill;
        trackColor = track;
        invalidate();
    }

    public void setValue(float v) {
        value = Math.max(0f, Math.min(1f, v));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float h = getHeight();
        float w = getWidth();
        float r = h / 2f;
        paint.setColor(trackColor);
        canvas.drawRoundRect(0, 0, w, h, r, r, paint);
        if (value > 0.001f) {
            paint.setColor(fillColor);
            float fillW = Math.max(h, w * value);
            canvas.drawRoundRect(0, 0, Math.min(fillW, w), h, r, r, paint);
        }
    }
}
