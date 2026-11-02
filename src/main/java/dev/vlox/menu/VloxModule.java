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

    public VloxModule(String name, String category, String icon) {
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.enabled = Boolean.parseBoolean(CONFIG.getProperty(name, "false"));
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

    public String name() {
        return name;
    }

    public String category() {
        return category;
    }

    public String icon() {
        return icon;
    }

    public boolean enabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
        save();
    }

    private void save() {
        // Keep unrelated properties intact and update every module in one write.
        for (VloxModule module : VloxModules.ALL) {
            CONFIG.setProperty(module.name(), Boolean.toString(module.enabled()));
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
