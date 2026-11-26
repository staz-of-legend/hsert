package com.example.fps;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    /** A bow needs at least 3 ticks of draw to fire; 20 ticks is a full charge. */
    public static final int MIN_TICKS = 3;
    public static final int MAX_TICKS = 20;

    public static boolean enabled = true;
    public static int drawTicks = 3;

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fps.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (InputStream in = Files.newInputStream(f)) {
            Properties p = new Properties();
            p.load(in);
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            drawTicks = clamp(Integer.parseInt(p.getProperty("drawTicks", "3")));
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("enabled", Boolean.toString(enabled));
        p.setProperty("drawTicks", Integer.toString(drawTicks));
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "fps settings");
        } catch (IOException ignored) {
        }
    }

    public static int clamp(int v) {
        return Math.max(MIN_TICKS, Math.min(MAX_TICKS, v));
    }
}
