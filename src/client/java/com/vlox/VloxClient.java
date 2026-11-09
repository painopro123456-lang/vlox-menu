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
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.glfw.GLFW;

public class VloxClient implements ClientModInitializer {
	public static final String MOD_ID = "vlox";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final long SESSION_START = System.currentTimeMillis();

	public static KeyMapping menuKey;
	private static boolean applied;

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

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!applied) {
				Features.applyAll();
				applied = true;
			}

			while (menuKey.consumeClick()) {
				client.setScreenAndShow(new VloxScreen());
			}
		});

		LOGGER.info("VLOX ready with {} settings", Features.ALL.size());
	}
}
