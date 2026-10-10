package dev.pulse.pvp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Config {
    public static final String TARGET = "Target HUD";
    public static final String NUMBERS = "Damage Numbers";
    public static final String PARTICLES = "Hit Particles";
    public static final String KEYS = "Keystrokes + CPS";
    public static final String ARMOR = "Armor HUD";
    public static final String POTIONS = "Potion HUD";
    public static final String ITEMS = "PvP Item Counter";
    public static final String COMBO = "Combo + Reach";
    public static final String INFO = "Info HUD";
    public static final String FLASH = "Damage Flash";

    public static final String[][] DEFS = {
            {TARGET, "Здоровье, броня и дистанция до цели"},
            {NUMBERS, "Всплывающие цифры урона над целью"},
            {PARTICLES, "Частицы при ударе и крите"},
            {KEYS, "WASD, мышь и CPS на экране"},
            {ARMOR, "Прочность брони справа"},
            {POTIONS, "Активные эффекты с таймером"},
            {ITEMS, "Тотемы, яблоки, перлы, кристаллы"},
            {COMBO, "Комбо-счётчик и дистанция удара"},
            {INFO, "FPS, пинг, координаты, направление"},
            {FLASH, "Красная вспышка при получении урона"},
    };

    public static final int[] ACCENTS = {0xFF7C5CFF, 0xFF00D4FF, 0xFFFF4D6D, 0xFF3DDC84, 0xFFFFB020};

    public static final Map<String, Boolean> MODULES = new LinkedHashMap<>();
    public static int accent = ACCENTS[0];
    public static float scale = 1.0f;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pulsepvp.json");

    static {
        for (String[] d : DEFS) MODULES.put(d[0], true);
    }

    private Config() {}

    public static boolean on(String key) {
        return MODULES.getOrDefault(key, true);
    }

    public static void load() {
        if (!Files.exists(FILE)) return;
        try {
            JsonObject o = GSON.fromJson(Files.readString(FILE), JsonObject.class);
            if (o == null) return;
            if (o.has("modules")) {
                JsonObject m = o.getAsJsonObject("modules");
                for (String k : MODULES.keySet()) {
                    if (m.has(k)) MODULES.put(k, m.get(k).getAsBoolean());
                }
            }
            if (o.has("accent")) accent = o.get("accent").getAsInt();
            if (o.has("scale")) scale = Math.max(0.7f, Math.min(1.5f, o.get("scale").getAsFloat()));
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            JsonObject m = new JsonObject();
            for (Map.Entry<String, Boolean> e : MODULES.entrySet()) m.addProperty(e.getKey(), e.getValue());
            o.add("modules", m);
            o.addProperty("accent", accent);
            o.addProperty("scale", scale);
            Files.writeString(FILE, GSON.toJson(o));
        } catch (IOException ignored) {
        }
    }
}
