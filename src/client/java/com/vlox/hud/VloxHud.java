package com.vlox.hud;

import com.vlox.VloxClient;
import com.vlox.config.VloxConfig;
import com.vlox.feature.Features;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix3x2fStack;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** The VLOX HUD: a small stack of optional readouts. */
public final class VloxHud implements HudElement {
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final int ON = 0xFFE8EEF6;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null || minecraft.player == null || minecraft.level == null) {
			return;
		}
		if (!VloxConfig.flag(Features.HUD_ENABLED)) {
			return;
		}

		List<String> lines = buildLines(minecraft);
		if (lines.isEmpty()) {
			return;
		}

		Font font = minecraft.font;
		float scale = (float) VloxConfig.get(Features.HUD_SCALE);
		int gap = (int) Math.round(VloxConfig.get(Features.HUD_SPACING));
		int lineHeight = font.lineHeight + gap;

		int textWidth = 0;
		for (String line : lines) {
			textWidth = Math.max(textWidth, font.width(line));
		}

		int pad = 4;
		int boxWidth = (int) ((textWidth + pad * 2) * scale);
		int boxHeight = (int) ((lines.size() * lineHeight + pad * 2 - gap) * scale);

		int screenWidth = graphics.guiWidth();
		int screenHeight = graphics.guiHeight();

		int corner = VloxConfig.index(Features.HUD_CORNER);
		int x = corner == 1 || corner == 3 ? screenWidth - boxWidth - 4 : 4;
		int y = corner == 2 || corner == 3 ? screenHeight - boxHeight - 4 : 4;

		if (VloxConfig.flag(Features.HUD_BACKGROUND)) {
			graphics.fill(x, y, x + boxWidth, y + boxHeight, 0x9A0B0F14);
		}

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate((float) x, (float) y);
		pose.scale(scale, scale);

		boolean shadow = VloxConfig.flag(Features.HUD_SHADOW);
		int textY = pad;
		for (String line : lines) {
			graphics.text(font, line, pad, textY, ON, shadow);
			textY += lineHeight;
		}

		pose.popMatrix();
	}

	private List<String> buildLines(Minecraft minecraft) {
		List<String> lines = new ArrayList<>();
		LocalPlayer player = minecraft.player;
		BlockPos pos = player.blockPosition();

		if (VloxConfig.flag(Features.HUD_FPS)) {
			lines.add("FPS  " + minecraft.getFps());
		}
		if (VloxConfig.flag(Features.HUD_COORDS)) {
			lines.add(String.format("XYZ  %.1f / %.1f / %.1f", player.getX(), player.getY(), player.getZ()));
		}
		if (VloxConfig.flag(Features.HUD_NETHER)) {
			lines.add(String.format("Nether  %.0f / %.0f", player.getX() / 8.0, player.getZ() / 8.0));
		}
		if (VloxConfig.flag(Features.HUD_FACING)) {
			lines.add("Facing  " + compass(player.getYRot()) + "  " + Math.round(player.getYRot()) + "\u00B0");
		}
		if (VloxConfig.flag(Features.HUD_SPEED)) {
			double dx = player.getDeltaMovement().x;
			double dz = player.getDeltaMovement().z;
			lines.add(String.format("Speed  %.2f b/s", Math.sqrt(dx * dx + dz * dz) * 20.0));
		}
		if (VloxConfig.flag(Features.HUD_BIOME)) {
			String biome = minecraft.level.getBiome(pos).unwrapKey()
					.map(key -> key.identifier().getPath())
					.orElse("unknown");
			lines.add("Biome  " + biome);
		}
		if (VloxConfig.flag(Features.HUD_DIMENSION)) {
			lines.add("Dimension  " + minecraft.level.dimension().identifier().getPath());
		}
		if (VloxConfig.flag(Features.HUD_TIME)) {
			long time = minecraft.level.getDefaultClockTime() % 24000L;
			int hours = (int) ((time / 1000L + 6L) % 24L);
			int minutes = (int) ((time % 1000L) * 60L / 1000L);
			lines.add(String.format("Time  %02d:%02d", hours, minutes));
		}
		if (VloxConfig.flag(Features.HUD_DAY)) {
			lines.add("Day  " + (minecraft.level.getDefaultClockTime() / 24000L));
		}
		if (VloxConfig.flag(Features.HUD_CHUNK)) {
			lines.add("Chunk  " + (pos.getX() >> 4) + " / " + (pos.getZ() >> 4));
		}
		if (VloxConfig.flag(Features.HUD_LIGHT)) {
			lines.add("Light  " + minecraft.level.getMaxLocalRawBrightness(pos));
		}
		if (VloxConfig.flag(Features.HUD_MEMORY)) {
			Runtime runtime = Runtime.getRuntime();
			long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
			long max = runtime.maxMemory() / (1024L * 1024L);
			lines.add("Memory  " + used + " / " + max + " MB");
		}
		if (VloxConfig.flag(Features.HUD_ENTITIES)) {
			lines.add("Entities  " + minecraft.level.getEntityCount());
		}
		if (VloxConfig.flag(Features.HUD_PING)) {
			lines.add("Ping  " + ping(minecraft) + " ms");
		}
		if (VloxConfig.flag(Features.HUD_HEALTH)) {
			lines.add(String.format("Health  %.1f  (+%.1f)", player.getHealth(), player.getAbsorptionAmount()));
		}
		if (VloxConfig.flag(Features.HUD_HUNGER)) {
			lines.add("Hunger  " + player.getFoodData().getFoodLevel() + "  sat " + Math.round(player.getFoodData().getSaturationLevel()));
		}
		if (VloxConfig.flag(Features.HUD_ARMOR)) {
			lines.add("Armour  " + player.getArmorValue());
		}
		if (VloxConfig.flag(Features.HUD_HAND)) {
			ItemStack stack = player.getMainHandItem();
			String hand = player.getMainArm() == HumanoidArm.LEFT ? "L" : "R";
			if (stack.isEmpty()) {
				lines.add("Hand (" + hand + ")  empty");
			} else if (stack.isDamageableItem()) {
				lines.add("Hand (" + hand + ")  " + (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage());
			} else {
				lines.add("Hand (" + hand + ")  " + stack.getCount() + "x " + stack.getHoverName().getString());
			}
		}
		if (VloxConfig.flag(Features.HUD_XP)) {
			lines.add("XP  " + player.experienceLevel + "  " + Math.round(player.experienceProgress * 100.0F) + "%");
		}
		if (VloxConfig.flag(Features.HUD_SPRINT) && player.isSprinting()) {
			lines.add("Sprinting");
		}
		if (VloxConfig.flag(Features.HUD_SNEAK) && player.isCrouching()) {
			lines.add("Sneaking");
		}
		if (VloxConfig.flag(Features.HUD_LOOKING)) {
			HitResult hit = minecraft.hitResult;
			if (hit instanceof BlockHitResult blockHit) {
				lines.add("Looking at  " + blockHit.getBlockPos().toShortString());
			}
		}
		if (VloxConfig.flag(Features.HUD_BLOCKID)) {
			HitResult hit = minecraft.hitResult;
			if (hit instanceof BlockHitResult blockHit) {
				var state = minecraft.level.getBlockState(blockHit.getBlockPos());
				lines.add("Block  " + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()));
			}
		}
		if (VloxConfig.flag(Features.HUD_PLAYTIME)) {
			long seconds = (System.currentTimeMillis() - VloxClient.SESSION_START) / 1000L;
			lines.add(String.format("Session  %d:%02d", seconds / 60, seconds % 60));
		}
		if (VloxConfig.flag(Features.HUD_CLOCK)) {
			lines.add("Clock  " + LocalTime.now().format(CLOCK));
		}
		if (VloxConfig.flag(Features.HUD_SERVER)) {
			lines.add("Server  " + serverLabel(minecraft));
		}
		if (VloxConfig.flag(Features.HUD_EFFECTS)) {
			lines.add("Effects  " + player.getActiveEffects().size());
		}
		if (VloxConfig.flag(Features.HUD_FALL) && player.fallDistance > 0.0F) {
			lines.add(String.format("Falling  %.1f", player.fallDistance));
		}
		if (VloxConfig.flag(Features.HUD_DIFFICULTY)) {
			lines.add("Difficulty  " + minecraft.level.getDifficulty());
		}

		return lines;
	}

	private static int ping(Minecraft minecraft) {
		try {
			var connection = minecraft.getConnection();
			if (connection == null) {
				return 0;
			}
			var info = connection.getPlayerInfo(minecraft.player.getUUID());
			return info == null ? 0 : info.getLatency();
		} catch (Exception e) {
			return 0;
		}
	}

	private static String serverLabel(Minecraft minecraft) {
		var server = minecraft.getCurrentServer();
		if (server != null && server.ip != null) {
			return server.ip;
		}
		return "singleplayer";
	}

	private static String compass(float yaw) {
		int index = Math.round(((yaw % 360.0F) + 360.0F) % 360.0F / 45.0F) % 8;
		return new String[] { "S", "SW", "W", "NW", "N", "NE", "E", "SE" }[index];
	}
}
