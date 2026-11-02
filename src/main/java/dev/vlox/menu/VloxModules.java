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
        new VloxModule("Durability HUD", "HUD", "♢"),
        new VloxModule("Targeted Block Info", "HUD", "▦"),
        new VloxModule("Food Level HUD", "HUD", "◒"),
        new VloxModule("World Time", "HUD", "☼"),
        new VloxModule("Recipe Reminder", "HUD", "▤"),
        new VloxModule("CPS Display", "HUD", "⌁"),
        new VloxModule("Memory Usage", "HUD", "▥"),
        new VloxModule("Horse Stats", "HUD", "♞"),
        new VloxModule("Light Level", "HUD", "☼"),
        new VloxModule("Chunk Coordinates", "HUD", "▦"),
        new VloxModule("Durability Percentage", "HUD", "◇"),
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
        new VloxModule("Fog Control", "Visual", "〰"),
        new VloxModule("Sky Color", "Visual", "☁"),
        new VloxModule("Fire Overlay Style", "Visual", "♨"),
        new VloxModule("Block Outline Style", "Visual", "▧"),
        new VloxModule("Vignette Toggle", "Visual", "◉"),
        new VloxModule("Cloud Style", "Visual", "☁"),
        new VloxModule("Rain Particles", "Visual", "☂"),
        new VloxModule("Beacon Beam Style", "Visual", "♢"),
        // Performance
        new VloxModule("Dynamic FPS", "Performance", "ϟ"),
        new VloxModule("Particle Reducer", "Performance", "✧"),
        new VloxModule("Low Fire", "Performance", "♨"),
        new VloxModule("Entity Render Distance", "Performance", "◉"),
        new VloxModule("Animation Optimizer", "Performance", "〰"),
        new VloxModule("Background Throttle", "Performance", "ϟ"),
        new VloxModule("Reduced Screen Animations", "Performance", "▤"),
        new VloxModule("Cloud Rendering", "Performance", "☁"),
        new VloxModule("Weather Rendering", "Performance", "☂"),
        // Utility
        new VloxModule("Chat Customizer", "Utility", "▤"),
        new VloxModule("Screenshot Toast", "Utility", "▧"),
        new VloxModule("Durability Warning", "Utility", "♢"),
        new VloxModule("Low Health Alert", "Utility", "♡"),
        new VloxModule("Chat Timestamps", "Utility", "◷"),
        new VloxModule("Auto Reconnect", "Utility", "↻"),
        new VloxModule("Screenshot Organizer", "Utility", "▧"),
        new VloxModule("Item Pickup Log", "Utility", "▣"),
        new VloxModule("World Join Message", "Utility", "➤"),
        new VloxModule("Quick Screenshot", "Utility", "▧"),
        new VloxModule("Chat Copy Button", "Utility", "▣"),
        new VloxModule("Server Info", "Utility", "⌁"),
        new VloxModule("World Save Reminder", "Utility", "▤"),
        new VloxModule("Screenshot Metadata", "Utility", "▧"),
        new VloxModule("Resource Pack Reminder", "Utility", "▣"),
        // Customization
        new VloxModule("GUI Customizer", "Customization", "⚙"),
        new VloxModule("Crosshair Customizer", "Customization", "⊕"),
        new VloxModule("HUD Editor", "Customization", "▦"),
        new VloxModule("Color Presets", "Customization", "◈"),
        new VloxModule("Corner Rounding", "Customization", "▢"),
        new VloxModule("Font Scale", "Customization", "A"),
        new VloxModule("Interface Blur", "Customization", "◉"),
        new VloxModule("Card Spacing", "Customization", "▤"),
        new VloxModule("Module Sorting", "Customization", "↕")
    );

    public static boolean isEnabled(String name) {
        return ALL.stream().anyMatch(module -> module.name().equals(name) && module.enabled());
    }

    private VloxModules() {
    }
}
