package dev.pulse.pvp;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DamageNumbers {
    private record Num(double x, double y, double z, String text, int color, long born) {}

    private static final List<Num> LIST = new ArrayList<>();
    private static final Random RNG = new Random();
    private static final long LIFE = 1100L;

    private DamageNumbers() {}

    public static void add(LivingEntity e, float dmg) {
        double ox = (RNG.nextDouble() - 0.5) * 0.6;
        double oz = (RNG.nextDouble() - 0.5) * 0.6;
        int color = dmg >= 6f ? 0xFFFF4D4D : dmg >= 3f ? 0xFFFFB020 : 0xFFFFFFFF;
        LIST.add(new Num(e.getX() + ox, e.getY() + e.getHeight() + 0.2, e.getZ() + oz,
                String.format("-%.1f", dmg), color, System.currentTimeMillis()));
        if (LIST.size() > 40) LIST.remove(0);
    }

    public static void render(WorldRenderContext ctx) {
        if (LIST.isEmpty() || !Config.on(Config.NUMBERS)) return;
        long now = System.currentTimeMillis();
        LIST.removeIf(n -> now - n.born() > LIFE);

        MatrixStack m = ctx.matrixStack();
        VertexConsumerProvider vc = ctx.consumers();
        Camera cam = ctx.camera();
        if (m == null || vc == null || cam == null) return;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        Vec3d cp = cam.getPos();

        for (Num n : LIST) {
            float t = (now - n.born()) / (float) LIFE;
            double y = n.y() + t * 0.8;
            m.push();
            m.translate(n.x() - cp.x, y - cp.y, n.z() - cp.z);
            m.multiply(cam.getRotation());
            float pop = 1f + (1f - Math.min(1f, t * 6f)) * 0.6f;
            float s = 0.03f * pop;
            m.scale(-s, -s, s);
            int a = Math.max(5, (int) (255 * (1f - t * t)));
            int color = (a << 24) | (n.color() & 0xFFFFFF);
            float w = tr.getWidth(n.text());
            tr.draw(n.text(), -w / 2f, 0f, color, true, m.peek().getPositionMatrix(), vc,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);
            m.pop();
        }
    }
}
