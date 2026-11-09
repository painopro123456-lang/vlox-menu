package com.vlox.gui;

import com.vlox.config.VloxConfig;
import com.vlox.feature.Category;
import com.vlox.feature.Feature;
import com.vlox.feature.Features;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** The VLOX settings menu. */
public final class VloxScreen extends Screen {
	private static final int PANEL_WIDTH = 460;
	private static final int ROW_HEIGHT = 20;
	private static final int ROW_HEIGHT_COMPACT = 16;
	private static final int CONTROL_WIDTH = 54;
	private static final int BG = 0xF00B0F14;
	private static final int PANEL = 0xFF11161E;
	private static final int ROW_ALT = 0xFF161C26;
	private static final int ACCENT = 0xFF35E08A;
	private static final int MUTED = 0xFF8A9BB0;

	private static Category lastCategory = Category.HUD;

	private final List<AbstractWidget> rowWidgets = new ArrayList<>();
	private final List<Feature> shown = new ArrayList<>();

	private EditBox search;
	private String query = "";
	private int scroll;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int columns;

	public VloxScreen() {
		super(Component.literal("VLOX"));
	}

	@Override
	protected void init() {
		this.panelWidth = Math.min(PANEL_WIDTH, this.width - 40);
		this.panelHeight = Math.min(340, this.height - 40);
		this.panelX = (this.width - this.panelWidth) / 2;
		this.panelY = (this.height - this.panelHeight) / 2;
		this.columns = this.panelWidth >= 520 ? 2 : 1;

		if (!VloxConfig.flag(Features.MENU_REMEMBER)) {
			lastCategory = Category.HUD;
		}

		buildTabs();
		buildSearch();
		buildRows();

		if (VloxConfig.flag(Features.MENU_SEARCH_FOCUS) && this.search != null) {
			this.setFocused(this.search);
		}
	}

	private Category category() {
		return lastCategory;
	}

	private void buildTabs() {
		int tabCount = Category.values().length;
		int gap = 4;
		int tabWidth = (this.panelWidth - 20 - gap * (tabCount - 1)) / tabCount;
		int y = this.panelY + 26;

		for (Category cat : Category.values()) {
			int index = cat.ordinal();
			int x = this.panelX + 10 + index * (tabWidth + gap);
			Button button = Button.builder(Component.literal(cat.label), b -> {
				lastCategory = cat;
				this.scroll = 0;
				this.rebuildWidgets();
			}).bounds(x, y, tabWidth, 18).build();

			if (cat == category()) {
				button.setMessage(Component.literal("> " + cat.label + " <"));
			}
			this.addRenderableWidget(button);
		}
	}

	private void buildSearch() {
		int y = this.panelY + 48;
		this.search = new EditBox(this.font, this.panelX + 10, y, this.panelWidth - 20, 18, Component.literal("Search"));
		this.search.setHint(Component.literal("Search " + Features.ALL.size() + " settings"));
		this.search.setValue(this.query);
		this.search.setResponder(value -> {
			this.query = value == null ? "" : value;
			this.scroll = 0;
			buildRows();
		});
		this.addRenderableWidget(this.search);
	}

	private void buildRows() {
		for (AbstractWidget widget : this.rowWidgets) {
			this.removeWidget(widget);
		}
		this.rowWidgets.clear();
		this.shown.clear();

		String needle = this.query.trim().toLowerCase();
		for (Feature feature : Features.ALL) {
			if (feature.category != category()) {
				continue;
			}
			if (!needle.isEmpty()
					&& !feature.label.toLowerCase().contains(needle)
					&& !feature.id.toLowerCase().contains(needle)
					&& !feature.description.toLowerCase().contains(needle)) {
				continue;
			}
			this.shown.add(feature);
		}

		int contentTop = this.panelY + 72;
		int contentBottom = this.panelY + this.panelHeight - 10;
		int rowHeight = VloxConfig.flag(Features.MENU_COMPACT) ? ROW_HEIGHT_COMPACT : ROW_HEIGHT;
		int perColumn = Math.max(1, (contentBottom - contentTop) / rowHeight);
		int perPage = perColumn * this.columns;

		if (this.scroll > Math.max(0, this.shown.size() - 1)) {
			this.scroll = Math.max(0, this.shown.size() - 1);
		}

		int columnWidth = (this.panelWidth - 20) / this.columns;
		for (int i = 0; i < Math.min(perPage, this.shown.size() - this.scroll); i++) {
			int absolute = this.scroll + i;
			Feature feature = this.shown.get(absolute);
			int column = i / perColumn;
			int row = i % perColumn;
			int x = this.panelX + 10 + column * columnWidth;
			int y = contentTop + row * rowHeight;
			buildRow(feature, x, y, columnWidth - 4, rowHeight);
		}
	}

	private void buildRow(Feature feature, int x, int y, int width, int rowHeight) {
		int controlX = x + width - CONTROL_WIDTH;
		int controlHeight = rowHeight - 4;
		int controlY = y + 2;

		switch (feature.type) {
			case TOGGLE -> {
				Button button = Button.builder(Component.literal(VloxConfig.flag(feature) ? "On" : "Off"), b -> {
					VloxConfig.setFlag(feature, !VloxConfig.flag(feature));
					b.setMessage(Component.literal(VloxConfig.flag(feature) ? "On" : "Off"));
				}).bounds(controlX, controlY, CONTROL_WIDTH, controlHeight).build();
				addRowWidget(button);
			}
			case NUMBER -> {
				int small = CONTROL_WIDTH / 2 - 1;
				Button minus = Button.builder(Component.literal("-"), b -> {
					VloxConfig.nudge(feature, -feature.step);
					Features.apply(feature);
				}).bounds(controlX, controlY, small, controlHeight).build();
				Button plus = Button.builder(Component.literal("+"), b -> {
					VloxConfig.nudge(feature, feature.step);
					Features.apply(feature);
				}).bounds(controlX + small + 2, controlY, small, controlHeight).build();
				addRowWidget(minus);
				addRowWidget(plus);
			}
			case CHOICE -> {
				Button button = Button.builder(Component.literal(VloxConfig.get(feature) == 0 ? feature.choices[0] : ""), b -> {
					int next = (VloxConfig.index(feature) + 1) % feature.choices.length;
					VloxConfig.set(feature, next);
					Features.apply(feature);
					b.setMessage(Component.literal(feature.choices[next]));
				}).bounds(controlX, controlY, CONTROL_WIDTH, controlHeight).build();
				button.setMessage(Component.literal(feature.describeValue(VloxConfig.get(feature))));
				addRowWidget(button);
			}
			case ACTION -> {
				Button button = Button.builder(Component.literal("Run"), b -> runAction(feature))
						.bounds(controlX, controlY, CONTROL_WIDTH, controlHeight).build();
				addRowWidget(button);
			}
			default -> {
			}
		}
	}

	private void addRowWidget(AbstractWidget widget) {
		this.rowWidgets.add(widget);
		this.addRenderableWidget(widget);
	}

	private void runAction(Feature feature) {
		if (feature == Features.MENU_RESET) {
			VloxConfig.resetAll();
			this.rebuildWidgets();
			return;
		}
		if (feature == Features.MENU_COPY_PATH) {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft != null && minecraft.keyboardHandler != null) {
				minecraft.keyboardHandler.setClipboard(VloxConfig.file().toAbsolutePath().toString());
			}
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, this.width, this.height, 0xC0000000);
		graphics.fill(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight, BG);
		graphics.fill(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + 22, PANEL);
		graphics.fill(this.panelX, this.panelY + this.panelHeight - 16, this.panelX + this.panelWidth,
				this.panelY + this.panelHeight, PANEL);

		String title = "VLOX";
		if (VloxConfig.flag(Features.MENU_SHOW_FPS)) {
			title += "   " + Minecraft.getInstance().getFps() + " fps";
		}
		graphics.text(this.font, title, this.panelX + 10, this.panelY + 7, ACCENT, true);

		String count = this.shown.size() + " / " + Features.ALL.size();
		graphics.text(this.font, count, this.panelX + this.panelWidth - 10 - this.font.width(count),
				this.panelY + 7, MUTED, true);

		drawRows(graphics);

		String hint = "Right shift closes - scroll to page";
		graphics.text(this.font, hint, this.panelX + 10, this.panelY + this.panelHeight - 12, MUTED, false);

		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private void drawRows(GuiGraphicsExtractor graphics) {
		int contentTop = this.panelY + 72;
		int contentBottom = this.panelY + this.panelHeight - 18;
		int rowHeight = VloxConfig.flag(Features.MENU_COMPACT) ? ROW_HEIGHT_COMPACT : ROW_HEIGHT;
		int perColumn = Math.max(1, (contentBottom - contentTop) / rowHeight);
		int perPage = perColumn * this.columns;
		int columnWidth = (this.panelWidth - 20) / this.columns;

		for (int i = 0; i < Math.min(perPage, this.shown.size() - this.scroll); i++) {
			Feature feature = this.shown.get(this.scroll + i);
			int column = i / perColumn;
			int row = i % perColumn;
			int x = this.panelX + 10 + column * columnWidth;
			int y = contentTop + row * rowHeight;

			graphics.fill(x, y, x + columnWidth - 4, y + rowHeight - 2, row % 2 == 0 ? ROW_ALT : PANEL);

			int textY = y + (rowHeight - 2 - this.font.lineHeight) / 2;
			if (VloxConfig.flag(Features.MENU_DESCRIPTIONS) && !VloxConfig.flag(Features.MENU_COMPACT)) {
				graphics.text(this.font, feature.label, x + 5, textY - 4, 0xFFE8EEF6, false);
				graphics.text(this.font, feature.description, x + 5, textY + 6, MUTED, false);
			} else {
				graphics.text(this.font, feature.label, x + 5, textY, 0xFFE8EEF6, false);
			}

			if (feature.type == Feature.Type.NUMBER) {
				String value = feature.describeValue(VloxConfig.get(feature));
				graphics.text(this.font, value, x + columnWidth - CONTROL_WIDTH - 8 - this.font.width(value),
						textY, ACCENT, false);
			}
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (vertical != 0) {
			int contentTop = this.panelY + 72;
			int contentBottom = this.panelY + this.panelHeight - 18;
			int rowHeight = VloxConfig.flag(Features.MENU_COMPACT) ? ROW_HEIGHT_COMPACT : ROW_HEIGHT;
			int perColumn = Math.max(1, (contentBottom - contentTop) / rowHeight);
			int step = Math.max(1, perColumn);

			this.scroll = Math.max(0, this.scroll - (int) Math.signum(vertical) * step);
			buildRows();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}
}
