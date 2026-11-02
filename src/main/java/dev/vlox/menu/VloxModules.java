package dev.vlox.menu;

import java.util.List;

public final class VloxModules {
    public static final List<VloxModule> ALL = List.of(
        new VloxModule("Keystrokes", "HUD", "⌨"),
        new VloxModule("FPS Display", "HUD", "◴"),
        new VloxModule("Ping Display", "HUD", "⌁"),
        new VloxModule("Clock", "HUD", "◷"),
        new VloxModule("Coordinates", "HUD", "⌖"),
        new VloxModule("Armor HUD", "HUD", "◇"),
        new VloxModule("Fullbright", "Visual", "☼"),
        new VloxModule("Zoom", "Visual", "◎"),
        new VloxModule("Weather", "Visual", "☁"),
        new VloxModule("Dynamic FPS", "Performance", "ϟ"),
        new VloxModule("Chat Customizer", "Utility", "▤"),
        new VloxModule("GUI Customizer", "Customization", "⚙")
    );

    private VloxModules() {
    }
}
