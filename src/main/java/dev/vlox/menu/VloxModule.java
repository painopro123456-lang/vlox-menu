package dev.vlox.menu;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class VloxModule {
    private static final Path CONFIG_FILE =
        FabricLoader.getInstance().getConfigDir().resolve("vlox-menu.properties");
    private static final Properties CONFIG = loadConfig();

    private final String name;
    private final String category;
    private final String icon;
    private boolean enabled;
    private int keyCode;
    private float scale;
    private int offsetX;
    private int offsetY;
    private float opacity;

    public VloxModule(String name, String category, String icon) {
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.enabled = Boolean.parseBoolean(CONFIG.getProperty(key("enabled"), CONFIG.getProperty(name, "false")));
        this.keyCode = parseInt(key("key"), -1);
        this.scale = parseFloat(key("scale"), 1.0f);
        this.offsetX = parseInt(key("x"), 0);
        this.offsetY = parseInt(key("y"), 0);
        this.opacity = parseFloat(key("opacity"), 1.0f);
    }

    private String key(String setting) {
        return "module." + name.toLowerCase().replaceAll("[^a-z0-9]+", "_") + "." + setting;
    }

    private static int parseInt(String key, int fallback) {
        try {
            return Integer.parseInt(CONFIG.getProperty(key, Integer.toString(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float parseFloat(String key, float fallback) {
        try {
            float value = Float.parseFloat(CONFIG.getProperty(key, Float.toString(fallback)));
            return Float.isFinite(value) ? value : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        if (Files.isRegularFile(CONFIG_FILE)) {
            try (InputStream input = Files.newInputStream(CONFIG_FILE)) {
                properties.load(input);
            } catch (IOException exception) {
                System.err.println("[VLOX] Could not load module settings: " + exception.getMessage());
            }
        }
        return properties;
    }

    public String name() { return name; }
    public String category() { return category; }
    public String icon() { return icon; }
    public boolean enabled() { return enabled; }
    public int keyCode() { return keyCode; }
    public float scale() { return scale; }
    public int offsetX() { return offsetX; }
    public int offsetY() { return offsetY; }
    public float opacity() { return opacity; }

    public void toggle() {
        enabled = !enabled;
        save();
    }

    public void setKeyCode(int keyCode) {
        if (keyCode < -1) {
            throw new IllegalArgumentException("Key code must be -1 (unbound) or a valid GLFW key code");
        }
        if (keyCode >= 0) {
            for (VloxModule other : VloxModules.ALL) {
                if (other != this && other.keyCode == keyCode) {
                    other.keyCode = -1;
                }
            }
        }
        this.keyCode = keyCode;
        save();
    }

    public void setScale(float scale) {
        this.scale = clamp(scale, 0.5f, 2.0f);
        save();
    }

    public void setPosition(int x, int y) {
        this.offsetX = Math.max(-2000, Math.min(2000, x));
        this.offsetY = Math.max(-2000, Math.min(2000, y));
        save();
    }

    public void setOpacity(float opacity) {
        this.opacity = clamp(opacity, 0.15f, 1.0f);
        save();
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void save() {
        for (VloxModule module : VloxModules.ALL) {
            CONFIG.setProperty(module.key("enabled"), Boolean.toString(module.enabled));
            CONFIG.setProperty(module.name, Boolean.toString(module.enabled));
            CONFIG.setProperty(module.key("key"), Integer.toString(module.keyCode));
            CONFIG.setProperty(module.key("scale"), Float.toString(module.scale));
            CONFIG.setProperty(module.key("x"), Integer.toString(module.offsetX));
            CONFIG.setProperty(module.key("y"), Integer.toString(module.offsetY));
            CONFIG.setProperty(module.key("opacity"), Float.toString(module.opacity));
        }
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_FILE)) {
                CONFIG.store(output, "VLOX MENU module settings");
            }
        } catch (IOException exception) {
            System.err.println("[VLOX] Could not save module settings: " + exception.getMessage());
        }
    }
}
