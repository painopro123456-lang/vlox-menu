package dev.vlox.menu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public final class VloxMenuClient implements ClientModInitializer {
    private static final int MENU_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private boolean wasMenuPressed;
    private final Set<Integer> previouslyPressed = new HashSet<>();

    @Override
    public void onInitializeClient() {
        VloxHud.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long window = client.getWindow().handle();
            boolean menuPressed = GLFW.glfwGetKey(window, MENU_KEY) == GLFW.GLFW_PRESS;

            if (menuPressed && !wasMenuPressed) {
                if (client.gui.screen() instanceof VloxMenuScreen) {
                    client.gui.setScreen(null);
                } else if (client.gui.screen() == null) {
                    client.gui.setScreen(new VloxMenuScreen());
                }
            }
            wasMenuPressed = menuPressed;

            Set<Integer> pressedNow = new HashSet<>();
            Set<Integer> triggeredThisTick = new HashSet<>();
            if (client.gui.screen() == null) {
                for (VloxModule module : VloxModules.ALL) {
                    int key = module.keyCode();
                    if (key < 0 || key == MENU_KEY) {
                        continue;
                    }
                    if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
                        pressedNow.add(key);
                        if (!previouslyPressed.contains(key) && triggeredThisTick.add(key)) {
                            module.toggle();
                        }
                    }
                }
            }
            previouslyPressed.clear();
            previouslyPressed.addAll(pressedNow);
        });
    }
}
