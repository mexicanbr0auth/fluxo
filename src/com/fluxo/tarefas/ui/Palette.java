package com.fluxo.tarefas.ui;

/**
 * Paleta tonal no estilo Material You.
 * As cores dinâmicas partem da cor de destaque do papel de parede (Monet)
 * e são expandidas em tons perceptuais (L*) com croma controlado.
 */
public final class Palette {

    public static final int[] SEEDS = {
            0xFF6750A4, // lavanda
            0xFF0B6BCB, // oceano
            0xFF006B5F, // esmeralda
            0xFFB4453A, // coral
            0xFF8B5000, // âmbar
            0xFF984061, // rosa
            0xFF00639B, // céu
            0xFF4C662B  // oliva
    };

    public static final String[] SEED_NAMES = {
            "Lavanda", "Oceano", "Esmeralda", "Coral", "Âmbar", "Rosa", "Céu", "Oliva"
    };

    public final boolean dark;
    public final int seed;

    public final int primary, onPrimary, primaryContainer, onPrimaryContainer;
    public final int secondary, onSecondary, secondaryContainer, onSecondaryContainer;
    public final int tertiary, onTertiary, tertiaryContainer, onTertiaryContainer;
    public final int error, onError, errorContainer, onErrorContainer;
    public final int background, onBackground;
    public final int surface, onSurface, surfaceVariant, onSurfaceVariant;
    public final int outline, outlineVariant;
    public final int surfaceContainerLowest, surfaceContainerLow, surfaceContainer,
            surfaceContainerHigh, surfaceContainerHighest;
    public final int inverseSurface, inverseOnSurface, inversePrimary;

    private Palette(boolean dark, int seed,
                    int primary, int onPrimary, int primaryContainer, int onPrimaryContainer,
                    int secondary, int onSecondary, int secondaryContainer, int onSecondaryContainer,
                    int tertiary, int onTertiary, int tertiaryContainer, int onTertiaryContainer,
                    int error, int onError, int errorContainer, int onErrorContainer,
                    int background, int onBackground,
                    int surface, int onSurface, int surfaceVariant, int onSurfaceVariant,
                    int outline, int outlineVariant,
                    int surfaceContainerLowest, int surfaceContainerLow, int surfaceContainer,
                    int surfaceContainerHigh, int surfaceContainerHighest,
                    int inverseSurface, int inverseOnSurface, int inversePrimary) {
        this.dark = dark;
        this.seed = seed;
        this.primary = primary;
        this.onPrimary = onPrimary;
        this.primaryContainer = primaryContainer;
        this.onPrimaryContainer = onPrimaryContainer;
        this.secondary = secondary;
        this.onSecondary = onSecondary;
        this.secondaryContainer = secondaryContainer;
        this.onSecondaryContainer = onSecondaryContainer;
        this.tertiary = tertiary;
        this.onTertiary = onTertiary;
        this.tertiaryContainer = tertiaryContainer;
        this.onTertiaryContainer = onTertiaryContainer;
        this.error = error;
        this.onError = onError;
        this.errorContainer = errorContainer;
        this.onErrorContainer = onErrorContainer;
        this.background = background;
        this.onBackground = onBackground;
        this.surface = surface;
        this.onSurface = onSurface;
        this.surfaceVariant = surfaceVariant;
        this.onSurfaceVariant = onSurfaceVariant;
        this.outline = outline;
        this.outlineVariant = outlineVariant;
        this.surfaceContainerLowest = surfaceContainerLowest;
        this.surfaceContainerLow = surfaceContainerLow;
        this.surfaceContainer = surfaceContainer;
        this.surfaceContainerHigh = surfaceContainerHigh;
        this.surfaceContainerHighest = surfaceContainerHighest;
        this.inverseSurface = inverseSurface;
        this.inverseOnSurface = inverseOnSurface;
        this.inversePrimary = inversePrimary;
    }

    public static Palette fromSeed(int seedColor, boolean dark) {
        int seed = seedColor | 0xFF000000;
        double[] lab = toLab(seed);
        double hue = Math.toDegrees(Math.atan2(lab[2], lab[1]));
        if (hue < 0) hue += 360;
        double chroma = Math.hypot(lab[1], lab[2]);

        double pHue = hue;
        double pC = clamp(chroma * 1.05, 26, 58);
        double sHue = hue;
        double sC = clamp(chroma * 0.55, 12, 30);
        double tHue = norm(hue + 60);
        double tC = clamp(chroma * 0.9, 22, 48);
        double nHue = hue;
        double nC = 3.2;
        double nvC = 7.0;
        double eHue = 27;
        double eC = 80;

        if (!dark) {
            int p40 = tone(40, pC, pHue);
            int p80 = tone(80, pC, pHue);
            int p90 = tone(90, pC, pHue);
            int p100 = tone(100, pC, pHue);
            int p10 = tone(10, pC, pHue);
            int p20 = tone(20, pC, pHue);
            int s40 = tone(40, sC, sHue);
            int s90 = tone(90, sC, sHue);
            int s100 = tone(100, sC, sHue);
            int s10 = tone(10, sC, sHue);
            int t40 = tone(40, tC, tHue);
            int t90 = tone(90, tC, tHue);
            int t100 = tone(100, tC, tHue);
            int t10 = tone(10, tC, tHue);
            int e40 = tone(40, eC, eHue);
            int e90 = tone(90, eC, eHue);
            int e100 = tone(100, eC, eHue);
            int e10 = tone(10, eC, eHue);
            int n99 = tone(99, nC, nHue);
            int n10 = tone(10, nC, nHue);
            int n20 = tone(20, nC, nHue);
            int nv90 = tone(90, nvC, nHue);
            int nv80 = tone(80, nvC, nHue);
            int nv60 = tone(60, nvC, nHue);
            int nv50 = tone(50, nvC, nHue);
            int nv30 = tone(30, nvC, nHue);
            int n100 = tone(100, nC, nHue);
            int n96 = tone(96, nC, nHue);
            int n95 = tone(95, nC, nHue);
            int n94 = tone(94, nC, nHue);
            int n92 = tone(92, nC, nHue);
            int n90 = tone(90, nC, nHue);

            return new Palette(false, seed,
                    p40, p100, p90, p10,
                    s40, s100, s90, s10,
                    t40, t100, t90, t10,
                    e40, e100, e90, e10,
                    n99, n10,
                    n99, n10, nv90, nv30,
                    nv50, nv80,
                    n100, n96, n95, n92, n90,
                    n20, n95, p80);
        } else {
            int p40 = tone(40, pC, pHue);
            int p20 = tone(20, pC, pHue);
            int p30 = tone(30, pC, pHue);
            int p80 = tone(80, pC, pHue);
            int p90 = tone(90, pC, pHue);
            int s20 = tone(20, sC, sHue);
            int s30 = tone(30, sC, sHue);
            int s80 = tone(80, sC, sHue);
            int s90 = tone(90, sC, sHue);
            int t20 = tone(20, tC, tHue);
            int t30 = tone(30, tC, tHue);
            int t80 = tone(80, tC, tHue);
            int t90 = tone(90, tC, tHue);
            int e20 = tone(20, eC, eHue);
            int e30 = tone(30, eC, eHue);
            int e80 = tone(80, eC, eHue);
            int e90 = tone(90, eC, eHue);
            int n6 = tone(6, nC, nHue);
            int n4 = tone(4, nC, nHue);
            int n10 = tone(10, nC, nHue);
            int n12 = tone(12, nC, nHue);
            int n17 = tone(17, nC, nHue);
            int n22 = tone(22, nC, nHue);
            int n90 = tone(90, nC, nHue);
            int nv30 = tone(30, nvC, nHue);
            int nv60 = tone(60, nvC, nHue);
            int nv80 = tone(80, nvC, nHue);
            int n20 = tone(20, nC, nHue);

            return new Palette(true, seed,
                    p80, p20, p30, p90,
                    s80, s20, s30, s90,
                    t80, t20, t30, t90,
                    e80, e20, e30, e90,
                    n6, n90,
                    n6, n90, nv30, nv80,
                    nv60, nv30,
                    n4, n10, n12, n17, n22,
                    n90, n20, p40);
        }
    }

    private static double norm(double h) {
        double v = h % 360;
        return v < 0 ? v + 360 : v;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Gera uma cor no espaço LCh com recorte de gama em sRGB. */
    private static int tone(double l, double chroma, double hue) {
        if (l <= 0.2) return 0xFF000000;
        if (l >= 99.8) return 0xFFFFFFFF;
        double lo = 0, hi = chroma;
        for (int i = 0; i < 16; i++) {
            double mid = (lo + hi) / 2;
            if (inGamut(l, mid, hue)) lo = mid;
            else hi = mid;
        }
        double[] rgb = lchToRgb(l, lo, hue);
        return 0xFF000000
                | (clampByte(rgb[0]) << 16)
                | (clampByte(rgb[1]) << 8)
                | clampByte(rgb[2]);
    }

    private static int clampByte(double v) {
        int i = (int) Math.round(v * 255.0);
        return Math.max(0, Math.min(255, i));
    }

    private static boolean inGamut(double l, double c, double h) {
        double[] rgb = lchToRgb(l, c, h);
        return rgb[0] >= -0.0008 && rgb[0] <= 1.0008
                && rgb[1] >= -0.0008 && rgb[1] <= 1.0008
                && rgb[2] >= -0.0008 && rgb[2] <= 1.0008;
    }

    private static double[] lchToRgb(double l, double c, double hue) {
        double rad = Math.toRadians(hue);
        double a = Math.cos(rad) * c;
        double b = Math.sin(rad) * c;
        return labToRgb(l, a, b);
    }

    private static double[] toLab(int color) {
        double r = linear(((color >> 16) & 0xFF) / 255.0);
        double g = linear(((color >> 8) & 0xFF) / 255.0);
        double b = linear((color & 0xFF) / 255.0);
        double x = 0.4124564 * r + 0.3575761 * g + 0.1804375 * b;
        double y = 0.2126729 * r + 0.7151522 * g + 0.0721750 * b;
        double z = 0.0193339 * r + 0.1191920 * g + 0.9503041 * b;
        double fx = f(x / 0.95047);
        double fy = f(y);
        double fz = f(z / 1.08883);
        return new double[]{116.0 * fy - 16.0, 500.0 * (fx - fy), 200.0 * (fy - fz)};
    }

    private static double[] labToRgb(double l, double a, double b) {
        double fy = (l + 16.0) / 116.0;
        double fx = fy + a / 500.0;
        double fz = fy - b / 200.0;
        double x = 0.95047 * finv(fx);
        double y = finv(fy);
        double z = 1.08883 * finv(fz);
        double r = 3.2404542 * x - 1.5371385 * y - 0.4985314 * z;
        double g = -0.9692660 * x + 1.8760108 * y + 0.0415560 * z;
        double bl = 0.0556434 * x - 0.2040259 * y + 1.0572252 * z;
        return new double[]{gamma(r), gamma(g), gamma(bl)};
    }

    private static double linear(double v) {
        return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    private static double gamma(double v) {
        return v <= 0.0031308 ? 12.92 * v : 1.055 * Math.pow(v, 1.0 / 2.4) - 0.055;
    }

    private static double f(double t) {
        return t > 0.008856 ? Math.cbrt(t) : (7.787 * t + 16.0 / 116.0);
    }

    private static double finv(double t) {
        double t3 = t * t * t;
        return t3 > 0.008856 ? t3 : (t - 16.0 / 116.0) / 7.787;
    }
}
