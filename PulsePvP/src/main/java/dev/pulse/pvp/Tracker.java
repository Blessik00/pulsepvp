package dev.pulse.pvp;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/** Collects PvP state: last target, combo, CPS, damage events. Purely client-side and visual. */
public final class Tracker {
    public static LivingEntity target;
    public static long targetTime;
    public static int combo;
    public static long comboTime;
    public static double lastDistance;
    public static float damageFlash;

    private static final Map<Integer, Float> LAST_HEALTH = new HashMap<>();
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
    private static boolean leftDown, rightDown;
    private static float lastSelf = -1f;

    private Tracker() {}

    public static int cpsLeft() { return LEFT.size(); }
    public static int cpsRight() { return RIGHT.size(); }

    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity le) {
                long now = System.currentTimeMillis();
                target = le;
                targetTime = now;
                combo++;
                comboTime = now;
                lastDistance = player.distanceTo(entity);
                if (Config.on(Config.PARTICLES)) hitFx(world, player, le);
            }
            return ActionResult.PASS;
        });
        ClientTickEvents.END_CLIENT_TICK.register(Tracker::tick);
    }

    private static void hitFx(World world, PlayerEntity p, LivingEntity t) {
        boolean crit = p.fallDistance > 0f && !p.isOnGround() && !p.isClimbing()
                && !p.isTouchingWater() && !p.hasVehicle();
        double cx = t.getX();
        double cy = t.getY() + t.getHeight() * 0.6;
        double cz = t.getZ();
        int n = crit ? 18 : 8;
        for (int i = 0; i < n; i++) {
            double vx = (world.random.nextDouble() - 0.5) * 0.9;
            double vy = world.random.nextDouble() * 0.5;
            double vz = (world.random.nextDouble() - 0.5) * 0.9;
            world.addParticle(crit ? ParticleTypes.CRIT : ParticleTypes.ENCHANTED_HIT, cx, cy, cz, vx, vy, vz);
        }
        if (crit) {
            for (int i = 0; i < 5; i++) {
                world.addParticle(ParticleTypes.DAMAGE_INDICATOR, cx, cy, cz,
                        (world.random.nextDouble() - 0.5) * 0.4, 0.2, (world.random.nextDouble() - 0.5) * 0.4);
            }
        }
    }

    private static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        ClientWorld w = mc.world;
        if (p == null || w == null) {
            LAST_HEALTH.clear();
            LEFT.clear();
            RIGHT.clear();
            target = null;
            combo = 0;
            lastSelf = -1f;
            damageFlash = 0f;
            return;
        }
        long now = System.currentTimeMillis();

        boolean l = mc.options.attackKey.isPressed();
        boolean r = mc.options.useKey.isPressed();
        if (l && !leftDown) LEFT.add(now);
        if (r && !rightDown) RIGHT.add(now);
        leftDown = l;
        rightDown = r;
        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();

        float hp = p.getHealth() + p.getAbsorptionAmount();
        if (lastSelf >= 0f && hp < lastSelf - 0.01f) {
            damageFlash = Math.min(1f, damageFlash + 0.6f);
            combo = 0;
        }
        lastSelf = hp;
        damageFlash = Math.max(0f, damageFlash - 0.05f);
        if (now - comboTime > 3000) combo = 0;

        for (Entity e : w.getEntities()) {
            if (!(e instanceof LivingEntity le) || e == p) continue;
            if (le.squaredDistanceTo(p) > 40 * 40) continue;
            float h = le.getHealth();
            Float prev = LAST_HEALTH.put(le.getId(), h);
            if (prev != null && h < prev - 0.01f && Config.on(Config.NUMBERS)) {
                DamageNumbers.add(le, prev - h);
            }
        }
        if (w.getTime() % 200 == 0) {
            LAST_HEALTH.keySet().removeIf(id -> w.getEntityById(id) == null);
        }
    }
}
