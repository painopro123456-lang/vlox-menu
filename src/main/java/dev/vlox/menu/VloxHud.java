package dev.vlox.menu;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class VloxHud {
    private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("vlox_menu", "quality_of_life_hud");
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private VloxHud() {
    }

    public static void register() {
        HudElementRegistry.addLast(HUD_ID, (graphics, deltaTracker) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) {
                return;
            }

            int y = 8;
            final int color = 0xFFEAF1FF;
            final int shadow = 0xFF17243A;

            if (VloxModules.isEnabled("Coordinates")) {
                graphics.text(client.font, String.format("XYZ  %.1f  %.1f  %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ()), 8, y, color, true);
                y += 11;
            }

            if (VloxModules.isEnabled("Chunk Coordinates")) {
                var pos = client.player.blockPosition();
                graphics.text(client.font, "CHUNK  " + (pos.getX() >> 4) + "  " + (pos.getZ() >> 4),
                    8, y, color, true);
                y += 11;
            }

            if (VloxModules.isEnabled("World Time")) {
                long ticks = Math.floorMod(client.level.getDayTime(), 24000L);
                long hours = (ticks / 1000 + 6) % 24;
                long minutes = (ticks % 1000) * 60 / 1000;
                graphics.text(client.font, String.format("WORLD TIME  %02d:%02d", hours, minutes),
                    8, y, color, true);
                y += 11;
            }

            if (VloxModules.isEnabled("Clock")) {
                graphics.text(client.font, "LOCAL  " + LocalTime.now().format(CLOCK_FORMAT),
                    8, y, color, true);
                y += 11;
            }

            if (VloxModules.isEnabled("Light Level")) {
                int light = client.level.getMaxLocalRawBrightness(client.player.blockPosition());
                graphics.text(client.font, "LIGHT  " + light, 8, y, color, true);
                y += 11;
            }

            if (VloxModules.isEnabled("Memory Usage")) {
                Runtime runtime = Runtime.getRuntime();
                long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
                long maxMb = runtime.maxMemory() / (1024L * 1024L);
                graphics.text(client.font, "MEMORY  " + usedMb + " / " + maxMb + " MB",
                    8, y, color, true);
            }
        });
    }
}
