package dev.pulse.pvp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

public final class Hud {
    private static float shownHp = -1f;
    private static LivingEntity shownFor;
    private static final Map<String, Float> ANIM = new HashMap<>();

    private static final Item[] COUNT_ITEMS = {
            Items.TOTEM_OF_UNDYING, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
            Items.ENDER_PEARL, Items.END_CRYSTAL, Items.SPLASH_POTION
    };
    private static ItemStack[] countStacks;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private Hud() {}

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;

        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        if (Config.on(Config.FLASH)) flash(ctx, w, h);

        float s = Config.scale;
        MatrixStack m = ctx.getMatrices();
        m.push();
        m.scale(s, s, 1f);
        int sw = (int) (w / s);
        int sh = (int) (h / s);
        TextRenderer tr = mc.textRenderer;

        if (Config.on(Config.INFO)) info(ctx, mc, tr);
        if (Config.on(Config.POTIONS)) potions(ctx, mc, tr, Config.on(Config.INFO) ? 36 : 6);
        if (Config.on(Config.KEYS)) keystrokes(ctx, mc, tr, 8, sh - 78);
        if (Config.on(Config.ARMOR)) armor(ctx, mc, tr, sw - 62, sh / 2 - 44);
        if (Config.on(Config.ITEMS)) itemCounter(ctx, mc, tr, sw, 4);
        if (Config.on(Config.TARGET)) targetHud(ctx, mc, tr, sw, sh);
        if (Config.on(Config.COMBO)) combo(ctx, tr, sw, sh);
        m.pop();
    }

    private static void flash(DrawContext c, int w, int h) {
        float a = Tracker.damageFlash;
        if (a <= 0.01f) return;
        int n = 14;
        for (int i = 0; i < n; i++) {
            float k = 1f - i / (float) n;
            int col = Ui.alpha(0xFF2030, (int) (110 * a * k * k));
            int o = i * 3;
            c.fill(0, o, w, o + 3, col);
            c.fill(0, h - o - 3, w, h - o, col);
            c.fill(o, 0, o + 3, h, col);
            c.fill(w - o - 3, 0, w - o, h, col);
        }
    }

    private static void info(DrawContext c, MinecraftClient mc, TextRenderer tr) {
        PlayerEntity p = mc.player;
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
            if (e != null) ping = e.getLatency();
        }
        String l1 = "FPS " + mc.getCurrentFps() + "   Ping " + ping + "ms";
        String face = p.getHorizontalFacing().asString();
        face = face.substring(0, 1).toUpperCase() + face.substring(1);
        String l2 = String.format("XYZ %d %d %d   %s", (int) Math.floor(p.getX()), (int) Math.floor(p.getY()),
                (int) Math.floor(p.getZ()), face);
        int w = Math.max(tr.getWidth(l1), tr.getWidth(l2)) + 16;
        Ui.panel(c, 6, 6, w, 26, 1f);
        c.drawText(tr, l1, 14, 11, 0xFFFFFFFF, true);
        c.drawText(tr, l2, 14, 21, 0xFF9A9AAE, false);
    }

    private static void potions(DrawContext c, MinecraftClient mc, TextRenderer tr, int startY) {
        int y = startY;
        int count = 0;
        for (StatusEffectInstance inst : mc.player.getStatusEffects()) {
            if (count++ >= 8) break;
            String name = inst.getEffectType().value().getName().getString();
            if (inst.getAmplifier() > 0) name += " " + (inst.getAmplifier() + 1);
            String time = inst.isInfinite() ? "inf" : fmtTime(inst.getDuration());
            int col = inst.getEffectType().value().getColor() | 0xFF000000;
            int w = tr.getWidth(name) + tr.getWidth(time) + 30;
            Ui.rrect(c, 6, y, w, 16, 4, 0xCC12121A);
            Ui.rrect(c, 6, y + 2, 3, 12, 1, col);
            c.drawText(tr, name, 14, y + 4, 0xFFFFFFFF, true);
            boolean low = !inst.isInfinite() && inst.getDuration() < 200;
            c.drawText(tr, time, 6 + w - 8 - tr.getWidth(time), y + 4, low ? 0xFFFF4D4D : 0xFF9A9AAE, true);
            y += 19;
        }
    }

    private static String fmtTime(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private static void keystrokes(DrawContext c, MinecraftClient mc, TextRenderer tr, int x, int y) {
        int s = 22, g = 3;
        var o = mc.options;
        key(c, tr, "W", o.forwardKey.isPressed(), x + s + g, y, s, s);
        key(c, tr, "A", o.leftKey.isPressed(), x, y + s + g, s, s);
        key(c, tr, "S", o.backKey.isPressed(), x + s + g, y + s + g, s, s);
        key(c, tr, "D", o.rightKey.isPressed(), x + 2 * (s + g), y + s + g, s, s);
        int bw = (3 * s + 2 * g - g) / 2;
        int my = y + 2 * (s + g);
        key(c, tr, "LMB " + Tracker.cpsLeft(), o.attackKey.isPressed(), x, my, bw, 20);
        key(c, tr, "RMB " + Tracker.cpsRight(), o.useKey.isPressed(), x + bw + g, my, bw, 20);
    }

    private static void key(DrawContext c, TextRenderer tr, String label, boolean down, int x, int y, int w, int h) {
        String id = label.length() > 3 ? label.substring(0, 3) : label;
        float a = ANIM.getOrDefault(id, 0f);
        a += ((down ? 1f : 0f) - a) * 0.35f;
        ANIM.put(id, a);
        int bg = Ui.lerp(0xB012121A, Ui.alpha(Config.accent, 0xE0), a);
        Ui.rrect(c, x, y, w, h, 4, bg);
        c.drawCenteredTextWithShadow(tr, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    private static void armor(DrawContext c, MinecraftClient mc, TextRenderer tr, int x, int y) {
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            ItemStack st = mc.player.getEquippedStack(ARMOR_SLOTS[i]);
            int ry = y + i * 22;
            Ui.rrect(c, x, ry, 56, 20, 4, 0xCC12121A);
            if (st.isEmpty()) continue;
            c.drawItem(st, x + 2, ry + 2);
            if (st.isDamageable()) {
                float ratio = 1f - st.getDamage() / (float) st.getMaxDamage();
                int col = Ui.lerp(0xFFFF4D4D, 0xFF3DDC84, ratio);
                c.drawText(tr, (int) (ratio * 100) + "%", x + 22, ry + 4, col, true);
                Ui.rrect(c, x + 22, ry + 14, 30, 3, 1, 0xFF2A2A36);
                int f = (int) (30 * ratio);
                if (f > 1) Ui.rrect(c, x + 22, ry + 14, f, 3, 1, col);
            }
        }
    }

    private static void itemCounter(DrawContext c, MinecraftClient mc, TextRenderer tr, int sw, int y) {
        if (countStacks == null) {
            countStacks = new ItemStack[COUNT_ITEMS.length];
            for (int i = 0; i < COUNT_ITEMS.length; i++) countStacks[i] = new ItemStack(COUNT_ITEMS[i]);
        }
        PlayerInventory inv = mc.player.getInventory();
        int[] counts = new int[COUNT_ITEMS.length];
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isEmpty()) continue;
            for (int k = 0; k < COUNT_ITEMS.length; k++) {
                if (s.isOf(COUNT_ITEMS[k])) counts[k] += s.getCount();
            }
        }
        int cell = 34;
        int total = cell * COUNT_ITEMS.length;
        int x0 = (sw - total) / 2;
        Ui.rrect(c, x0 - 4, y, total + 8, 22, 5, 0xB012121A);
        for (int k = 0; k < COUNT_ITEMS.length; k++) {
            int x = x0 + k * cell;
            c.drawItem(countStacks[k], x, y + 3);
            if (counts[k] == 0) c.fill(x, y + 3, x + 16, y + 19, 0xAA12121A);
            c.drawText(tr, String.valueOf(counts[k]), x + 18, y + 7,
                    counts[k] == 0 ? 0xFF666677 : 0xFFFFFFFF, true);
        }
    }

    private static void targetHud(DrawContext c, MinecraftClient mc, TextRenderer tr, int sw, int sh) {
        LivingEntity t = Tracker.target;
        if (t == null || t.isRemoved()) return;
        long age = System.currentTimeMillis() - Tracker.targetTime;
        if (age > 4500) return;
        float f = age > 3800 ? 1f - (age - 3800) / 700f : 1f;
        if (f < 0.04f) return;

        if (shownFor != t) {
            shownFor = t;
            shownHp = t.getHealth();
        }
        float max = Math.max(1f, t.getMaxHealth());
        shownHp += (t.getHealth() - shownHp) * 0.15f;
        float ratio = Math.max(0f, Math.min(1f, shownHp / max));

        int pw = 150, ph = 46;
        int x = sw / 2 + 24, y = sh / 2 + 18;
        Ui.panel(c, x, y, pw, ph, f);

        String name = t.getName().getString();
        String hp = String.format("%.1f HP", t.getHealth());
        c.drawText(tr, name, x + 8, y + 8, Ui.mulAlpha(0xFFFFFFFF, f), true);
        c.drawText(tr, hp, x + pw - 8 - tr.getWidth(hp), y + 8, Ui.mulAlpha(0xFFBBBBBB, f), true);

        int bx = x + 8, by = y + 22, bw = pw - 16, bh = 6;
        Ui.rrect(c, bx, by, bw, bh, 3, Ui.mulAlpha(0xFF2A2A36, f));
        int fill = (int) (bw * ratio);
        if (fill >= 2) Ui.rrect(c, bx, by, fill, bh, 3, Ui.mulAlpha(Ui.lerp(0xFFFF4D4D, 0xFF3DDC84, ratio), f));

        String info = String.format("Armor %d   Dist %.1fm", t.getArmor(), mc.player.distanceTo(t));
        c.drawText(tr, info, x + 8, y + 33, Ui.mulAlpha(0xFF9A9AAE, f), false);
    }

    private static void combo(DrawContext c, TextRenderer tr, int sw, int sh) {
        if (Tracker.combo < 2) return;
        long age = System.currentTimeMillis() - Tracker.comboTime;
        if (age > 3000) return;
        float f = age > 2400 ? 1f - (age - 2400) / 600f : 1f;
        if (f < 0.04f) return;
        MatrixStack m = c.getMatrices();
        m.push();
        m.translate(sw / 2f, sh / 2f - 48f, 0f);
        m.scale(1.6f, 1.6f, 1f);
        c.drawCenteredTextWithShadow(tr, "x" + Tracker.combo, 0, 0, Ui.mulAlpha(Config.accent | 0xFF000000, f));
        m.pop();
        c.drawCenteredTextWithShadow(tr, String.format("reach %.2f", Tracker.lastDistance),
                sw / 2, (int) (sh / 2f - 30f), Ui.mulAlpha(0xFF9A9AAE, f));
    }
}
