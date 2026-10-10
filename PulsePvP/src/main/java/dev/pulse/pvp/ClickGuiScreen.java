package dev.pulse.pvp;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class ClickGuiScreen extends Screen {
    private static final int W = 320, ROW = 30, HEADER = 46, FOOTER = 66;

    private float open = 0.2f;
    private double scroll = 0;
    private boolean dragSlider;
    private final Map<String, Float> anim = new HashMap<>();

    public ClickGuiScreen() {
        super(Text.literal("PulsePvP"));
    }

    private int px() { return (width - W) / 2; }
    private int ph() { return Math.min(height - 20, HEADER + Config.DEFS.length * ROW + FOOTER); }
    private int py() { return (height - ph()) / 2; }
    private int listTop() { return py() + HEADER; }
    private int listBottom() { return py() + ph() - FOOTER; }
    private int maxScroll() { return Math.max(0, Config.DEFS.length * ROW - (listBottom() - listTop())); }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0x88000000);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        super.render(ctx, mx, my, delta);
        open += (1f - open) * 0.2f;
        float f = Math.min(1f, open);

        int x = px();
        int h = ph();
        int y = py() + (int) ((1f - f) * 12);

        Ui.rrect(ctx, x - 1, y - 1, W + 2, h + 2, 7, Ui.mulAlpha(Ui.alpha(Config.accent, 90), f));
        Ui.rrect(ctx, x, y, W, h, 6, Ui.mulAlpha(0xF0101018, f));

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.translate(x + 16, y + 10, 0);
        m.scale(1.4f, 1.4f, 1f);
        ctx.drawText(textRenderer, "PulsePvP", 0, 0, Ui.mulAlpha(0xFFFFFFFF, f), true);
        m.pop();
        ctx.drawText(textRenderer, "PvP visuals  -  Right Shift: закрыть", x + 16, y + 26,
                Ui.mulAlpha(0xFF8A8AA0, f), false);
        ctx.fill(x + 16, y + HEADER - 6, x + W - 16, y + HEADER - 5, Ui.mulAlpha(Ui.alpha(Config.accent, 120), f));

        // module list
        int lt = listTop() + (y - py());
        int lb = listBottom() + (y - py());
        ctx.enableScissor(x, lt, x + W, lb);
        for (int i = 0; i < Config.DEFS.length; i++) {
            String name = Config.DEFS[i][0];
            String desc = Config.DEFS[i][1];
            int ry = lt + i * ROW - (int) scroll;
            if (ry + ROW < lt || ry > lb) continue;

            boolean hover = mx >= x + 10 && mx <= x + W - 10 && my >= Math.max(ry, lt)
                    && my <= Math.min(ry + ROW - 4, lb);
            Ui.rrect(ctx, x + 10, ry + 1, W - 20, ROW - 4, 5,
                    Ui.mulAlpha(hover ? 0x38FFFFFF : 0x1CFFFFFF, f));
            ctx.drawText(textRenderer, name, x + 22, ry + 6, Ui.mulAlpha(0xFFFFFFFF, f), true);
            ctx.drawText(textRenderer, desc, x + 22, ry + 17, Ui.mulAlpha(0xFF8A8AA0, f), false);

            boolean on = Config.on(name);
            float a = anim.getOrDefault(name, on ? 1f : 0f);
            a += ((on ? 1f : 0f) - a) * 0.25f;
            anim.put(name, a);

            int tw = 30, th = 14;
            int tx = x + W - 22 - tw;
            int ty = ry + (ROW - 4 - th) / 2 + 1;
            Ui.rrect(ctx, tx, ty, tw, th, 7, Ui.mulAlpha(Ui.lerp(0xFF3A3A4A, Config.accent, a), f));
            int kx = tx + 2 + (int) ((tw - 14) * a);
            Ui.rrect(ctx, kx, ty + 2, 10, 10, 5, Ui.mulAlpha(0xFFFFFFFF, f));
        }
        ctx.disableScissor();

        // footer
        int fy = y + h - FOOTER;
        ctx.fill(x + 16, fy, x + W - 16, fy + 1, Ui.mulAlpha(0x22FFFFFF, f));
        ctx.drawText(textRenderer, "Цвет", x + 16, fy + 12, Ui.mulAlpha(0xFFFFFFFF, f), true);
        for (int i = 0; i < Config.ACCENTS.length; i++) {
            int sx = x + 110 + i * 24;
            int sy = fy + 8;
            if (Config.accent == Config.ACCENTS[i]) {
                Ui.rrect(ctx, sx - 2, sy - 2, 18, 18, 9, Ui.mulAlpha(0xFFFFFFFF, f));
            }
            Ui.rrect(ctx, sx, sy, 14, 14, 7, Ui.mulAlpha(Config.ACCENTS[i], f));
        }

        ctx.drawText(textRenderer, String.format("Масштаб HUD  %.2f", Config.scale), x + 16, fy + 40,
                Ui.mulAlpha(0xFFFFFFFF, f), true);
        int tx0 = x + 150, tw = W - 150 - 20, ty = fy + 42;
        float t = (Config.scale - 0.7f) / 0.8f;
        Ui.rrect(ctx, tx0, ty, tw, 4, 2, Ui.mulAlpha(0xFF2A2A36, f));
        Ui.rrect(ctx, tx0, ty, Math.max(2, (int) (tw * t)), 4, 2, Ui.mulAlpha(Config.accent, f));
        Ui.rrect(ctx, tx0 + (int) (tw * t) - 4, ty - 3, 8, 10, 4, Ui.mulAlpha(0xFFFFFFFF, f));
    }

    private void setSlider(double mx) {
        int tx0 = px() + 150, tw = W - 150 - 20;
        float t = (float) ((mx - tx0) / tw);
        t = Math.max(0f, Math.min(1f, t));
        Config.scale = Math.round((0.7f + t * 0.8f) * 20f) / 20f;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        int x = px();
        int lt = listTop(), lb = listBottom();

        if (my >= lt && my <= lb) {
            for (int i = 0; i < Config.DEFS.length; i++) {
                int ry = lt + i * ROW - (int) scroll;
                if (mx >= x + 10 && mx <= x + W - 10 && my >= ry && my <= ry + ROW - 4) {
                    String name = Config.DEFS[i][0];
                    Config.MODULES.put(name, !Config.on(name));
                    return true;
                }
            }
        }

        int fy = py() + ph() - FOOTER;
        for (int i = 0; i < Config.ACCENTS.length; i++) {
            int sx = x + 110 + i * 24, sy = fy + 8;
            if (mx >= sx - 2 && mx <= sx + 16 && my >= sy - 2 && my <= sy + 16) {
                Config.accent = Config.ACCENTS[i];
                return true;
            }
        }

        int tx0 = x + 150, tw = W - 150 - 20, ty = fy + 42;
        if (mx >= tx0 - 6 && mx <= tx0 + tw + 6 && my >= ty - 6 && my <= ty + 10) {
            dragSlider = true;
            setSlider(mx);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragSlider) {
            setSlider(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragSlider = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - vAmount * 20));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        Config.save();
        super.removed();
    }
}
