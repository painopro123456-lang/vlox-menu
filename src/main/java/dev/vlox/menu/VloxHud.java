package dev.vlox.menu;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

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

            if (VloxModules.isEnabled("FPS Display")) {
                graphics.text(client.font, "FPS  " + client.getFps(), 8, y, color, true);
                y += 11;
            }
            if (VloxModules.isEnabled("Ping Display") && client.getConnection() != null) {
                var info = client.getConnection().getPlayerInfo(client.player.getUUID());
                if (info != null) {
                    graphics.text(client.font, "PING  " + info.getLatency() + " ms", 8, y, color, true);
                    y += 11;
                }
            }
            if (VloxModules.isEnabled("Keystrokes")) {
                long window = client.getWindow().handle();
                String keys = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS ? "[W]" : " W ")
                    + " " + (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS ? "[A]" : " A ")
                    + " " + (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS ? "[S]" : " S ")
                    + " " + (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS ? "[D]" : " D ");
                graphics.text(client.font, keys, 8, y, color, true);
                y += 11;
            }
            if (VloxModules.isEnabled("Direction HUD")) {
                float yaw = client.player.getYRot() % 360.0f;
                if (yaw < 0) yaw += 360.0f;
                String direction = yaw < 45 || yaw >= 315 ? "SOUTH"
                    : yaw < 135 ? "WEST" : yaw < 225 ? "NORTH" : "EAST";
                graphics.text(client.font, "FACING  " + direction, 8, y, color, true);
                y += 11;
            }
            if (VloxModules.isEnabled("Speedometer")) {
                var velocity = client.player.getDeltaMovement();
                double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0;
                graphics.text(client.font, String.format("SPEED  %.2f blocks/s", speed), 8, y, color, true);
                y += 11;
            }
            if (VloxModules.isEnabled("Item Counter")) {
                ItemStack stack = client.player.getMainHandItem();
                graphics.text(client.font, "HELD  " + (stack.isEmpty() ? "EMPTY" : stack.getCount() + "x " + stack.getHoverName().getString()),
                    8, y, color, true);
                y += 11;
            }
            if (VloxModules.isEnabled("Nearby Entity Count")) {
                int count = client.level.getEntities(client.player, client.player.getBoundingBox().inflate(16.0)).size();
                graphics.text(client.font, "ENTITIES NEARBY  " + count, 8, y, color, true);
                y += 11;
            }
            if (VloxModules.isEnabled("Days Played")) {
                graphics.text(client.font, "WORLD DAYS  " + (client.level.getDayTime() / 24000L), 8, y, color, true);
                y += 11;
            }
            if (VloxModules.isEnabled("Targeted Block Info") && client.hitResult instanceof BlockHitResult blockHit
                && client.hitResult.getType() == HitResult.Type.BLOCK) {
                var state = client.level.getBlockState(blockHit.getBlockPos());
                graphics.text(client.font, "BLOCK  " + state.getBlock().getName().getString(), 8, y, color, true);
                y += 11;
            }
            if (VloxModules.isEnabled("Targeted Entity Health") && client.hitResult instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof net.minecraft.world.entity.LivingEntity living) {
                graphics.text(client.font, String.format("TARGET HP  %.1f / %.1f", living.getHealth(), living.getMaxHealth()),
                    8, y, color, true);
                y += 11;
            }

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
                long ticks = client.level.getDayTime() % 24000L;
                graphics.text(client.font, "WORLD TIME  " + String.format("%02d:%02d", (ticks / 1000 + 6) % 24, (ticks % 1000) * 60 / 1000), 8, y, color, true);
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
