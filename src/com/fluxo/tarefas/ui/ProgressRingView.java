package com.fluxo.tarefas.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ProgressRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();
    private float shown = 0f;
    private float target = 0f;
    private ValueAnimator animator;

    public ProgressRingView(Context c, AttributeSet attrs) {
        super(c, attrs);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        trackPaint.setColor(0x22000000);
        arcPaint.setColor(0xFF6750A4);
        textPaint.setColor(0xFF1D1B20);
    }

    public void setColors(int fill, int track, int text) {
        arcPaint.setColor(fill);
        trackPaint.setColor(track);
        textPaint.setColor(text);
        invalidate();
    }

    public void setProgress(float value, boolean animate) {
        target = Math.max(0f, Math.min(1f, value));
        if (animator != null) animator.cancel();
        if (!animate) {
            shown = target;
            invalidate();
            return;
        }
        animator = ValueAnimator.ofFloat(shown, target);
        animator.setDuration(650);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            shown = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;
        float stroke = dp(8);
        float pad = stroke / 2f + dp(2);
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(w, h) / 2f - pad;
        if (radius <= 0) return;

        trackPaint.setStrokeWidth(stroke);
        arcPaint.setStrokeWidth(stroke);
        canvas.drawCircle(cx, cy, radius, trackPaint);
        if (shown > 0.002f) {
            arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);
            canvas.drawArc(arcRect, -90f, 360f * shown, false, arcPaint);
        }

        textPaint.setTextSize(Math.min(radius * 0.55f, dp(42)));
        String label = Math.round(shown * 100f) + "%";
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        canvas.drawText(label, cx, cy - (fm.ascent + fm.descent) / 2f, textPaint);
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
