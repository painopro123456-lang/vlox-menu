# Nova Mod Menu

A standalone, client-side Fabric mod for Minecraft **26.2**. This is a prototype for the Nova in-game menu; it is kept separate from the Nova launcher until the menu is approved.

## Open the menu

- Press **Right Shift** in game.
- Press **V** to use the optional zoom feature when it is enabled.
- The menu uses a three-column module grid, a midnight navy translucent panel, restrained violet details and an icy-blue default accent.
- Categories: **All**, **HUD**, **Visual**, **Player**, and **Mods**.
- Click a module card to toggle it.
- The **Mods** page lists Fabric mods loaded in the current instance.
- **Advanced settings** opens the detailed settings screen for sliders, choices, presets and other options.

## Current prototype scope

The module grid exposes Nova's own client-side toggles and vanilla options. The installed-mod page is an inventory view; it does not hot-disable arbitrary Fabric mods. Fabric mods are loaded at startup, so safely enabling or disabling third-party mods requires changing the instance's mod set and restarting Minecraft.

Mod discovery and dependency-aware installation are a planned follow-up, not part of this first prototype. Nothing is installed or downloaded by this build.

## Compatibility

- Minecraft **26.2**
- Fabric Loader **0.20.1** or newer
- Fabric API **0.161.0+26.2**
- Java **25**
- Client-side only; no server mod is required

## Install

1. Install Fabric Loader for Minecraft 26.2.
2. Add Fabric API `0.161.0+26.2` to the instance's `mods` folder.
3. Put `nova-mod-menu-0.1.0.jar` in the same `mods` folder.
4. Launch Minecraft and press **Right Shift**.

## Build

Requires JDK 25 and Gradle 9.8 or newer:

```bash
gradle clean test build
```

The mod JAR is written to `build/libs/nova-mod-menu-0.1.0.jar`. GitHub Actions builds this branch and publishes the JAR as a workflow artifact and release asset.

## Notes

This prototype is built from the existing client-side feature registry, with a separate Nova-branded module grid. The Nova launcher is not modified. Test the JAR in a real Minecraft client before deciding whether to integrate it.
