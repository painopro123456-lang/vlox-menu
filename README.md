# VLOX MENU

A client-side Minecraft **26.2 Fabric** quality-of-life mod.

## Current foundation
- Right Shift opens the in-game VLOX menu.
- Translucent dark glass-style panel.
- Exactly three module cards per row.
- Each card shows an icon, module name, and category.
- Clicking a card changes its enabled state.
- The menu does not pause single-player gameplay.

## Build
Requires JDK 25 and Gradle 9.6.1.

```bash
gradle build
```

The mod JAR is written to `build/libs/vlox-menu-0.1.0.jar`.

## Status
This is the initial UI and module-system foundation. The listed modules currently provide menu toggles; their individual gameplay/HUD behaviours will be implemented in subsequent commits.
