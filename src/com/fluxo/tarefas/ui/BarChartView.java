package com.fluxo.tarefas.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class BarChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int[] values = new int[7];
    private String[] labels = new String[]{"", "", "", "", "", "", ""};
    private Palette palette;
    private float reveal = 1f;
    private ValueAnimator animator;

    public BarChartView(Context c, AttributeSet attrs) {
        super(c, attrs);
        textPaint.setTextAlign(Paint.Align.CENTER);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(1));
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
    }

    public void setData(int[] values, String[] labels, Palette p, boolean animate) {
        this.values = values;
        this.labels = labels;
        this.palette = p;
        if (animator != null) animator.cancel();
        if (!animate) {
            reveal = 1f;
            invalidate();
            return;
        }
        reveal = 0f;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(620);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            reveal = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (palette == null) return;
        float w = getWidth();
        float h = getHeight();
        float bottom = h - dp(24);
        float top = dp(24);
        float slot = w / 7f;
        float barW = Math.min(slot * 0.46f, dp(28));
        int max = 1;
        for (int v : values) max = Math.max(max, v);

        linePaint.setColor(withAlpha(palette.outlineVariant, 150));
        canvas.drawLine(0, bottom, w, bottom, linePaint);

        Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(dp(11));
        valuePaint.setColor(palette.onSurfaceVariant);

        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(dp(10.5f));
        labelPaint.setColor(palette.onSurfaceVariant);

        for (int i = 0; i < 7; i++) {
            float center = slot * i + slot / 2f;
            int value = i < values.length ? values[i] : 0;
            float bh = (bottom - top) * ((float) value / max) * reveal;
            barPaint.setColor(i == 6 ? palette.tertiary : palette.primary);
            if (bh > dp(1)) {
                canvas.drawRoundRect(center - barW / 2f, bottom - bh,
                        center + barW / 2f, bottom, barW / 2f, barW / 2f, barPaint);
            }
            if (value > 0) {
                valuePaint.setColor(i == 6 ? palette.tertiary : palette.onSurfaceVariant);
                canvas.drawText(String.valueOf(value), center, bottom - bh - dp(8), valuePaint);
            }
            if (i < labels.length && labels[i] != null && !labels[i].isEmpty()) {
                labelPaint.setColor(i == 6 ? palette.primary : palette.onSurfaceVariant);
                labelPaint.setTypeface(Typeface.create(
                        i == 6 ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
                canvas.drawText(labels[i], center, bottom + dp(16), labelPaint);
            }
        }
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
