package dev.vlox.menu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class VloxMenuClient implements ClientModInitializer {
    private static final int MENU_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private boolean wasPressed;

    @Override
    public void onInitializeClient() {
        VloxHud.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long window = client.getWindow().handle();
            boolean pressed = GLFW.glfwGetKey(window, MENU_KEY) == GLFW.GLFW_PRESS;

            // Treat Right Shift as a true toggle: open the menu from gameplay,
            // and close it again when pressed while the VLOX screen is visible.
            if (pressed && !wasPressed) {
                if (client.gui.screen() instanceof VloxMenuScreen) {
                    client.gui.setScreen(null);
                } else if (client.gui.screen() == null) {
                    client.gui.setScreen(new VloxMenuScreen());
                }
            }
            wasPressed = pressed;
        });
    }
}
