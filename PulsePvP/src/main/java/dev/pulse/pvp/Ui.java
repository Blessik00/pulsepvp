package dev.pulse.pvp;

import net.minecraft.client.gui.DrawContext;

public final class Ui {
    private Ui() {}

    public static int alpha(int color, int a) {
        a = Math.max(0, Math.min(255, a));
        return (a << 24) | (color & 0xFFFFFF);
    }

    public static int mulAlpha(int color, float f) {
        int a = (int) (((color >>> 24) & 0xFF) * f);
        return alpha(color, a);
    }

    public static int lerp(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = mix((c1 >>> 24) & 255, (c2 >>> 24) & 255, t);
        int r = mix((c1 >>> 16) & 255, (c2 >>> 16) & 255, t);
        int g = mix((c1 >>> 8) & 255, (c2 >>> 8) & 255, t);
        int b = mix(c1 & 255, c2 & 255, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int mix(int a, int b, float t) {
        return (int) (a + (b - a) * t);
    }

    /** Rounded rectangle built from horizontal strips. */
    public static void rrect(DrawContext c, int x, int y, int w, int h, int r, int col) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            c.fill(x, y, x + w, y + h, col);
            return;
        }
        c.fill(x, y + r, x + w, y + h - r, col);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * (double) r - dy * dy));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
    }

    public static void panel(DrawContext c, int x, int y, int w, int h, float f) {
        rrect(c, x, y, w, h, 4, mulAlpha(0xCC12121A, f));
        rrect(c, x + 3, y, w - 6, 2, 1, mulAlpha(Config.accent, f));
    }
}
