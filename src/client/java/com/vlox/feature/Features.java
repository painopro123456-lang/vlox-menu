package com.vlox.feature;

import com.vlox.config.VloxConfig;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

import java.util.ArrayList;
import java.util.List;

/**
 * Every setting VLOX exposes, grouped by menu category.
 *
 * Features that drive a vanilla option are forwarded in {@link #apply(Feature)},
 * which is called on change and once at startup. Features that only affect
 * VLOX's own HUD are read while drawing and need no forwarding.
 */
public final class Features {
	public static final List<Feature> ALL = new ArrayList<>();

	public static final Feature HUD_ENABLED = toggle(Category.HUD, "hud.enabled", "HUD overlay",
			"Master switch for every VLOX HUD line.", true);
	public static final Feature HUD_BACKGROUND = toggle(Category.HUD, "hud.background", "Backdrop",
			"Draw a dark panel behind the HUD text.", true);
	public static final Feature HUD_SHADOW = toggle(Category.HUD, "hud.shadow", "Text shadow",
			"Draw the drop shadow behind HUD text.", true);
	public static final Feature HUD_CORNER = choice(Category.HUD, "hud.corner", "Corner",
			"Which screen corner the HUD sits in.", new String[] { "Top left", "Top right", "Bottom left", "Bottom right" }, 0);
	public static final Feature HUD_SCALE = number(Category.HUD, "hud.scale", "Scale",
			"HUD text size multiplier.", 0.5, 2.0, 0.1, 1.0);
	public static final Feature HUD_SPACING = number(Category.HUD, "hud.spacing", "Line spacing",
			"Gap between HUD lines in pixels.", 1, 6, 1, 2);

	public static final Feature HUD_FPS = toggle(Category.HUD, "hud.fps", "FPS", "Frames per second.", true);
	public static final Feature HUD_COORDS = toggle(Category.HUD, "hud.coords", "Coordinates", "Your X, Y and Z position.", true);
	public static final Feature HUD_NETHER = toggle(Category.HUD, "hud.nether", "Nether coords", "Overworld position converted to Nether scale.", false);
	public static final Feature HUD_FACING = toggle(Category.HUD, "hud.facing", "Facing", "Compass direction and yaw.", false);
	public static final Feature HUD_SPEED = toggle(Category.HUD, "hud.speed", "Speed", "Movement speed in blocks per second.", false);
	public static final Feature HUD_BIOME = toggle(Category.HUD, "hud.biome", "Biome", "The biome you are standing in.", false);
	public static final Feature HUD_DIMENSION = toggle(Category.HUD, "hud.dimension", "Dimension", "Current dimension id.", false);
	public static final Feature HUD_TIME = toggle(Category.HUD, "hud.time", "World time", "In-game time of day.", false);
	public static final Feature HUD_DAY = toggle(Category.HUD, "hud.day", "Day count", "Days since the world started.", false);
	public static final Feature HUD_CHUNK = toggle(Category.HUD, "hud.chunk", "Chunk position", "Which chunk you are in.", false);
	public static final Feature HUD_LIGHT = toggle(Category.HUD, "hud.light", "Light level", "Block and sky light where you stand.", false);
	public static final Feature HUD_MEMORY = toggle(Category.HUD, "hud.memory", "Memory", "Used and maximum JVM memory.", false);
	public static final Feature HUD_ENTITIES = toggle(Category.HUD, "hud.entities", "Entities", "Entities currently being rendered.", false);
	public static final Feature HUD_PING = toggle(Category.HUD, "hud.ping", "Ping", "Latency to the current server.", false);
	public static final Feature HUD_HEALTH = toggle(Category.HUD, "hud.health", "Health", "Health and absorption as numbers.", false);
	public static final Feature HUD_HUNGER = toggle(Category.HUD, "hud.hunger", "Hunger", "Food level and saturation.", false);
	public static final Feature HUD_ARMOR = toggle(Category.HUD, "hud.armor", "Armour", "Total armour points.", false);
	public static final Feature HUD_HAND = toggle(Category.HUD, "hud.hand", "Held item", "Name, count and durability of the held item.", false);
	public static final Feature HUD_XP = toggle(Category.HUD, "hud.xp", "Experience", "Level and progress to the next level.", false);
	public static final Feature HUD_SPRINT = toggle(Category.HUD, "hud.sprint", "Sprint state", "Shows when you are sprinting.", false);
	public static final Feature HUD_SNEAK = toggle(Category.HUD, "hud.sneak", "Sneak state", "Shows when you are sneaking.", false);
	public static final Feature HUD_LOOKING = toggle(Category.HUD, "hud.looking", "Looking at", "The block or entity under your crosshair.", false);
	public static final Feature HUD_BLOCKID = toggle(Category.HUD, "hud.blockid", "Block id", "Registry id of the targeted block.", false);
	public static final Feature HUD_PLAYTIME = toggle(Category.HUD, "hud.playtime", "Session time", "How long this game session has been open.", false);
	public static final Feature HUD_CLOCK = toggle(Category.HUD, "hud.clock", "Real clock", "Your system time.", false);
	public static final Feature HUD_SERVER = toggle(Category.HUD, "hud.server", "Server", "Address of the server you are on.", false);
	public static final Feature HUD_EFFECTS = toggle(Category.HUD, "hud.effects", "Effects", "Active potion effect count.", false);
	public static final Feature HUD_FALL = toggle(Category.HUD, "hud.fall", "Fall distance", "Distance fallen, so you know when to water bucket.", false);
	public static final Feature HUD_DIFFICULTY = toggle(Category.HUD, "hud.difficulty", "Difficulty", "Current world difficulty.", false);

	public static final Feature VIDEO_RENDER_DISTANCE = number(Category.GRAPHICS, "video.renderDistance", "Render distance",
			"Chunks drawn in each direction.", 2, 32, 1, 12);
	public static final Feature VIDEO_SIM_DISTANCE = number(Category.GRAPHICS, "video.simulationDistance", "Simulation distance",
			"Chunks that keep ticking.", 2, 32, 1, 12);
	public static final Feature VIDEO_ENTITY_DISTANCE = number(Category.GRAPHICS, "video.entityDistance", "Entity distance",
			"How far entities render, as a fraction of render distance.", 0.5, 2.0, 0.05, 1.0);
	public static final Feature VIDEO_FPS_LIMIT = number(Category.GRAPHICS, "video.fpsLimit", "FPS limit",
			"Maximum framerate.", 30, 260, 10, 120);
	public static final Feature VIDEO_VSYNC = toggle(Category.GRAPHICS, "video.vsync", "VSync",
			"Sync frames to the display refresh rate.", false);
	public static final Feature VIDEO_CLOUDS = choice(Category.GRAPHICS, "video.clouds", "Clouds",
			"Cloud rendering quality.", new String[] { "Off", "Fast", "Fancy" }, 2);
	public static final Feature VIDEO_CLOUD_RANGE = number(Category.GRAPHICS, "video.cloudRange", "Cloud range",
			"How far clouds draw.", 0, 128, 4, 64);
	public static final Feature VIDEO_WEATHER_RADIUS = number(Category.GRAPHICS, "video.weatherRadius", "Weather radius",
			"Radius of rain and snow rendering.", 1, 32, 1, 10);
	public static final Feature VIDEO_CUTOUT_LEAVES = toggle(Category.GRAPHICS, "video.cutoutLeaves", "Cutout leaves",
			"Draw leaves with transparent cutouts instead of solid.", true);
	public static final Feature VIDEO_VIGNETTE = toggle(Category.GRAPHICS, "video.vignette", "Vignette",
			"Darken the screen edges.", true);
	public static final Feature VIDEO_TRANSPARENCY = toggle(Category.GRAPHICS, "video.improvedTransparency", "Improved transparency",
			"Sort translucent geometry for fewer visual glitches.", true);
	public static final Feature VIDEO_AO = toggle(Category.GRAPHICS, "video.ambientOcclusion", "Ambient occlusion",
			"Softer shading in corners.", true);
	public static final Feature VIDEO_CHUNK_FADE = number(Category.GRAPHICS, "video.chunkFade", "Chunk fade in",
			"Seconds a newly loaded chunk takes to fade in.", 0, 2.5, 0.25, 0.5);
	public static final Feature VIDEO_CHUNK_UPDATES = choice(Category.GRAPHICS, "video.chunkUpdates", "Chunk updates",
			"How aggressively chunk meshes update.", new String[] { "None", "Player affected", "Nearby" }, 1);
	public static final Feature VIDEO_ENTITY_SHADOWS = toggle(Category.GRAPHICS, "video.entityShadows", "Entity shadows",
			"Drop shadows under mobs and items.", true);
	public static final Feature VIDEO_MIPMAPS = number(Category.GRAPHICS, "video.mipmaps", "Mipmap levels",
			"Texture mipmap depth.", 0, 4, 1, 4);
	public static final Feature VIDEO_BIOME_BLEND = number(Category.GRAPHICS, "video.biomeBlend", "Biome blend",
			"Radius blended between biomes.", 0, 7, 1, 2);
	public static final Feature VIDEO_ANISOTROPY = number(Category.GRAPHICS, "video.anisotropy", "Anisotropic filtering",
			"Texture sharpness at grazing angles.", 0, 4, 1, 0);
	public static final Feature VIDEO_MENU_BLUR = number(Category.GRAPHICS, "video.menuBlur", "Menu background blur",
			"Blur strength behind menus.", 0, 10, 1, 5);
	public static final Feature VIDEO_PANORAMA_SPEED = number(Category.GRAPHICS, "video.panoramaSpeed", "Panorama speed",
			"Menu background rotation speed.", 0, 1, 0.05, 1.0);
	public static final Feature VIDEO_HIGH_CONTRAST = toggle(Category.GRAPHICS, "video.highContrast", "High contrast menus",
			"Stronger contrast for UI elements.", false);
	public static final Feature VIDEO_HIGH_CONTRAST_OUTLINE = toggle(Category.GRAPHICS, "video.highContrastOutline", "High contrast block outline",
			"Brighter selection outline.", false);
	public static final Feature VIDEO_LIGHTNING = toggle(Category.GRAPHICS, "video.hideLightning", "Hide lightning flash",
			"Do not flash the screen during thunderstorms.", false);
	public static final Feature VIDEO_SPLASH = toggle(Category.GRAPHICS, "video.hideSplash", "Hide splash text",
			"Remove the yellow splash text from the title screen.", false);

	public static final Feature INPUT_SENSITIVITY = number(Category.INPUT, "input.sensitivity", "Mouse sensitivity",
			"Look sensitivity.", 0, 1, 0.05, 0.5);
	public static final Feature INPUT_WHEEL = number(Category.INPUT, "input.mouseWheel", "Mouse wheel sensitivity",
			"How far the scroll wheel moves one slot.", 1, 10, 0.5, 1.0);
	public static final Feature INPUT_INVERT_X = toggle(Category.INPUT, "input.invertX", "Invert mouse X", "Flip horizontal look.", false);
	public static final Feature INPUT_INVERT_Y = toggle(Category.INPUT, "input.invertY", "Invert mouse Y", "Flip vertical look.", false);
	public static final Feature INPUT_RAW = toggle(Category.INPUT, "input.raw", "Raw mouse input", "Bypass OS pointer acceleration.", true);
	public static final Feature INPUT_DISCRETE_SCROLL = toggle(Category.INPUT, "input.discreteScroll", "Discrete scrolling", "One slot per wheel notch.", false);
	public static final Feature INPUT_CURSOR_CHANGES = toggle(Category.INPUT, "input.cursorChanges", "Cursor changes", "Change the OS cursor shape in menus.", true);
	public static final Feature INPUT_AUTO_JUMP = toggle(Category.INPUT, "input.autoJump", "Auto jump", "Jump automatically at one block steps.", false);
	public static final Feature INPUT_MINECART_ROTATE = toggle(Category.INPUT, "input.minecartRotate", "Rotate with minecart", "Camera follows minecart turns.", true);
	public static final Feature INPUT_MAIN_HAND = choice(Category.INPUT, "input.mainHand", "Main hand",
			"Which hand holds your items.", new String[] { "Left", "Right" }, 1);
	public static final Feature INPUT_SUGGESTIONS = toggle(Category.INPUT, "input.suggestions", "Command suggestions",
			"Show the command suggestion list.", true);

	public static final Feature CHAT_VISIBILITY = choice(Category.CHAT, "chat.visibility", "Visibility",
			"How much chat is shown.", new String[] { "Full", "Commands only", "Hidden" }, 0);
	public static final Feature CHAT_OPACITY = number(Category.CHAT, "chat.opacity", "Opacity",
			"Chat background opacity.", 0.1, 1.0, 0.05, 1.0);
	public static final Feature CHAT_SCALE = number(Category.CHAT, "chat.scale", "Scale",
			"Chat text size.", 0.5, 1.5, 0.05, 1.0);
	public static final Feature CHAT_LINE_SPACING = number(Category.CHAT, "chat.lineSpacing", "Line spacing",
			"Extra space between chat lines.", 0, 1, 0.05, 0);
	public static final Feature CHAT_WIDTH = number(Category.CHAT, "chat.width", "Width",
			"Chat box width as a fraction of the screen.", 0.5, 1.0, 0.05, 1.0);
	public static final Feature CHAT_HEIGHT_FOCUSED = number(Category.CHAT, "chat.heightFocused", "Height (open)",
			"Chat height while the box is open.", 0.2, 1.0, 0.05, 0.5);
	public static final Feature CHAT_HEIGHT_UNFOCUSED = number(Category.CHAT, "chat.heightUnfocused", "Height (closed)",
			"Chat height while the box is closed.", 0.2, 1.0, 0.05, 0.5);
	public static final Feature CHAT_COLORS = toggle(Category.CHAT, "chat.colors", "Colours", "Show message colours.", true);
	public static final Feature CHAT_LINKS = toggle(Category.CHAT, "chat.links", "Links", "Detect and highlight URLs.", true);
	public static final Feature CHAT_BACKGROUND_ONLY = toggle(Category.CHAT, "chat.backgroundOnly", "Backdrop on chat only",
			"Draw the dark backdrop behind chat but not other text.", false);
	public static final Feature CHAT_DELAY = number(Category.CHAT, "chat.delay", "Message delay",
			"Seconds of delay between messages.", 0, 2, 0.1, 0);
	public static final Feature CHAT_TEXT_BACKGROUND = number(Category.CHAT, "chat.textBackground", "Text backdrop opacity",
			"Opacity of text backgrounds.", 0, 1, 0.05, 0.5);

	public static final Feature MENU_DESCRIPTIONS = toggle(Category.MENU, "menu.descriptions", "Show descriptions",
			"Display a one line description under each feature name.", true);
	public static final Feature MENU_COMPACT = toggle(Category.MENU, "menu.compact", "Compact rows",
			"Fit more features on screen at once.", false);
	public static final Feature MENU_SHOW_FPS = toggle(Category.MENU, "menu.showFps", "FPS in menu title",
			"Show the current framerate in the menu header.", true);
	public static final Feature MENU_REMEMBER = toggle(Category.MENU, "menu.rememberCategory", "Remember category",
			"Reopen the menu on the category you used last.", true);
	public static final Feature MENU_SEARCH_FOCUS = toggle(Category.MENU, "menu.searchFocus", "Focus search",
			"Put the cursor in the search box when the menu opens.", false);
	public static final Feature MENU_RESET = Feature.action(Category.MENU, "menu.reset", "Reset all settings",
			"Return every VLOX setting to its default.");
	public static final Feature MENU_COPY_PATH = Feature.action(Category.MENU, "menu.copyPath", "Copy config path",
			"Copy the config file location to the clipboard.");

	private Features() {
	}

	private static Feature toggle(Category category, String id, String label, String description, boolean def) {
		Feature feature = Feature.toggle(category, id, label, description, def);
		ALL.add(feature);
		return feature;
	}

	private static Feature number(Category category, String id, String label, String description,
			double min, double max, double step, double def) {
		Feature feature = Feature.number(category, id, label, description, min, max, step, def);
		ALL.add(feature);
		return feature;
	}

	private static Feature choice(Category category, String id, String label, String description, String[] choices, int def) {
		Feature feature = Feature.choice(category, id, label, description, choices, def);
		ALL.add(feature);
		return feature;
	}

	/** Pushes every stored value into the game. */
	public static void applyAll() {
		for (Feature feature : ALL) {
			apply(feature);
		}
	}

	/** Pushes one feature's value into the game, if it drives a vanilla option. */
	public static void apply(Feature feature) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return;
		}
		Options options = minecraft.options;
		if (options == null) {
			return;
		}

		double raw = VloxConfig.get(feature);
		int i = (int) Math.round(raw);
		boolean b = raw >= 0.5;

		switch (feature.id) {
			case "video.renderDistance" -> options.renderDistance().set(i);
			case "video.simulationDistance" -> options.simulationDistance().set(i);
			case "video.entityDistance" -> options.entityDistanceScaling().set(raw);
			case "video.fpsLimit" -> options.framerateLimit().set(i);
			case "video.vsync" -> options.enableVsync().set(b);
			case "video.clouds" -> options.cloudStatus().set(CloudStatus.values()[i]);
			case "video.cloudRange" -> options.cloudRange().set(i);
			case "video.weatherRadius" -> options.weatherRadius().set(i);
			case "video.cutoutLeaves" -> options.cutoutLeaves().set(b);
			case "video.vignette" -> options.vignette().set(b);
			case "video.improvedTransparency" -> options.improvedTransparency().set(b);
			case "video.ambientOcclusion" -> options.ambientOcclusion().set(b);
			case "video.chunkFade" -> options.chunkSectionFadeInTime().set(raw);
			case "video.chunkUpdates" -> options.prioritizeChunkUpdates().set(PrioritizeChunkUpdates.values()[i]);
			case "video.entityShadows" -> options.entityShadows().set(b);
			case "video.mipmaps" -> options.mipmapLevels().set(i);
			case "video.biomeBlend" -> options.biomeBlendRadius().set(i);
			case "video.anisotropy" -> options.maxAnisotropyBit().set(i);
			case "video.menuBlur" -> options.menuBackgroundBlurriness().set(i);
			case "video.panoramaSpeed" -> options.panoramaSpeed().set(raw);
			case "video.highContrast" -> options.highContrast().set(b);
			case "video.highContrastOutline" -> options.highContrastBlockOutline().set(b);
			case "video.hideLightning" -> options.hideLightningFlash().set(b);
			case "video.hideSplash" -> options.hideSplashTexts().set(b);

			case "input.sensitivity" -> options.sensitivity().set(raw);
			case "input.mouseWheel" -> options.mouseWheelSensitivity().set(raw);
			case "input.invertX" -> options.invertMouseX().set(b);
			case "input.invertY" -> options.invertMouseY().set(b);
			case "input.raw" -> options.rawMouseInput().set(b);
			case "input.discreteScroll" -> options.discreteMouseScroll().set(b);
			case "input.cursorChanges" -> options.allowCursorChanges().set(b);
			case "input.autoJump" -> options.autoJump().set(b);
			case "input.minecartRotate" -> options.rotateWithMinecart().set(b);
			case "input.mainHand" -> options.mainHand().set(i == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
			case "input.suggestions" -> options.autoSuggestions().set(b);

			case "chat.visibility" -> options.chatVisibility().set(i == 0 ? ChatVisiblity.FULL : i == 1 ? ChatVisiblity.SYSTEM : ChatVisiblity.HIDDEN);
			case "chat.opacity" -> options.chatOpacity().set(raw);
			case "chat.scale" -> options.chatScale().set(raw);
			case "chat.lineSpacing" -> options.chatLineSpacing().set(raw);
			case "chat.width" -> options.chatWidth().set(raw);
			case "chat.heightFocused" -> options.chatHeightFocused().set(raw);
			case "chat.heightUnfocused" -> options.chatHeightUnfocused().set(raw);
			case "chat.colors" -> options.chatColors().set(b);
			case "chat.links" -> options.chatLinks().set(b);
			case "chat.backgroundOnly" -> options.backgroundForChatOnly().set(b);
			case "chat.delay" -> options.chatDelay().set(raw);
			case "chat.textBackground" -> options.textBackgroundOpacity().set(raw);

			default -> {
				// Settings that only affect VLOX's own drawing need no forwarding.
			}
		}
	}
}
