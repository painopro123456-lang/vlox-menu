package com.vlox.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vlox.feature.Feature;
import com.vlox.feature.Features;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Stores every feature value in one small JSON file. */
public final class VloxConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Map<String, Double> VALUES = new HashMap<>();
	/** Free-form text kept beside the settings, such as the last death point. */
	private static final Map<String, String> TEXT = new HashMap<>();
	private static final long WRITE_DELAY_MS = 400L;

	/** Bumped on every change, so the HUD knows when its cached layout is stale. */
	private static long revision;
	private static boolean dirty;
	private static long dirtyAt;

	private VloxConfig() {
	}

	public static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("vlox.json");
	}

	public static void load() {
		Path path = file();
		if (!Files.exists(path)) {
			return;
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			JsonObject values = root.getAsJsonObject("values");
			if (values != null) {
				for (String key : values.keySet()) {
					if (values.get(key).isJsonPrimitive()) {
						VALUES.put(key, values.get(key).getAsDouble());
					}
				}
			}
			JsonObject text = root.getAsJsonObject("text");
			if (text != null) {
				for (String key : text.keySet()) {
					if (text.get(key).isJsonPrimitive()) {
						TEXT.put(key, text.get(key).getAsString());
					}
				}
			}
		} catch (Exception e) {
			System.err.println("[vlox] could not read config: " + e.getMessage());
		}
	}

	public static long revision() {
		return revision;
	}

	/**
	 * Writes the file if something changed and the change has had a moment to settle.
	 * Dragging a slider then costs one write instead of one per pixel, and a sudden
	 * crash loses at most {@link #WRITE_DELAY_MS} milliseconds of settings.
	 */
	public static void flushIfDirty() {
		if (dirty && System.currentTimeMillis() - dirtyAt >= WRITE_DELAY_MS) {
			save();
		}
	}

	/** Writes now, whatever the timer says. */
	public static void flush() {
		if (dirty) {
			save();
		}
	}

	public static void save() {
		dirty = false;
		JsonObject values = new JsonObject();
		for (String key : VALUES.keySet()) {
			values.addProperty(key, VALUES.get(key));
		}

		JsonObject text = new JsonObject();
		for (String key : TEXT.keySet()) {
			text.addProperty(key, TEXT.get(key));
		}

		JsonObject root = new JsonObject();
		root.addProperty("version", 1);
		root.add("values", values);
		root.add("text", text);

		try {
			Files.createDirectories(file().getParent());
			Files.writeString(file(), GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (Exception e) {
			System.err.println("[vlox] could not save config: " + e.getMessage());
		}
	}

	public static String getText(String key, String fallback) {
		return TEXT.getOrDefault(key, fallback);
	}

	public static boolean hasText(String key) {
		return TEXT.containsKey(key);
	}

	/** Stores free-form text and schedules a write. */
	public static void putText(String key, String value) {
		if (value == null) {
			TEXT.remove(key);
		} else {
			TEXT.put(key, value);
		}
		revision++;
		dirty = true;
		dirtyAt = System.currentTimeMillis();
	}

	public static double get(Feature feature) {
		Double value = VALUES.get(feature.id);
		if (value == null) {
			return clamp(feature, feature.def);
		}
		return clamp(feature, value);
	}

	public static void set(Feature feature, double value) {
		VALUES.put(feature.id, clamp(feature, value));
		revision++;
		dirty = true;
		dirtyAt = System.currentTimeMillis();
		feature.fireChange();
	}

	public static void nudge(Feature feature, double delta) {
		set(feature, get(feature) + delta);
	}

	public static boolean flag(Feature feature) {
		return get(feature) >= 0.5;
	}

	public static int index(Feature feature) {
		return (int) Math.round(get(feature));
	}

	public static void setFlag(Feature feature, boolean value) {
		set(feature, value ? 1 : 0);
	}

	public static void resetAll() {
		VALUES.clear();
		for (Feature feature : Features.ALL) {
			VALUES.put(feature.id, feature.def);
		}
		revision++;
		dirty = true;
		save();
	}

	private static double clamp(Feature feature, double value) {
		switch (feature.type) {
			case NUMBER -> {
				double v = Math.max(feature.min, Math.min(feature.max, value));
				double steps = Math.round((v - feature.min) / feature.step);
				v = feature.min + steps * feature.step;
				return Math.max(feature.min, Math.min(feature.max, v));
			}
			case CHOICE -> {
				return Math.max(0, Math.min(feature.choices.length - 1, Math.round(value)));
			}
			case TOGGLE -> {
				return value >= 0.5 ? 1 : 0;
			}
			default -> {
				return 0;
			}
		}
	}
}
