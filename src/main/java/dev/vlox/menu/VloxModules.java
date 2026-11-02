package dev.vlox.menu;

import java.util.List;

public final class VloxModules {
    public static final List<VloxModule> ALL = List.of(
        // HUD
        new VloxModule("Keystrokes", "HUD", "⌨"),
        new VloxModule("FPS Display", "HUD", "◴"),
        new VloxModule("Ping Display", "HUD", "⌁"),
        new VloxModule("Clock", "HUD", "◷"),
        new VloxModule("Coordinates", "HUD", "⌖"),
        new VloxModule("Armor HUD", "HUD", "◇"),
        new VloxModule("Direction HUD", "HUD", "➤"),
        new VloxModule("Item Counter", "HUD", "▣"),
        new VloxModule("Session Stats", "HUD", "▥"),
        new VloxModule("Stopwatch", "HUD", "◷"),
        new VloxModule("Potion HUD", "HUD", "⚗"),
        new VloxModule("Effect Timers", "HUD", "◴"),
        new VloxModule("Compass HUD", "HUD", "✥"),
        new VloxModule("Speedometer", "HUD", "➟"),
        new VloxModule("Biome Display", "HUD", "♧"),
        new VloxModule("Jump Circle", "HUD", "◯"),
        // Visual
        new VloxModule("Fullbright", "Visual", "☼"),
        new VloxModule("Zoom", "Visual", "◎"),
        new VloxModule("Weather", "Visual", "☁"),
        new VloxModule("Smooth Camera", "Visual", "◉"),
        new VloxModule("Clear Water", "Visual", "≋"),
        new VloxModule("View Bobbing", "Visual", "〰"),
        new VloxModule("Entity Shadows", "Visual", "◌"),
        new VloxModule("Damage Tilt", "Visual", "↝"),
        new VloxModule("Hide Pumpkin Overlay", "Visual", "▱"),
        // Performance
        new VloxModule("Dynamic FPS", "Performance", "ϟ"),
        new VloxModule("Particle Reducer", "Performance", "✧"),
        new VloxModule("Low Fire", "Performance", "♨"),
        // Utility
        new VloxModule("Chat Customizer", "Utility", "▤"),
        new VloxModule("Screenshot Toast", "Utility", "▧"),
        new VloxModule("Durability Warning", "Utility", "♢"),
        new VloxModule("Low Health Alert", "Utility", "♡"),
        new VloxModule("Chat Timestamps", "Utility", "◷"),
        new VloxModule("Auto Reconnect", "Utility", "↻"),
        new VloxModule("Screenshot Organizer", "Utility", "▧"),
        // Customization
        new VloxModule("GUI Customizer", "Customization", "⚙"),
        new VloxModule("Crosshair Customizer", "Customization", "⊕"),
        new VloxModule("HUD Editor", "Customization", "▦"),
        new VloxModule("Color Presets", "Customization", "◈")
    );

    private VloxModules() {
    }
}
