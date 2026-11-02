package com.fluxo.tarefas.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class Ui {

    private Ui() {
    }

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int blend(int a, int b, float f) {
        float t = Math.max(0f, Math.min(1f, f));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000
                | (Math.round(ar + (br - ar) * t) << 16)
                | (Math.round(ag + (bg - ag) * t) << 8)
                | Math.round(ab + (bb - ab) * t);
    }

    public static boolean isDark(int color) {
        double lum = 0.299 * ((color >> 16) & 0xFF) + 0.587 * ((color >> 8) & 0xFF) + 0.114 * (color & 0xFF);
        return lum < 150;
    }

    // ---------------------------------------------------------------- shapes

    public static GradientDrawable shape(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable shapeStroke(Context c, int color, float radiusDp,
                                               int strokeColor, float strokeDp) {
        GradientDrawable g = shape(c, color, radiusDp);
        if (strokeDp > 0) g.setStroke(dp(c, strokeDp), strokeColor);
        return g;
    }

    public static GradientDrawable topRounded(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        float r = dp(c, radiusDp);
        g.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return g;
    }

    public static Drawable rippleCircle(Context c, int bgColor, int rippleColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(bgColor);
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), bg, null);
    }

    public static Drawable rippleBg(Context c, int bgColor, float radiusDp, int rippleColor) {
        GradientDrawable bg = shape(c, bgColor, radiusDp);
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), bg, null);
    }

    public static Drawable rippleBgStroke(Context c, int bgColor, float radiusDp,
                                          int strokeColor, float strokeDp, int rippleColor) {
        GradientDrawable bg = shapeStroke(c, bgColor, radiusDp, strokeColor, strokeDp);
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), bg, null);
    }

    public static void ripple(View v, Context c, int bgColor, float radiusDp, int rippleColor) {
        v.setBackground(rippleBg(c, bgColor, radiusDp, rippleColor));
        v.setClickable(true);
        v.setFocusable(true);
    }

    // ---------------------------------------------------------------- text

    public static TextView text(Context c, String value, float sizeSp, int color, boolean medium) {
        TextView tv = new TextView(c);
        tv.setText(value);
        tv.setTextSize(sizeSp);
        tv.setTextColor(color);
        tv.setIncludeFontPadding(false);
        if (medium) tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return tv;
    }

    public static TextView headline(Context c, String value, float sizeSp, int color) {
        TextView tv = text(c, value, sizeSp, color, true);
        tv.setLetterSpacing(-0.02f);
        return tv;
    }

    public static TextView label(Context c, String value, float sizeSp, int color) {
        TextView tv = text(c, value, sizeSp, color, true);
        tv.setLetterSpacing(0.06f);
        return tv;
    }

    public static ImageView image(Context c, int res, int tint, int sizeDp) {
        ImageView iv = new ImageView(c);
        iv.setImageResource(res);
        iv.setColorFilter(tint);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp));
        iv.setLayoutParams(lp);
        return iv;
    }

    public static ImageButton iconButton(Context c, int res, int tint, int rippleColor) {
        ImageButton b = new ImageButton(c);
        b.setImageResource(res);
        b.setColorFilter(tint);
        b.setScaleType(ImageView.ScaleType.FIT_CENTER);
        b.setPadding(dp(c, 11), dp(c, 11), dp(c, 11), dp(c, 11));
        b.setBackground(rippleCircle(c, Color.TRANSPARENT, rippleColor));
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(c, 46), dp(c, 46)));
        return b;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static View spacer(Context c, int heightDp) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(c, heightDp)));
        return v;
    }

    public static View divider(Context c, int color) {
        View v = new View(c);
        v.setBackgroundColor(color);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(c, 1))));
        return v;
    }

    public static void margins(View v, Context c, int l, int t, int r, int b) {
        ViewGroup.LayoutParams raw = v.getLayoutParams();
        int w = raw != null ? raw.width : ViewGroup.LayoutParams.WRAP_CONTENT;
        int h = raw != null ? raw.height : ViewGroup.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.setMargins(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
        v.setLayoutParams(lp);
    }

    // ---------------------------------------------------------------- chips

    public static TextView chip(Context c, String label, Palette p) {
        TextView tv = text(c, label, 13f, p.onSurfaceVariant, true);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(c, 16), dp(c, 9), dp(c, 16), dp(c, 9));
        tv.setMinHeight(dp(c, 36));
        tv.setClickable(true);
        tv.setFocusable(true);
        styleChip(tv, false, p);
        return tv;
    }

    public static void styleChip(TextView tv, boolean selected, Palette p) {
        Context c = tv.getContext();
        if (selected) {
            tv.setTextColor(p.onSecondaryContainer);
            tv.setBackground(rippleBg(c, p.secondaryContainer, 999, withAlpha(p.onSecondaryContainer, 0x33)));
        } else {
            tv.setTextColor(p.onSurfaceVariant);
            tv.setBackground(rippleBgStroke(c, Color.TRANSPARENT, 999, p.outline, 1,
                    withAlpha(p.onSurfaceVariant, 0x22)));
        }
    }

    public static int[] categoryColors(Palette p, String category) {
        int hash = Math.abs(category.hashCode());
        switch (hash % 3) {
            case 1:
                return new int[]{p.secondaryContainer, p.onSecondaryContainer};
            case 2:
                return new int[]{p.tertiaryContainer, p.onTertiaryContainer};
            default:
                return new int[]{p.primaryContainer, p.onPrimaryContainer};
        }
    }

    // ---------------------------------------------------------------- animations

    public static void animateIn(View v, long delay) {
        v.setAlpha(0f);
        v.setTranslationY(dp(v.getContext(), 14));
        v.animate().alpha(1f).translationY(0f)
                .setStartDelay(delay).setDuration(300)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    public static void shake(View v) {
        float d = dp(v.getContext(), 8);
        android.animation.ObjectAnimator.ofFloat(v, "translationX", 0, d, -d, d * 0.7f, -d * 0.7f, 0)
                .setDuration(340).start();
    }

    public static void hideKeyboard(Activity a) {
        View focus = a.getCurrentFocus();
        if (focus != null) {
            InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(View v) {
        v.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) v.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }, 180);
    }

    // ---------------------------------------------------------------- snackbar

    public static void snackbar(Activity a, Palette p, String message, String actionLabel,
                                Runnable action, int bottomMarginDp) {
        ViewGroup root = a.findViewById(android.R.id.content);
        if (root == null) return;
        View old = root.findViewWithTag("fluxo_snack");
        if (old != null) root.removeView(old);

        LinearLayout bar = row(a);
        bar.setTag("fluxo_snack");
        bar.setBackground(shape(a, p.inverseSurface, 16));
        bar.setElevation(dp(a, 8));
        bar.setPadding(dp(a, 16), dp(a, 12), dp(a, 8), dp(a, 12));

        TextView msg = text(a, message, 13.5f, p.inverseOnSurface, false);
        LinearLayout.LayoutParams msgLp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        msg.setLayoutParams(msgLp);
        bar.addView(msg);

        if (actionLabel != null) {
            TextView act = text(a, actionLabel, 13.5f, p.inversePrimary, true);
            act.setPadding(dp(a, 14), dp(a, 8), dp(a, 14), dp(a, 8));
            act.setBackground(rippleBg(a, Color.TRANSPARENT, 999, withAlpha(p.inversePrimary, 0x33)));
            bar.addView(act);
            act.setOnClickListener(v -> {
                dismiss(root, bar);
                if (action != null) action.run();
            });
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM;
        lp.setMargins(dp(a, 16), 0, dp(a, 16), dp(a, bottomMarginDp));
        root.addView(bar, lp);

        bar.setAlpha(0f);
        bar.setTranslationY(dp(a, 24));
        bar.animate().alpha(1f).translationY(0f).setDuration(220)
                .setInterpolator(new DecelerateInterpolator()).start();

        Handler h = new Handler(Looper.getMainLooper());
        h.postDelayed(() -> dismiss(root, bar), 4200);
    }

    private static void dismiss(ViewGroup root, View bar) {
        if (bar.getParent() == null) return;
        bar.animate().alpha(0f).translationY(dp(bar.getContext(), 18)).setDuration(170)
                .withEndAction(() -> root.removeView(bar)).start();
    }

    // ---------------------------------------------------------------- sheet

    public static final class Item {
        public final int icon;
        public final String label;
        public final int color;
        public final Runnable action;

        public Item(int icon, String label, int color, Runnable action) {
            this.icon = icon;
            this.label = label;
            this.color = color;
            this.action = action;
        }
    }

    public static void sheet(Activity a, Palette p, String title, String subtitle, List<Item> items) {
        Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setCanceledOnTouchOutside(true);

        LinearLayout content = column(a);
        content.setBackground(topRounded(a, p.surfaceContainerHigh, 28));
        content.setPadding(dp(a, 8), dp(a, 8), dp(a, 8), dp(a, 12));

        View handle = new View(a);
        handle.setBackground(shape(a, p.outlineVariant, 999));
        LinearLayout.LayoutParams handleLp = new LinearLayout.LayoutParams(dp(a, 34), dp(a, 4));
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        handleLp.topMargin = dp(a, 6);
        handleLp.bottomMargin = dp(a, 8);
        content.addView(handle, handleLp);

        if (title != null) {
            TextView t = text(a, title, 19f, p.onSurface, true);
            t.setLetterSpacing(-0.01f);
            t.setPadding(dp(a, 20), dp(a, 8), dp(a, 20), dp(a, 2));
            t.setMaxLines(1);
            t.setEllipsize(android.text.TextUtils.TruncateAt.END);
            content.addView(t);
        }
        if (subtitle != null) {
            TextView s = text(a, subtitle, 12.5f, p.onSurfaceVariant, false);
            s.setPadding(dp(a, 20), dp(a, 2), dp(a, 20), dp(a, 10));
            content.addView(s);
        }

        for (Item item : items) {
            LinearLayout row = row(a);
            row.setPadding(dp(a, 14), 0, dp(a, 14), 0);
            row.setMinimumHeight(dp(a, 56));
            row.setBackground(rippleBg(a, Color.TRANSPARENT, 14, withAlpha(p.onSurface, 0x22)));
            ImageView icon = image(a, item.icon, item.color, 23);
            LinearLayout.LayoutParams iconLp = (LinearLayout.LayoutParams) icon.getLayoutParams();
            iconLp.setMargins(dp(a, 6), 0, 0, 0);
            row.addView(icon);
            TextView label = text(a, item.label, 15f, item.color, false);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            labelLp.setMarginStart(dp(a, 16));
            label.setLayoutParams(labelLp);
            row.addView(label);
            row.setOnClickListener(v -> {
                d.dismiss();
                if (item.action != null) item.action.run();
            });
            content.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 56)));
        }

        d.setContentView(content);
        Theme.applyDialogWindow(a, d, p, p.surfaceContainerHigh);
        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            w.setDimAmount(0.45f);
        }
        d.show();
        if (w != null) {
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        content.setTranslationY(dp(a, 80));
        content.animate().translationY(0f).setDuration(280)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    // ---------------------------------------------------------------- dialogs

    public static void confirm(Activity a, Palette p, String title, String message,
                               String positiveLabel, boolean destructive, Runnable onOk) {
        Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout frame = new FrameLayout(a);
        frame.setPadding(dp(a, 26), dp(a, 26), dp(a, 26), dp(a, 26));

        LinearLayout card = column(a);
        card.setBackground(shape(a, p.surfaceContainerHigh, 28));
        card.setPadding(dp(a, 24), dp(a, 22), dp(a, 24), dp(a, 16));

        TextView t = text(a, title, 20f, p.onSurface, true);
        t.setLetterSpacing(-0.01f);
        card.addView(t);

        TextView m = text(a, message, 14f, p.onSurfaceVariant, false);
        m.setLineSpacing(dp(a, 3), 1f);
        LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLp.topMargin = dp(a, 10);
        card.addView(m, mLp);

        LinearLayout buttons = row(a);
        buttons.setGravity(Gravity.END);
        LinearLayout.LayoutParams buttonsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonsLp.topMargin = dp(a, 22);
        card.addView(buttons, buttonsLp);

        TextView cancel = text(a, "Cancelar", 14f, p.primary, true);
        cancel.setPadding(dp(a, 14), dp(a, 10), dp(a, 14), dp(a, 10));
        cancel.setBackground(rippleBg(a, Color.TRANSPARENT, 999, withAlpha(p.primary, 0x33)));
        buttons.addView(cancel);

        int confirmBg = destructive ? p.error : p.primary;
        int confirmFg = destructive ? p.onError : p.onPrimary;
        TextView ok = text(a, positiveLabel, 14f, confirmFg, true);
        ok.setPadding(dp(a, 18), dp(a, 10), dp(a, 18), dp(a, 10));
        ok.setBackground(rippleBg(a, confirmBg, 999, withAlpha(confirmFg, 0x33)));
        LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        okLp.setMarginStart(dp(a, 6));
        buttons.addView(ok, okLp);

        frame.addView(card, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        d.setContentView(frame);
        Theme.applyDialogWindow(a, d, p, p.surfaceContainer);
        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.CENTER);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            w.setDimAmount(0.5f);
        }
        cancel.setOnClickListener(v -> d.dismiss());
        ok.setOnClickListener(v -> {
            d.dismiss();
            if (onOk != null) onOk.run();
        });
        d.show();
        card.setScaleX(0.92f);
        card.setScaleY(0.92f);
        card.setAlpha(0f);
        card.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(200)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    public static void prompt(Activity a, Palette p, String title, String hint,
                              String initial, Consumer<String> onOk) {
        prompt(a, p, title, hint, initial, false, onOk);
    }

    public static void prompt(Activity a, Palette p, String title, String hint,
                              String initial, boolean allowEmpty, Consumer<String> onOk) {
        Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout frame = new FrameLayout(a);
        frame.setPadding(dp(a, 26), dp(a, 26), dp(a, 26), dp(a, 26));

        LinearLayout card = column(a);
        card.setBackground(shape(a, p.surfaceContainerHigh, 28));
        card.setPadding(dp(a, 24), dp(a, 22), dp(a, 24), dp(a, 16));

        TextView t = text(a, title, 20f, p.onSurface, true);
        t.setLetterSpacing(-0.01f);
        card.addView(t);

        EditText input = new EditText(a);
        input.setText(initial == null ? "" : initial);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setTextSize(15f);
        input.setTextColor(p.onSurface);
        input.setHintTextColor(p.onSurfaceVariant);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(dp(a, 14), dp(a, 12), dp(a, 14), dp(a, 12));
        input.setBackground(shapeStroke(a, p.surfaceContainerHighest, 14, p.outline, 1));
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputLp.topMargin = dp(a, 16);
        card.addView(input, inputLp);

        LinearLayout buttons = row(a);
        buttons.setGravity(Gravity.END);
        LinearLayout.LayoutParams buttonsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonsLp.topMargin = dp(a, 20);
        card.addView(buttons, buttonsLp);

        TextView cancel = text(a, "Cancelar", 14f, p.primary, true);
        cancel.setPadding(dp(a, 14), dp(a, 10), dp(a, 14), dp(a, 10));
        cancel.setBackground(rippleBg(a, Color.TRANSPARENT, 999, withAlpha(p.primary, 0x33)));
        buttons.addView(cancel);

        TextView ok = text(a, "Salvar", 14f, p.onPrimary, true);
        ok.setPadding(dp(a, 18), dp(a, 10), dp(a, 18), dp(a, 10));
        ok.setBackground(rippleBg(a, p.primary, 999, withAlpha(p.onPrimary, 0x33)));
        LinearLayout.LayoutParams okLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        okLp.setMarginStart(dp(a, 6));
        buttons.addView(ok, okLp);

        frame.addView(card);
        d.setContentView(frame);
        Theme.applyDialogWindow(a, d, p, p.surfaceContainer);
        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.CENTER);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            w.setDimAmount(0.5f);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        cancel.setOnClickListener(v -> d.dismiss());
        ok.setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty() && !allowEmpty) {
                shake(input);
                return;
            }
            d.dismiss();
            if (onOk != null) onOk.accept(value);
        });
        d.show();
        input.requestFocus();
        input.setSelection(input.getText().length());
        showKeyboard(input);
        card.setAlpha(0f);
        card.setTranslationY(dp(a, 12));
        card.animate().alpha(1f).translationY(0f).setDuration(200)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    // ---------------------------------------------------------------- color picker

    public static void colorPicker(Activity a, Palette p, int initial, Consumer<Integer> onPick) {
        Dialog d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout frame = new FrameLayout(a);
        frame.setPadding(dp(a, 22), dp(a, 22), dp(a, 22), dp(a, 22));

        LinearLayout card = column(a);
        card.setBackground(shape(a, p.surfaceContainerHigh, 28));
        card.setPadding(dp(a, 24), dp(a, 22), dp(a, 24), dp(a, 16));

        TextView title = text(a, "Personalizar cor", 20f, p.onSurface, true);
        title.setLetterSpacing(-0.01f);
        card.addView(title);

        TextView hint = text(a, "Escolha a cor base. O Fluxo gera uma paleta completa a partir dela.",
                12.5f, p.onSurfaceVariant, false);
        hint.setLineSpacing(dp(a, 2), 1f);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hintLp.topMargin = dp(a, 6);
        card.addView(hint, hintLp);

        LinearLayout preview = row(a);
        preview.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        previewLp.topMargin = dp(a, 16);
        card.addView(preview, previewLp);

        View[] dots = new View[4];
        for (int i = 0; i < 4; i++) {
            dots[i] = new View(a);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    dp(a, 44), dp(a, 44));
            lp.setMarginStart(i == 0 ? 0 : dp(a, 10));
            preview.addView(dots[i], lp);
        }

        TextView hex = text(a, "", 13f, p.primary, true);
        hex.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hexLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hexLp.topMargin = dp(a, 10);
        card.addView(hex, hexLp);

        TextView hueLabel = text(a, "MATIZ", 11f, p.onSurfaceVariant, true);
        hueLabel.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams hueLabelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hueLabelLp.topMargin = dp(a, 18);
        card.addView(hueLabel, hueLabelLp);

        SeekBar hueBar = new SeekBar(a);
        hueBar.setMax(360);
        GradientDrawable hueGradient = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF,
                        0xFF0000FF, 0xFFFF00FF, 0xFFFF0000});
        hueGradient.setCornerRadius(dp(a, 6));
        hueBar.setProgressDrawable(hueGradient);
        hueBar.setThumbTintList(ColorStateList.valueOf(p.onSurface));
        hueBar.setPadding(dp(a, 8), dp(a, 8), dp(a, 8), dp(a, 8));
        card.addView(hueBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 44)));

        TextView satLabel = text(a, "INTENSIDADE", 11f, p.onSurfaceVariant, true);
        satLabel.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams satLabelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        satLabelLp.topMargin = dp(a, 14);
        card.addView(satLabel, satLabelLp);

        SeekBar satBar = new SeekBar(a);
        satBar.setMax(90);
        satBar.setThumbTintList(ColorStateList.valueOf(p.onSurface));
        satBar.setPadding(dp(a, 8), dp(a, 8), dp(a, 8), dp(a, 8));
        card.addView(satBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 44)));

        final float[] state = new float[]{0f, 70f};
        float[] initialHsv = new float[3];
        Color.colorToHSV(initial, initialHsv);
        state[0] = initialHsv[0];
        state[1] = Math.max(10f, Math.min(100f, initialHsv[1] * 100f));
        hueBar.setProgress(Math.round(state[0]));
        satBar.setProgress(Math.round(state[1]) - 10);

        final int[] seed = {initial};
        Runnable update = () -> {
            seed[0] = Color.HSVToColor(new float[]{
                    state[0], Math.max(0.12f, state[1] / 100f), 0.85f});
            Palette previewPalette = Palette.fromSeed(seed[0], p.dark);
            int[] colors = {previewPalette.primary, previewPalette.secondary,
                    previewPalette.tertiary, previewPalette.primaryContainer};
            for (int i = 0; i < 4; i++) {
                dots[i].setBackground(shape(a, colors[i], 999));
            }
            hex.setText(String.format(Locale.US, "#%06X", 0xFFFFFF & seed[0]));
            GradientDrawable satGradient = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{0xFFBDBDBD, seed[0]});
            satGradient.setCornerRadius(dp(a, 6));
            satBar.setProgressDrawable(satGradient);
        };

        hueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                state[0] = value;
                update.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
        satBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                state[1] = value + 10f;
                update.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
        update.run();

        LinearLayout buttons = row(a);
        buttons.setGravity(Gravity.END);
        LinearLayout.LayoutParams buttonsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonsLp.topMargin = dp(a, 20);
        card.addView(buttons, buttonsLp);

        TextView random = text(a, "Aleatória", 13f, p.primary, true);
        random.setPadding(dp(a, 14), dp(a, 10), dp(a, 14), dp(a, 10));
        random.setBackground(rippleBg(a, Color.TRANSPARENT, 999,
                withAlpha(p.primary, 0x33)));
        random.setOnClickListener(v -> {
            state[0] = (float) (Math.random() * 360);
            state[1] = 55f + (float) (Math.random() * 45);
            hueBar.setProgress(Math.round(state[0]));
            satBar.setProgress(Math.round(state[1]) - 10);
            update.run();
        });
        buttons.addView(random);

        View buttonsSpacer = new View(a);
        buttons.addView(buttonsSpacer, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView cancel = text(a, "Cancelar", 14f, p.primary, true);
        cancel.setPadding(dp(a, 14), dp(a, 10), dp(a, 14), dp(a, 10));
        cancel.setBackground(rippleBg(a, Color.TRANSPARENT, 999,
                withAlpha(p.primary, 0x33)));
        buttons.addView(cancel);

        TextView apply = text(a, "Aplicar", 14f, p.onPrimary, true);
        apply.setPadding(dp(a, 18), dp(a, 10), dp(a, 18), dp(a, 10));
        apply.setBackground(rippleBg(a, p.primary, 999,
                withAlpha(p.onPrimary, 0x33)));
        LinearLayout.LayoutParams applyLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        applyLp.setMarginStart(dp(a, 6));
        buttons.addView(apply, applyLp);

        frame.addView(card);
        d.setContentView(frame);
        Theme.applyDialogWindow(a, d, p, p.surfaceContainer);
        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.CENTER);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            w.setDimAmount(0.5f);
        }
        cancel.setOnClickListener(v -> d.dismiss());
        apply.setOnClickListener(v -> {
            d.dismiss();
            if (onPick != null) onPick.accept(seed[0]);
        });
        d.show();
        card.setAlpha(0f);
        card.setTranslationY(dp(a, 12));
        card.animate().alpha(1f).translationY(0f).setDuration(200)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    public static void tintPlatformDialog(Dialog d, Palette p) {
        if (!(d instanceof android.app.AlertDialog)) return;
        android.app.AlertDialog ad = (android.app.AlertDialog) d;
        d.setOnShowListener(x -> {
            ad.getButton(Dialog.BUTTON_POSITIVE).setTextColor(p.primary);
            ad.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(p.onSurfaceVariant);
            ad.getButton(Dialog.BUTTON_NEUTRAL).setTextColor(p.primary);
        });
    }

    public static void addRowIcon(LinearLayout row, Context c, int icon, int tint) {
        ImageView iv = image(c, icon, tint, 22);
        row.addView(iv);
    }
}
