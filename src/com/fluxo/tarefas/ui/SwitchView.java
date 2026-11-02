package com.fluxo.tarefas.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class SwitchView extends View {

    public interface OnCheckedChangeListener {
        void onChanged(boolean checked);
    }

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean checked = false;
    private float position = 0f;
    private Palette palette;
    private OnCheckedChangeListener listener;
    private ValueAnimator animator;

    public SwitchView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    public SwitchView(Context c) {
        super(c);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dp(2));
    }

    public void setPalette(Palette p) {
        palette = p;
        invalidate();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        listener = l;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean value, boolean animate) {
        boolean changed = checked != value;
        checked = value;
        if (!changed) {
            position = value ? 1f : 0f;
            invalidate();
            return;
        }
        if (animator != null) animator.cancel();
        if (!animate) {
            position = value ? 1f : 0f;
            invalidate();
            return;
        }
        animator = ValueAnimator.ofFloat(position, value ? 1f : 0f);
        animator.setDuration(220);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            position = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void setCheckedSilently(boolean value) {
        setChecked(value, false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = resolveSize((int) dp(52), widthMeasureSpec);
        int h = resolveSize((int) dp(32), heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (palette == null) return;
        float w = getWidth();
        float h = getHeight();
        float trackH = h - dp(4);
        float top = (h - trackH) / 2f;
        float radius = trackH / 2f;

        int offTrack = palette.surfaceContainerHighest;
        int onTrack = palette.primary;
        int trackColor = blend(offTrack, onTrack, position);
        trackPaint.setColor(trackColor);
        trackPaint.setAlpha(isEnabled() ? 255 : 120);
        canvas.drawRoundRect(0, top, w, top + trackH, radius, radius, trackPaint);

        if (position < 0.5f) {
            strokePaint.setColor(withAlpha(palette.outline, (int) (255 * (1 - position * 2))));
            canvas.drawRoundRect(dp(1), top + dp(1), w - dp(1), top + trackH - dp(1),
                    radius, radius, strokePaint);
        }

        float thumbR = radius - dp(4);
        float travel = w - dp(4) - thumbR * 2;
        float cx = dp(2) + thumbR + travel * position;
        float cy = h / 2f;
        thumbPaint.setColor(blend(palette.outline, palette.onPrimary, position));
        canvas.drawCircle(cx, cy, thumbR, thumbPaint);
    }

    @Override
    public boolean performClick() {
        boolean handled = super.performClick();
        setChecked(!checked, true);
        if (listener != null) listener.onChanged(checked);
        return handled;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }

    private static int blend(int a, int b, float f) {
        float t = Math.max(0f, Math.min(1f, f));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((Math.max(0, Math.min(255, alpha))) << 24);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
