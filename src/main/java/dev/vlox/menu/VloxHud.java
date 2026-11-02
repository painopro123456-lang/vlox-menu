package dev.vlox.menu;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

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
                long ticks = client.level.getLevelData().getGameTime();
                graphics.text(client.font, "WORLD TICKS  " + ticks, 8, y, color, true);
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

            if (VloxModules.isEnabled("Food Level HUD")) {
                int food = client.player.getFoodData().getFoodLevel();
                graphics.text(client.font, "FOOD  " + food + " / 20", 8, y, color, true);
                y += 11;
            }

            if (VloxModules.isEnabled("Saturation HUD")) {
                float saturation = client.player.getFoodData().getSaturationLevel();
                graphics.text(client.font, String.format("SATURATION  %.1f", saturation),
                    8, y, color, true);
                y += 11;
            }

            if (VloxModules.isEnabled("Armor HUD")) {
                graphics.text(client.font, "ARMOR  " + client.player.getArmorValue() + " / 20",
                    8, y, color, true);
                y += 11;
            }

            ItemStack heldItem = client.player.getMainHandItem();
            if (heldItem.isDamageableItem()) {
                int remaining = heldItem.getMaxDamage() - heldItem.getDamageValue();
                if (VloxModules.isEnabled("Durability HUD")) {
                    graphics.text(client.font, "DURABILITY  " + remaining + " / " + heldItem.getMaxDamage(),
                        8, y, color, true);
                    y += 11;
                }
                if (VloxModules.isEnabled("Durability Percentage")) {
                    int percentage = Math.round(remaining * 100.0f / heldItem.getMaxDamage());
                    graphics.text(client.font, "ITEM  " + percentage + "%",
                        8, y, color, true);
                    y += 11;
                }
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
