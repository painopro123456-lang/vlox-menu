package com.vlox.hud;

import com.vlox.VloxClient;
import com.vlox.config.VloxConfig;
import com.vlox.feature.Features;
import com.vlox.gui.Ui;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix3x2fStack;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The VLOX HUD.
 *
 * A stack of rows that can be text or 16x16 item icons, so armour and held items
 * show the actual item rather than a number. The list of rows and the panel size
 * are rebuilt at most every {@link #REFRESH_MS} milliseconds instead of every
 * frame, which keeps the cost of a full HUD near zero: no formatting, no
 * measuring and no allocation on most frames.
 */
public final class VloxHud implements HudElement {
	private static final int ON = 0xFFE8EEF6;
	private static final long REFRESH_MS = 50L;
	private static final int PAD = 4;
	private static final String[] ROMAN = { "", " II", " III", " IV", " V", " VI", " VII", " VIII" };
	private static final String[] COMPASS = { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };

	private final List<Row> rows = new ArrayList<>();
	private final StringBuilder text = new StringBuilder(96);
	private final MobEffectInstance[] effects = new MobEffectInstance[16];

	private long builtAt = Long.MIN_VALUE;
	private long builtRevision = -1L;
	private int boxWidth;
	private int boxHeight;
	private int gap;
	private int corner;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return;
		}
		CombatInput.poll(minecraft);

		if (minecraft.player == null || minecraft.level == null || !VloxConfig.flag(Features.HUD_ENABLED)) {
			return;
		}

		long now = System.currentTimeMillis();
		if (now - builtAt >= REFRESH_MS || builtRevision != VloxConfig.revision()) {
			rebuild(minecraft);
			builtAt = now;
		}
		if (rows.isEmpty()) {
			return;
		}

		Font font = minecraft.font;
		float scale = (float) VloxConfig.get(Features.HUD_SCALE);

		int screenWidth = graphics.guiWidth();
		int screenHeight = graphics.guiHeight();
		int x = corner == 1 || corner == 3 ? screenWidth - boxWidth - 4 : 4;
		int y = corner == 2 || corner == 3 ? screenHeight - boxHeight - 4 : 4;

		if (VloxConfig.flag(Features.HUD_BACKGROUND)) {
			int backdrop = Math.round(154 * (float) VloxConfig.get(Features.HUD_BACKDROP_OPACITY));
			// Accent underneath, panel on top: the accent border follows the panel's own
			// curve and runs the full height, with no dark caps at either end.
			Ui.rounded(graphics, x, y, boxWidth, boxHeight, 6, Ui.alpha(Ui.accent(), 200));
			Ui.rounded(graphics, x + 3, y, boxWidth - 3, boxHeight, 6, Ui.alpha(0x0A0F15, backdrop));
		}

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate((float) x, (float) y);
		pose.scale(scale, scale);

		boolean shadow = VloxConfig.flag(Features.HUD_SHADOW);
		int cursorY = PAD;
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			row.draw(graphics, font, PAD + 4, cursorY, shadow);
			cursorY += row.height(font) + gap;
		}

		pose.popMatrix();
	}

	/** Rebuilds the row list and the panel size. Runs a few times a second, not per frame. */
	private void rebuild(Minecraft minecraft) {
		rows.clear();
		gap = (int) Math.round(VloxConfig.get(Features.HUD_SPACING));
		corner = VloxConfig.index(Features.HUD_CORNER);
		builtRevision = VloxConfig.revision();

		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null) {
			return;
		}

		Font font = minecraft.font;
		BlockPos pos = player.blockPosition();

		if (VloxConfig.flag(Features.HUD_FPS)) {
			line("FPS  ").append(minecraft.getFps());
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_COORDS)) {
			line("XYZ  ");
			appendFixed(text, player.getX(), 1);
			text.append(" / ");
			appendFixed(text, player.getY(), 1);
			text.append(" / ");
			appendFixed(text, player.getZ(), 1);
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_NETHER)) {
			line("Nether  ");
			appendFixed(text, player.getX() / 8.0, 0);
			text.append(" / ");
			appendFixed(text, player.getZ() / 8.0, 0);
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_FACING)) {
			line("Facing  ").append(compass(player.getYRot()));
			text.append("  ").append(Math.round(player.getYRot())).append('\u00B0');
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_SPEED)) {
			double dx = player.getDeltaMovement().x;
			double dz = player.getDeltaMovement().z;
			line("Speed  ");
			appendFixed(text, Math.sqrt(dx * dx + dz * dz) * 20.0, 2);
			text.append(" b/s");
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_BIOME)) {
			String biome = minecraft.level.getBiome(pos).unwrapKey()
					.map(key -> key.identifier().getPath())
					.orElse("unknown");
			line("Biome  ").append(biome);
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_DIMENSION)) {
			line("Dimension  ").append(minecraft.level.dimension().identifier().getPath());
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_TIME)) {
			long time = minecraft.level.getDefaultClockTime() % 24000L;
			int hours = (int) ((time / 1000L + 6L) % 24L);
			int minutes = (int) ((time % 1000L) * 60L / 1000L);
			line("Time  ");
			pad2(hours);
			text.append(':');
			pad2(minutes);
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_DAY)) {
			line("Day  ").append(minecraft.level.getDefaultClockTime() / 24000L);
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_CHUNK)) {
			line("Chunk  ").append(pos.getX() >> 4).append(" / ").append(pos.getZ() >> 4);
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_LIGHT)) {
			line("Light  ").append(minecraft.level.getMaxLocalRawBrightness(pos));
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_MEMORY)) {
			Runtime runtime = Runtime.getRuntime();
			long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
			long max = runtime.maxMemory() / (1024L * 1024L);
			line("Memory  ").append(used).append(" / ").append(max).append(" MB");
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_ENTITIES)) {
			line("Entities  ").append(minecraft.level.getEntityCount());
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_PING)) {
			line("Ping  ").append(ping(minecraft)).append(" ms");
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_HEALTH)) {
			line("Health  ");
			appendFixed(text, player.getHealth(), 1);
			if (player.getAbsorptionAmount() > 0.0F) {
				text.append("  +");
				appendFixed(text, player.getAbsorptionAmount(), 1);
			}
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_HUNGER)) {
			line("Hunger  ").append(player.getFoodData().getFoodLevel())
					.append("  sat ").append(Math.round(player.getFoodData().getSaturationLevel()));
			addLine();
		}

		int armourMode = VloxConfig.index(Features.HUD_ARMOR);
		if (armourMode == 0) {
			line("Armour  ").append(player.getArmorValue());
			addLine();
		} else if (armourMode > 0) {
			addArmour(player, armourMode == 2);
		}

		if (VloxConfig.flag(Features.HUD_HAND)) {
			addHeldItem(player);
		}
		if (VloxConfig.flag(Features.HUD_XP)) {
			line("XP  ").append(player.experienceLevel).append("  ")
					.append(Math.round(player.experienceProgress * 100.0F)).append('%');
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_SPRINT) && player.isSprinting()) {
			line("Sprinting");
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_SNEAK) && player.isCrouching()) {
			line("Sneaking");
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_KEYSTROKES)) {
			rows.add(new KeysRow());
		}
		if (VloxConfig.flag(Features.HUD_CPS)) {
			line("CPS  ").append(CombatInput.cps(false)).append(" / ").append(CombatInput.cps(true));
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_ITEMS)) {
			addPvpItems(player);
		}

		int effectMode = VloxConfig.index(Features.HUD_EFFECTS);
		if (effectMode == 1) {
			line("Effects  ").append(player.getActiveEffects().size());
			addLine();
		} else if (effectMode == 2) {
			addEffectTimers(player);
		}

		if (VloxConfig.flag(Features.HUD_LOOKING)) {
			HitResult hit = minecraft.hitResult;
			if (hit instanceof BlockHitResult blockHit) {
				line("Looking at  ").append(blockHit.getBlockPos().toShortString());
				addLine();
			}
		}
		if (VloxConfig.flag(Features.HUD_BLOCKID)) {
			HitResult hit = minecraft.hitResult;
			if (hit instanceof BlockHitResult blockHit) {
				var state = minecraft.level.getBlockState(blockHit.getBlockPos());
				line("Block  ").append(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()));
				addLine();
			}
		}
		if (VloxConfig.flag(Features.HUD_PLAYTIME)) {
			long seconds = (System.currentTimeMillis() - VloxClient.SESSION_START) / 1000L;
			line("Session  ").append(seconds / 60).append(':');
			pad2((int) (seconds % 60));
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_CLOCK)) {
			LocalTime now = LocalTime.now();
			line("Clock  ");
			pad2(now.getHour());
			text.append(':');
			pad2(now.getMinute());
			text.append(':');
			pad2(now.getSecond());
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_SERVER)) {
			line("Server  ").append(serverLabel(minecraft));
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_FALL) && player.fallDistance > 0.0F) {
			line("Falling  ");
			appendFixed(text, player.fallDistance, 1);
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_DIFFICULTY)) {
			line("Difficulty  ").append(minecraft.level.getDifficulty());
			addLine();
		}
		if (VloxConfig.flag(Features.HUD_DEATH_POINT)) {
			addDeathPoint(minecraft);
		}

		if (rows.isEmpty()) {
			boxWidth = 0;
			boxHeight = 0;
			return;
		}

		float scale = (float) VloxConfig.get(Features.HUD_SCALE);
		int textWidth = 0;
		int height = 0;
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			textWidth = Math.max(textWidth, row.width(font));
			height += row.height(font);
			if (i > 0) {
				height += gap;
			}
		}
		boxWidth = Math.round((textWidth + PAD * 2) * scale);
		boxHeight = Math.round((height + PAD * 2) * scale);
	}

	/** The stored death position, with the distance to it when it is in this dimension. */
	private void addDeathPoint(Minecraft minecraft) {
		if (!VloxConfig.hasText("death.x")) {
			line("Last death  none yet");
			addLine();
			return;
		}

		double x = number("death.x");
		double y = number("death.y");
		double z = number("death.z");
		String dimension = VloxConfig.getText("death.dim", "");
		boolean inThisDimension = dimension.equals(minecraft.level.dimension().identifier().toString());

		line("Last death  ");
		appendFixed(text, x, 0);
		text.append(" / ");
		appendFixed(text, y, 0);
		text.append(" / ");
		appendFixed(text, z, 0);

		if (inThisDimension) {
			LocalPlayer player = minecraft.player;
			double dx = player.getX() - x;
			double dy = player.getY() - y;
			double dz = player.getZ() - z;
			text.append("  ");
			appendFixed(text, Math.sqrt(dx * dx + dy * dy + dz * dz), 0);
			text.append('m');
		} else if (!dimension.isEmpty()) {
			int colon = dimension.indexOf(':');
			text.append("  ").append(colon >= 0 ? dimension.substring(colon + 1) : dimension);
		}
		addLine();
	}

	private static double number(String key) {
		try {
			return Double.parseDouble(VloxConfig.getText(key, "0"));
		} catch (NumberFormatException e) {
			return 0.0D;
		}
	}

	/** Draws the four equipped armour pieces as their real items. */
	private void addArmour(LocalPlayer player, boolean durability) {
		ItemStack[] worn = new ItemStack[4];
		EquipmentSlot[] slots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
		int count = 0;
		for (EquipmentSlot slot : slots) {
			ItemStack stack = player.getItemBySlot(slot);
			if (!stack.isEmpty()) {
				worn[count++] = stack;
			}
		}
		if (count == 0) {
			line("Armour  none");
			addLine();
			return;
		}
		ItemStack[] trimmed = new ItemStack[count];
		System.arraycopy(worn, 0, trimmed, 0, count);
		rows.add(new ItemRow(trimmed, durability));
	}

	private void addHeldItem(LocalPlayer player) {
		ItemStack stack = player.getMainHandItem();
		String hand = player.getMainArm() == HumanoidArm.LEFT ? "L" : "R";
		line("Hand (").append(hand).append(")  ");
		if (stack.isEmpty()) {
			text.append("empty");
			addLine();
			return;
		}
		int remaining = stack.getMaxDamage() - stack.getDamageValue();
		if (stack.isDamageableItem()) {
			text.append(remaining).append('/').append(stack.getMaxDamage());
			addLine();
			rows.add(new ItemRow(new ItemStack[] { stack }, true));
			return;
		}
		text.append(stack.getCount()).append("x ").append(stack.getHoverName().getString());
		addLine();
	}

	/** Totems, pearls, arrows and golden apples, so a fight does not need the inventory open. */
	private void addPvpItems(LocalPlayer player) {
		int totems = 0;
		int pearls = 0;
		int arrows = 0;
		int apples = 0;

		var inventory = player.getInventory();
		int size = inventory.getContainerSize();
		for (int i = 0; i < size; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			var item = stack.getItem();
			int count = stack.getCount();
			if (item == Items.TOTEM_OF_UNDYING) {
				totems += count;
			} else if (item == Items.ENDER_PEARL) {
				pearls += count;
			} else if (item == Items.ARROW || item == Items.SPECTRAL_ARROW) {
				arrows += count;
			} else if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
				apples += count;
			}
		}

		if (totems + pearls + arrows + apples == 0) {
			return;
		}

		text.setLength(0);
		boolean first = true;
		if (totems > 0) {
			first = appendCounter("Totem ", totems, first);
		}
		if (pearls > 0) {
			first = appendCounter("Pearl ", pearls, first);
		}
		if (arrows > 0) {
			first = appendCounter("Arrow ", arrows, first);
		}
		if (apples > 0) {
			appendCounter("Gapple ", apples, first);
		}
		rows.add(new TextRow(text.toString()));
	}

	private boolean appendCounter(String label, int value, boolean first) {
		if (!first) {
			text.append("   ");
		}
		text.append(label).append(value);
		return false;
	}

	/** Active effects with the time left, soonest first. */
	private void addEffectTimers(LocalPlayer player) {
		Collection<MobEffectInstance> active = player.getActiveEffects();
		if (active.isEmpty()) {
			return;
		}

		int count = 0;
		for (MobEffectInstance effect : active) {
			if (count >= effects.length) {
				break;
			}
			effects[count++] = effect;
		}

		for (int i = 1; i < count; i++) {
			MobEffectInstance current = effects[i];
			int j = i - 1;
			while (j >= 0 && effects[j].getDuration() > current.getDuration()) {
				effects[j + 1] = effects[j];
				j--;
			}
			effects[j + 1] = current;
		}

		for (int i = 0; i < count; i++) {
			MobEffectInstance effect = effects[i];
			effects[i] = null;

			text.setLength(0);
			text.append(effect.getEffect().value().getDisplayName().getString());
			int amplifier = effect.getAmplifier();
			if (amplifier > 0 && amplifier < ROMAN.length) {
				text.append(ROMAN[amplifier]);
			}
			text.append("  ");
			if (effect.isInfiniteDuration()) {
				text.append("inf");
			} else {
				int seconds = effect.getDuration() / 20;
				text.append(seconds / 60).append(':');
				pad2(seconds % 60);
			}
			rows.add(new TextRow(text.toString()));
		}
	}

	private StringBuilder line(String label) {
		text.setLength(0);
		return text.append(label);
	}

	private void addLine() {
		rows.add(new TextRow(text.toString()));
	}

	/** Appends a double with a fixed number of decimals, without String.format's locale work. */
	private static void appendFixed(StringBuilder out, double value, int decimals) {
		long factor = switch (decimals) {
			case 0 -> 1L;
			case 1 -> 10L;
			case 2 -> 100L;
			default -> 1000L;
		};
		long scaled = Math.round(value * factor);
		if (scaled < 0L) {
			out.append('-');
			scaled = -scaled;
		}
		out.append(scaled / factor);
		if (decimals > 0) {
			out.append('.');
			long fraction = scaled % factor;
			for (long divisor = factor / 10L; divisor > 0L; divisor /= 10L) {
				out.append((char) ('0' + (fraction / divisor) % 10L));
			}
		}
	}

	private void pad2(int value) {
		if (value < 10) {
			text.append('0');
		}
		text.append(value);
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
		return COMPASS[index];
	}

	/** One line of the HUD: plain text, a strip of items, or the keystroke block. */
	private sealed interface Row permits TextRow, ItemRow, KeysRow {
		int width(Font font);

		int height(Font font);

		void draw(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean shadow);
	}

	private record TextRow(String text) implements Row {
		@Override
		public int width(Font font) {
			return font.width(this.text);
		}

		@Override
		public int height(Font font) {
			return font.lineHeight;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean shadow) {
			graphics.text(font, this.text, x, y, ON, shadow);
		}
	}

	private record ItemRow(ItemStack[] stacks, boolean durability) implements Row {
		@Override
		public int width(Font font) {
			return Math.max(1, this.stacks.length * 18 - 2);
		}

		@Override
		public int height(Font font) {
			return 18;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean shadow) {
			for (int i = 0; i < this.stacks.length; i++) {
				int dx = x + i * 18;
				graphics.item(this.stacks[i], dx, y + 1);
				if (this.durability) {
					graphics.itemDecorations(font, this.stacks[i], dx, y + 1);
				}
			}
		}
	}

	/** WASD plus both mouse buttons, lit while held: the classic PvP keystroke block. */
	private record KeysRow() implements Row {
		private static final int KEY_WIDTH = 13;
		private static final int KEY_HEIGHT = 11;
		private static final int KEY_GAP = 1;
		private static final int MOUSE_WIDTH = 20;
		private static final int BLOCK_WIDTH = KEY_WIDTH * 3 + KEY_GAP * 2;
		private static final int MOUSE_X = BLOCK_WIDTH + 4;

		@Override
		public int width(Font font) {
			return MOUSE_X + MOUSE_WIDTH;
		}

		@Override
		public int height(Font font) {
			return KEY_HEIGHT * 2 + KEY_GAP;
		}

		@Override
		public void draw(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean shadow) {
			int secondRow = y + KEY_HEIGHT + KEY_GAP;
			key(graphics, font, x + KEY_WIDTH + KEY_GAP, y, "W", CombatInput.up);
			key(graphics, font, x, secondRow, "A", CombatInput.left);
			key(graphics, font, x + KEY_WIDTH + KEY_GAP, secondRow, "S", CombatInput.down);
			key(graphics, font, x + (KEY_WIDTH + KEY_GAP) * 2, secondRow, "D", CombatInput.right);
			key(graphics, font, x + MOUSE_X, y, MOUSE_WIDTH, "LMB", CombatInput.attack);
			key(graphics, font, x + MOUSE_X, secondRow, MOUSE_WIDTH, "RMB", CombatInput.use);

			if (CombatInput.jump || CombatInput.sprint || CombatInput.sneak) {
				int dotX = x + MOUSE_X + MOUSE_WIDTH + 3;
				int dotY = y + 3;
				if (CombatInput.jump) {
					graphics.fill(dotX, dotY, dotX + 3, dotY + 3, Ui.accent());
					dotY += 5;
				}
				if (CombatInput.sprint) {
					graphics.fill(dotX, dotY, dotX + 3, dotY + 3, Ui.accent());
					dotY += 5;
				}
				if (CombatInput.sneak) {
					graphics.fill(dotX, dotY, dotX + 3, dotY + 3, Ui.accent());
				}
			}
		}

		private static void key(GuiGraphicsExtractor graphics, Font font, int x, int y, String label, boolean pressed) {
			key(graphics, font, x, y, KEY_WIDTH, label, pressed);
		}

		private static void key(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, String label,
				boolean pressed) {
			int background = pressed ? Ui.alpha(Ui.accent(), 215) : 0x8A0A0F15;
			int border = pressed ? Ui.accent() : 0x3AFFFFFF;
			graphics.fill(x, y, x + width, y + KEY_HEIGHT, border);
			graphics.fill(x + 1, y + 1, x + width - 1, y + KEY_HEIGHT - 1, background);
			graphics.centeredText(font, label, x + width / 2,
					y + (KEY_HEIGHT - font.lineHeight) / 2 + 1, pressed ? 0xFF0A0F15 : ON);
		}
	}
}
