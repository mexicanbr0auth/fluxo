package com.fluxo.tarefas.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.fluxo.tarefas.data.Prefs;

public final class Theme {

    private Theme() {
    }

    public static boolean isDark(Context c) {
        int mode = Prefs.themeMode(c);
        if (mode == 1) return false;
        if (mode == 2) return true;
        int ui = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return ui == Configuration.UI_MODE_NIGHT_YES;
    }

    public static Palette palette(Context c) {
        boolean dark = isDark(c);
        int custom = Prefs.customSeed(c);
        int seed;
        if (Prefs.dynamicColor(c) && Build.VERSION.SDK_INT >= 31) {
            seed = dynamicSeed(c);
        } else if (custom != 0) {
            seed = custom;
        } else {
            seed = Palette.SEEDS[Prefs.seedIndex(c)];
        }
        return Palette.fromSeed(seed, dark);
    }

    private static int dynamicSeed(Context c) {
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                return c.getColor(android.R.color.system_accent1_500);
            } catch (Throwable ignored) {
            }
        }
        return Palette.SEEDS[0];
    }

    public static void setupWindow(Activity a, Palette p) {
        Window w = a.getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(p.surfaceContainer);
        w.setBackgroundDrawable(new ColorDrawable(p.surface));
        if (Build.VERSION.SDK_INT >= 29) {
            w.setStatusBarContrastEnforced(false);
            w.setNavigationBarContrastEnforced(false);
        }
        View decor = w.getDecorView();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (!p.dark) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    public static void applyDialogWindow(Activity a, android.app.Dialog d, Palette p, int navColor) {
        Window w = d.getWindow();
        if (w == null) return;
        w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        w.setNavigationBarColor(navColor);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= 29) w.setNavigationBarContrastEnforced(false);
        View decor = w.getDecorView();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (!p.dark) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        decor.setSystemUiVisibility(flags);
    }

    public static int statusBarHeight(Context c) {
        int id = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) return c.getResources().getDimensionPixelSize(id);
        return (int) (24 * c.getResources().getDisplayMetrics().density);
    }
}
