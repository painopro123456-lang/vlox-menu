package com.vloxboost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

/**
 * Moves the render distance one step at a time towards whatever holds the target
 * frame rate. It never raises the distance above the value the player had when
 * the session's world was joined, so it only ever takes work away.
 *
 * <p>The value is written through the live option and never saved, so options.txt
 * keeps the player's own render distance for the next launch.</p>
 */
public final class DynamicQuality {
	/** Long enough that a single stutter does not move anything. */
	private static final long INTERVAL_MS = 4000L;
	/** Only act on a clear miss, so it does not oscillate around the target. */
	private static final double DOWN_AT = 0.85D;
	private static final double UP_AT = 1.20D;

	private static long lastCheck;
	private static int ceiling = -1;

	private DynamicQuality() {
	}

	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null || minecraft.level == null || minecraft.options == null) {
			// Leaving a world: forget the ceiling so the next world starts fresh.
			ceiling = -1;
			return;
		}

		BoostConfig config = BoostConfig.get();
		OptionInstance<Integer> distance = minecraft.options.renderDistance();

		if (!config.dynamicRenderDistance) {
			restore(distance);
			return;
		}

		int current = distance.get();
		if (ceiling < 0) {
			ceiling = current;
		}

		long now = System.currentTimeMillis();
		if (now - lastCheck < INTERVAL_MS) {
			return;
		}
		lastCheck = now;

		int min = Math.min(config.minRenderDistance, config.maxRenderDistance);
		int max = Math.max(min, Math.min(config.maxRenderDistance, ceiling));

		int fps = minecraft.getFps();
		int next = current;

		if (fps < config.targetFps * DOWN_AT && current > min) {
			next = current - 1;
		} else if (fps > config.targetFps * UP_AT && current < max) {
			next = current + 1;
		}

		if (next != current) {
			distance.set(next);
			VloxBoost.LOGGER.debug("Render distance {} -> {} at {} fps", current, next, fps);
		}
	}

	/** Gives the player their render distance back when the feature is switched off. */
	private static void restore(OptionInstance<Integer> distance) {
		if (ceiling >= 0 && distance.get() < ceiling) {
			distance.set(ceiling);
		}
		ceiling = -1;
	}
}
