package com.vloxboost;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The whole configuration, read from {@code config/vloxboost.json}. The fields
 * are written straight into the file, so the file stays hand-editable.
 */
public final class BoostConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static BoostConfig instance = new BoostConfig();

	public boolean entityCull = true;
	/** Entities further than this from the camera are not drawn at all. */
	public double entityDistance = 64.0D;

	public boolean blockEntityCull = true;
	/** Chests, signs and other block entities further than this are skipped. */
	public double blockEntityDistance = 48.0D;

	public boolean particleCap = true;
	public int maxParticles = 2000;

	public boolean dynamicRenderDistance = true;
	public int targetFps = 75;
	public int minRenderDistance = 4;
	public int maxRenderDistance = 16;

	private BoostConfig() {
	}

	public static BoostConfig get() {
		return instance;
	}

	public static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("vloxboost.json");
	}

	public static void load() {
		Path path = file();
		if (!Files.exists(path)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			BoostConfig loaded = GSON.fromJson(reader, BoostConfig.class);
			if (loaded != null) {
				instance = loaded;
			}
		} catch (Exception e) {
			VloxBoost.LOGGER.warn("Could not read {}: {}", path, e.getMessage());
		}
	}

	public static void save() {
		try {
			Files.createDirectories(file().getParent());
			Files.writeString(file(), GSON.toJson(instance), StandardCharsets.UTF_8);
		} catch (Exception e) {
			VloxBoost.LOGGER.warn("Could not write {}: {}", file(), e.getMessage());
		}
	}
}
