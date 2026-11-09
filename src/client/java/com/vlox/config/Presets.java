package com.vlox.config;

import com.vlox.feature.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

/**
 * One-click layouts.
 *
 * The HUD presets only touch VLOX's own settings, so they can never fight with
 * the vanilla options screen. The performance preset is the exception: it sets
 * four real video options through their live instances and then saves
 * options.txt, exactly as the vanilla screen would.
 */
public final class Presets {
	private Presets() {
	}

	/** The lines a fight actually uses, in the top right. */
	public static void pvp() {
		VloxConfig.setFlag(Features.HUD_ENABLED, true);
		VloxConfig.setFlag(Features.HUD_BACKGROUND, true);
		VloxConfig.set(Features.HUD_CORNER, 1);
		VloxConfig.set(Features.HUD_ARMOR, 2);
		VloxConfig.setFlag(Features.HUD_HEALTH, true);
		VloxConfig.setFlag(Features.HUD_HUNGER, true);
		VloxConfig.setFlag(Features.HUD_PING, true);
		VloxConfig.setFlag(Features.HUD_FPS, true);
		VloxConfig.setFlag(Features.HUD_HAND, true);
		VloxConfig.setFlag(Features.HUD_KEYSTROKES, true);
		VloxConfig.setFlag(Features.HUD_CPS, true);
		VloxConfig.setFlag(Features.HUD_ITEMS, true);
		VloxConfig.set(Features.HUD_EFFECTS, 2);
		VloxConfig.setFlag(Features.HUD_SPRINT, true);
		VloxConfig.setFlag(Features.INPUT_AUTO_SPRINT, true);
	}

	/** Where you are and what the world is doing, in the top left. */
	public static void explorer() {
		VloxConfig.setFlag(Features.HUD_ENABLED, true);
		VloxConfig.setFlag(Features.HUD_BACKGROUND, true);
		VloxConfig.set(Features.HUD_CORNER, 0);
		VloxConfig.setFlag(Features.HUD_COORDS, true);
		VloxConfig.setFlag(Features.HUD_FACING, true);
		VloxConfig.setFlag(Features.HUD_BIOME, true);
		VloxConfig.setFlag(Features.HUD_DIMENSION, true);
		VloxConfig.setFlag(Features.HUD_TIME, true);
		VloxConfig.setFlag(Features.HUD_DAY, true);
		VloxConfig.setFlag(Features.HUD_LIGHT, true);
		VloxConfig.setFlag(Features.HUD_CHUNK, true);
		VloxConfig.setFlag(Features.HUD_SPEED, true);
		VloxConfig.setFlag(Features.HUD_NETHER, true);
		VloxConfig.setFlag(Features.HUD_DEATH_POINT, true);
		VloxConfig.setFlag(Features.HUD_FPS, true);
	}

	/** Lower the expensive video options and write them to options.txt. */
	public static void performance() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null || minecraft.options == null) {
			return;
		}
		Options options = minecraft.options;
		options.renderDistance().set(8);
		options.simulationDistance().set(8);
		options.entityDistanceScaling().set(0.5D);
		options.entityShadows().set(false);
		options.chunkSectionFadeInTime().set(0.0D);
		options.save();
	}
}
