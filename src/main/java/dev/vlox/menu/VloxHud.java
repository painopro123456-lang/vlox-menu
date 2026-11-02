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
import java.util.ArrayDeque;
import java.util.Deque;

public final class VloxHud {
    private static final Identifier HUD_ID = Identifier.fromNamespaceAndPath("vlox_menu", "quality_of_life_hud");
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long SESSION_STARTED_AT = System.currentTimeMillis();
    private static long stopwatchStartedAt;
    private static long stopwatchAccumulated;
    private static boolean stopwatchWasEnabled;
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();
    private static boolean leftMouseWasDown;
    private static boolean rightMouseWasDown;

    private static VloxModule findModule(String name) {
        return VloxModules.ALL.stream().filter(module -> module.name().equals(name)).findFirst().orElse(null);
    }

    private static float moduleScale(String name) {
        VloxModule module = findModule(name);
        return module == null ? 1.0f : module.scale();
    }

    private static void drawText(net.minecraft.client.gui.GuiGraphicsExtractor graphics, Minecraft client,
                                 String moduleName, String message, int x, int y, int color, boolean shadow) {
        VloxModule module = findModule(moduleName);
        if (module == null) return;
        int alpha = Math.round(((color >>> 24) & 0xFF) * module.opacity());
        int adjustedColor = (alpha << 24) | (color & 0x00FFFFFF);
        graphics.text(client.font, message, x + module.offsetX(), y + module.offsetY(), adjustedColor, shadow);
    }

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

            long now = System.currentTimeMillis();
            long windowHandle = client.getWindow().handle();
            boolean leftMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean rightMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            if (leftMouseDown && !leftMouseWasDown) leftClicks.addLast(now);
            if (rightMouseDown && !rightMouseWasDown) rightClicks.addLast(now);
            leftMouseWasDown = leftMouseDown;
            rightMouseWasDown = rightMouseDown;
            while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000L) leftClicks.removeFirst();
            while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000L) rightClicks.removeFirst();
            if (VloxModules.isEnabled("CPS Display")) {
                drawText(graphics, client, "CPS Display", "CPS  L " + leftClicks.size() + "  R " + rightClicks.size(), 8, y, color, true);
                y += Math.round(11 * moduleScale("CPS Display"));
            }
            boolean stopwatchEnabled = VloxModules.isEnabled("Stopwatch");
            if (stopwatchEnabled && !stopwatchWasEnabled) stopwatchStartedAt = now;
            if (!stopwatchEnabled && stopwatchWasEnabled) stopwatchAccumulated += now - stopwatchStartedAt;
            stopwatchWasEnabled = stopwatchEnabled;

            if (VloxModules.isEnabled("Session Stats")) {
                long seconds = (now - SESSION_STARTED_AT) / 1000L;
                drawText(graphics, client, "Session Stats", "SESSION  " + String.format("%02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60), 8, y, color, true);
                y += Math.round(11 * moduleScale("Session Stats"));
            }
            if (stopwatchEnabled) {
                long elapsed = (stopwatchAccumulated + now - stopwatchStartedAt) / 1000L;
                drawText(graphics, client, "Stopwatch", "STOPWATCH  " + String.format("%02d:%02d:%02d", elapsed / 3600, (elapsed / 60) % 60, elapsed % 60), 8, y, color, true);
                y += Math.round(11 * moduleScale("Stopwatch"));
            }
            if (VloxModules.isEnabled("Low Health Alert") && client.player.getHealth() <= 6.0f) {
                drawText(graphics, client, "Low Health Alert", "LOW HEALTH  " + String.format("%.1f", client.player.getHealth()) + " HP", 8, y, 0xFFFF7777, true);
                y += Math.round(11 * moduleScale("Low Health Alert"));
            }
            ItemStack warningStack = client.player.getMainHandItem();
            if (VloxModules.isEnabled("Durability Warning") && warningStack.isDamageableItem()) {
                int remaining = warningStack.getMaxDamage() - warningStack.getDamageValue();
                if (remaining <= Math.max(1, warningStack.getMaxDamage() / 5)) {
                    drawText(graphics, client, "Durability Warning", "LOW DURABILITY  " + remaining + " LEFT", 8, y, 0xFFFFAA66, true);
                    y += Math.round(11 * moduleScale("Durability Warning"));
                }
            }

            if (VloxModules.isEnabled("FPS Display")) {
                drawText(graphics, client, "FPS Display", "FPS  " + client.getFps(), 8, y, color, true);
                y += Math.round(11 * moduleScale("FPS Display"));
            }
            if (VloxModules.isEnabled("Ping Display") && client.getConnection() != null) {
                var info = client.getConnection().getPlayerInfo(client.player.getUUID());
                if (info != null) {
                    drawText(graphics, client, "Ping Display", "PING  " + info.getLatency() + " ms", 8, y, color, true);
                y += Math.round(11 * moduleScale("Ping Display"));
                }
            }
            if (VloxModules.isEnabled("Keystrokes")) {
                long window = client.getWindow().handle();
                String keys = (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS ? "[W]" : " W ")
                    + " " + (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS ? "[A]" : " A ")
                    + " " + (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS ? "[S]" : " S ")
                    + " " + (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS ? "[D]" : " D ");
                drawText(graphics, client, "Keystrokes", keys, 8, y, color, true);
                y += Math.round(11 * moduleScale("Keystrokes"));
            }
            if (VloxModules.isEnabled("Direction HUD")) {
                float yaw = client.player.getYRot() % 360.0f;
                if (yaw < 0) yaw += 360.0f;
                String direction = yaw < 45 || yaw >= 315 ? "SOUTH"
                    : yaw < 135 ? "WEST" : yaw < 225 ? "NORTH" : "EAST";
                drawText(graphics, client, "Direction HUD", "FACING  " + direction, 8, y, color, true);
                y += Math.round(11 * moduleScale("Direction HUD"));
            }
            if (VloxModules.isEnabled("Compass HUD")) {
                float yaw = client.player.getYRot() % 360.0f;
                if (yaw < 0) yaw += 360.0f;
                drawText(graphics, client, "Compass HUD", String.format("COMPASS  %.0f°", yaw), 8, y, color, true);
                y += Math.round(11 * moduleScale("Compass HUD"));
            }
            if (VloxModules.isEnabled("Biome Display")) {
                var biome = client.level.getBiome(client.player.blockPosition());
                String biomeName = biome.getRegisteredName();
                drawText(graphics, client, "Biome Display", "BIOME  " + biomeName, 8, y, color, true);
                y += Math.round(11 * moduleScale("Biome Display"));
            }
            if (VloxModules.isEnabled("World Border Distance")) {
                double distance = client.level.getWorldBorder().getDistanceToBorder(client.player);
                drawText(graphics, client, "World Border Distance", String.format("BORDER  %.1f blocks", distance), 8, y, color, true);
                y += Math.round(11 * moduleScale("World Border Distance"));
            }
            if (VloxModules.isEnabled("Speedometer")) {
                var velocity = client.player.getDeltaMovement();
                double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0;
                drawText(graphics, client, "Speedometer", String.format("SPEED  %.2f blocks/s", speed), 8, y, color, true);
                y += Math.round(11 * moduleScale("Speedometer"));
            }
            if (VloxModules.isEnabled("Item Counter")) {
                ItemStack stack = client.player.getMainHandItem();
                drawText(graphics, client, "Item Counter", "HELD  " + (stack.isEmpty() ? "EMPTY" : stack.getCount() + "x " + stack.getHoverName().getString()),
                    8, y, color, true);
                y += Math.round(11 * moduleScale("Item Counter"));
            }
            if (VloxModules.isEnabled("Nearby Entity Count")) {
                int count = client.level.getEntities(client.player, client.player.getBoundingBox().inflate(16.0)).size();
                drawText(graphics, client, "Nearby Entity Count", "ENTITIES NEARBY  " + count, 8, y, color, true);
                y += Math.round(11 * moduleScale("Nearby Entity Count"));
            }
            if (VloxModules.isEnabled("Days Played")) {
                drawText(graphics, client, "Days Played", "WORLD DAYS  " + (client.level.getLevelData().getGameTime() / 24000L), 8, y, color, true);
                y += Math.round(11 * moduleScale("Days Played"));
            }
            if (VloxModules.isEnabled("Targeted Block Info") && client.hitResult instanceof BlockHitResult blockHit
                && client.hitResult.getType() == HitResult.Type.BLOCK) {
                var state = client.level.getBlockState(blockHit.getBlockPos());
                drawText(graphics, client, "Targeted Block Info", "BLOCK  " + state.getBlock().getName().getString(), 8, y, color, true);
                y += Math.round(11 * moduleScale("Targeted Block Info"));
            }
            if (VloxModules.isEnabled("Targeted Entity Health") && client.hitResult instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof net.minecraft.world.entity.LivingEntity living) {
                drawText(graphics, client, "Targeted Entity Health", String.format("TARGET HP  %.1f / %.1f", living.getHealth(), living.getMaxHealth()),
                    8, y, color, true);
                y += Math.round(11 * moduleScale("Targeted Entity Health"));
            }

            if (VloxModules.isEnabled("Coordinates")) {
                drawText(graphics, client, "Coordinates", String.format("XYZ  %.1f  %.1f  %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ()), 8, y, color, true);
                y += Math.round(11 * moduleScale("Coordinates"));
            }

            if (VloxModules.isEnabled("Chunk Coordinates")) {
                var pos = client.player.blockPosition();
                drawText(graphics, client, "Chunk Coordinates", "CHUNK  " + (pos.getX() >> 4) + "  " + (pos.getZ() >> 4),
                    8, y, color, true);
                y += Math.round(11 * moduleScale("Chunk Coordinates"));
            }

            if (VloxModules.isEnabled("World Time")) {
                long ticks = client.level.getLevelData().getGameTime() % 24000L;
                drawText(graphics, client, "World Time", "WORLD TIME  " + String.format("%02d:%02d", (ticks / 1000 + 6) % 24, (ticks % 1000) * 60 / 1000), 8, y, color, true);
                y += Math.round(11 * moduleScale("World Time"));
            }

            if (VloxModules.isEnabled("Clock")) {
                drawText(graphics, client, "Clock", "LOCAL  " + LocalTime.now().format(CLOCK_FORMAT),
                    8, y, color, true);
                y += Math.round(11 * moduleScale("Clock"));
            }

            if (VloxModules.isEnabled("Light Level")) {
                int light = client.level.getMaxLocalRawBrightness(client.player.blockPosition());
                drawText(graphics, client, "Light Level", "LIGHT  " + light, 8, y, color, true);
                y += Math.round(11 * moduleScale("Light Level"));
            }

            if (VloxModules.isEnabled("Food Level HUD")) {
                int food = client.player.getFoodData().getFoodLevel();
                drawText(graphics, client, "Food Level HUD", "FOOD  " + food + " / 20", 8, y, color, true);
                y += Math.round(11 * moduleScale("Food Level HUD"));
            }

            if (VloxModules.isEnabled("Saturation HUD")) {
                float saturation = client.player.getFoodData().getSaturationLevel();
                drawText(graphics, client, "Saturation HUD", String.format("SATURATION  %.1f", saturation),
                    8, y, color, true);
                y += Math.round(11 * moduleScale("Saturation HUD"));
            }

            if (VloxModules.isEnabled("Armor HUD")) {
                drawText(graphics, client, "Armor HUD", "ARMOR  " + client.player.getArmorValue() + " / 20",
                    8, y, color, true);
                y += Math.round(11 * moduleScale("Armor HUD"));
            }

            ItemStack heldItem = client.player.getMainHandItem();
            if (heldItem.isDamageableItem()) {
                int remaining = heldItem.getMaxDamage() - heldItem.getDamageValue();
                if (VloxModules.isEnabled("Durability HUD")) {
                    drawText(graphics, client, "Durability HUD", "DURABILITY  " + remaining + " / " + heldItem.getMaxDamage(),
                        8, y, color, true);
                y += Math.round(11 * moduleScale("Durability HUD"));
                }
                if (VloxModules.isEnabled("Durability Percentage")) {
                    int percentage = Math.round(remaining * 100.0f / heldItem.getMaxDamage());
                    drawText(graphics, client, "Durability Percentage", "ITEM  " + percentage + "%",
                        8, y, color, true);
                y += Math.round(11 * moduleScale("Durability Percentage"));
                }
            }

            if (VloxModules.isEnabled("Memory Usage")) {
                Runtime runtime = Runtime.getRuntime();
                long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
                long maxMb = runtime.maxMemory() / (1024L * 1024L);
                drawText(graphics, client, "Memory Usage", "MEMORY  " + usedMb + " / " + maxMb + " MB",
                    8, y, color, true);
                y += Math.round(11 * moduleScale("Memory Usage"));
            }
        });
    }
}
