package com.vlox;

import com.mojang.blaze3d.platform.InputConstants;
import com.vlox.config.VloxConfig;
import com.vlox.feature.Features;
import com.vlox.gui.VloxScreen;
import com.vlox.hud.VloxHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.glfw.GLFW;

public class VloxClient implements ClientModInitializer {
	public static final String MOD_ID = "vlox";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final long SESSION_START = System.currentTimeMillis();

	public static KeyMapping menuKey;
	public static KeyMapping zoomKey;

	/** Field of view to return to, or -1 when not zoomed. */
	private static int baseFov = -1;
	private static double zoomedFov;
	private static boolean deathRecorded;

	@Override
	public void onInitializeClient() {
		VloxConfig.load();

		HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
				new VloxHud());

		menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.vlox.menu",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"))));

		zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.vlox.zoom",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"))));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			VloxConfig.flushIfDirty();
			while (menuKey.consumeClick()) {
				client.setScreenAndShow(new VloxScreen());
			}
			tickZoom(client);
			tickAutoSprint(client);
			tickDeathPoint(client);
		});

		LOGGER.info("VLOX ready with {} settings", Features.ALL.size());
	}

	/**
	 * Holds the field of view at a fraction of its normal value while the zoom
	 * key is down. The FOV option is written through its live instance and never
	 * saved, so a zoom never leaks into options.txt.
	 */
	private static void tickZoom(Minecraft client) {
		OptionInstance<Integer> fov = client.options == null ? null : client.options.fov();
		boolean wanted = VloxConfig.flag(Features.INPUT_ZOOM)
				&& client.player != null
				&& inGame(client)
				&& zoomKey != null
				&& zoomKey.isDown();

		if (fov == null || !wanted) {
			restoreFov(client);
			return;
		}

		if (baseFov < 0) {
			baseFov = fov.get();
			zoomedFov = baseFov;
		}

		double factor = Math.max(1.5D, VloxConfig.get(Features.INPUT_ZOOM_AMOUNT));
		double target = baseFov / factor;
		if (VloxConfig.flag(Features.INPUT_ZOOM_SMOOTH)) {
			zoomedFov += (target - zoomedFov) * 0.35D;
		} else {
			zoomedFov = target;
		}

		int value = (int) Math.round(Math.max(1.0D, zoomedFov));
		if (fov.get() != value) {
			fov.set(value);
		}
	}

	private static void restoreFov(Minecraft client) {
		if (baseFov >= 0 && client.options != null) {
			client.options.fov().set(baseFov);
		}
		baseFov = -1;
	}

	private static void tickAutoSprint(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || !inGame(client) || !VloxConfig.flag(Features.INPUT_AUTO_SPRINT)) {
			return;
		}
		if (player.isSprinting() || player.isCrouching() || player.isUsingItem()) {
			return;
		}
		if (player.getFoodData().getFoodLevel() <= 6) {
			return;
		}
		if (client.options != null && client.options.keyUp.isDown()) {
			player.setSprinting(true);
		}
	}

	/**
	 * True while the player is actually playing: the mouse is grabbed only when no
	 * screen is open, which keeps zoom and auto sprint out of menus.
	 */
	private static boolean inGame(Minecraft client) {
		return client.mouseHandler != null && client.mouseHandler.isMouseGrabbed();
	}

	/** Records where the player died, once per death, so the HUD can point back to it. */
	private static void tickDeathPoint(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.level == null) {
			deathRecorded = false;
			return;
		}
		if (player.getHealth() > 0.0F && !player.isDeadOrDying()) {
			deathRecorded = false;
			return;
		}
		if (deathRecorded || !VloxConfig.flag(Features.HUD_DEATH_POINT)) {
			return;
		}

		VloxConfig.putText("death.x", Double.toString(player.getX()));
		VloxConfig.putText("death.y", Double.toString(player.getY()));
		VloxConfig.putText("death.z", Double.toString(player.getZ()));
		VloxConfig.putText("death.dim", client.level.dimension().identifier().toString());
		deathRecorded = true;
	}
}
