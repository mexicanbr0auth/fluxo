package com.fluxo.tarefas.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.fluxo.tarefas.ui.Palette;

public final class Prefs {

    private static final String FILE = "fluxo_prefs";

    private Prefs() {
    }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean dynamicColor(Context c) {
        return sp(c).getBoolean("dynamic", Build.VERSION.SDK_INT >= 31);
    }

    public static void setDynamicColor(Context c, boolean v) {
        sp(c).edit().putBoolean("dynamic", v).apply();
    }

    public static int seedIndex(Context c) {
        int i = sp(c).getInt("seed", 0);
        if (i < 0 || i >= Palette.SEEDS.length) i = 0;
        return i;
    }

    public static void setSeedIndex(Context c, int v) {
        sp(c).edit().putInt("seed", v).apply();
    }

    /** Cor personalizada (ARGB). 0 = nenhuma. */
    public static int customSeed(Context c) {
        return sp(c).getInt("custom_seed", 0);
    }

    public static void setCustomSeed(Context c, int v) {
        sp(c).edit().putInt("custom_seed", v).apply();
    }

    /** 0 = seguir sistema, 1 = claro, 2 = escuro */
    public static int themeMode(Context c) {
        return sp(c).getInt("theme", 0);
    }

    public static void setThemeMode(Context c, int v) {
        sp(c).edit().putInt("theme", v).apply();
    }

    public static boolean showCompleted(Context c) {
        return sp(c).getBoolean("show_done", true);
    }

    public static void setShowCompleted(Context c, boolean v) {
        sp(c).edit().putBoolean("show_done", v).apply();
    }

    public static String name(Context c) {
        return sp(c).getString("name", "");
    }

    public static void setName(Context c, String v) {
        sp(c).edit().putString("name", v).apply();
    }
}
