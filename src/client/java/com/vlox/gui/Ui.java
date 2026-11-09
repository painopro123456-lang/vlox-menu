package com.vlox.gui;

import com.vlox.config.VloxConfig;
import com.vlox.feature.Features;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/** Small drawing helpers so the menu can be laid out by hand. */
public final class Ui {
	public static final int TEXT = 0xFFF2F6FA;
	public static final int MUTED = 0xFF8290A3;
	public static final int FAINT = 0xFF46515F;
	public static final int TRACK = 0xFF1B2430;

	/** The selectable accent colours, in menu order. */
	private static final int[] ACCENTS = {
			0xFF35E08A, 0xFF3AC8FF, 0xFFFFB03A, 0xFFA97BFF, 0xFFFF6F9B
	};

	private static int accent = ACCENTS[0];
	private static long accentRevision = Long.MIN_VALUE;

	private Ui() {
	}

	/**
	 * The current accent colour. Re-read only when the config changes, so drawing
	 * code can call this freely.
	 */
	public static int accent() {
		long revision = VloxConfig.revision();
		if (revision != accentRevision) {
			accentRevision = revision;
			int index = VloxConfig.index(Features.MENU_ACCENT);
			accent = ACCENTS[Math.max(0, Math.min(ACCENTS.length - 1, index))];
		}
		return accent;
	}

	public static int alpha(int color, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
	}

	public static float lerp(float from, float to, float t) {
		return from + (to - from) * Math.max(0.0F, Math.min(1.0F, t));
	}

	public static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	/** A filled rectangle with softly rounded corners. */
	public static void rounded(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int r = Math.min(radius, Math.min(width / 2, height / 2));
		if (r <= 0) {
			graphics.fill(x, y, x + width, y + height, color);
			return;
		}

		graphics.fill(x + r, y, x + width - r, y + height, color);
		graphics.fill(x, y + r, x + r, y + height - r, color);
		graphics.fill(x + width - r, y + r, x + width, y + height - r, color);

		for (int row = 0; row < r; row++) {
			int dy = r - row - 1;
			int inset = r - (int) Math.sqrt(Math.max(0, r * r - dy * dy));
			graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
			graphics.fill(x + inset, y + height - row - 1, x + width - inset, y + height - row, color);
		}
	}

	/** A rounded rectangle with a soft shadow underneath. */
	public static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
		rounded(graphics, x + 2, y + 5, width, height, radius, 0x66000000);
		rounded(graphics, x + 1, y + 2, width, height, radius, 0x33000000);
		rounded(graphics, x, y, width, height, radius, color);
	}

	public static void text(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color) {
		graphics.text(font, text, x, y, color, false);
	}

	public static void textScaled(GuiGraphicsExtractor graphics, Font font, String text, int x, int y,
			int color, float scale) {
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate((float) x, (float) y);
		pose.scale(scale, scale);
		graphics.text(font, text, 0, 0, color, false);
		pose.popMatrix();
	}

	/** Truncates text with an ellipsis so it fits inside {@code maxWidth}. */
	public static String fit(Font font, String text, int maxWidth) {
		if (font.width(text) <= maxWidth) {
			return text;
		}
		String ellipsis = "...";
		int limit = maxWidth - font.width(ellipsis);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (font.width(builder.toString() + c) > limit) {
				break;
			}
			builder.append(c);
		}
		return builder + ellipsis;
	}

	public static String pretty(String enumName) {
		String lower = enumName.toLowerCase().replace('_', ' ');
		if (lower.isEmpty()) {
			return lower;
		}
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}
}
