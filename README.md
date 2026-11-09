# VLOX

A client-side settings menu for **Minecraft 26.2** on **Fabric**, with 89 settings
in one in-game screen. Press **Right Shift** to open it.

No mixins, no server-side code, nothing that changes combat or movement: every
setting either draws VLOX's own HUD or forwards to a vanilla option that already
exists in the Minecraft options screen.

## Features

| Category | Count | Examples |
| --- | --- | --- |
| **HUD** | 36 | FPS, coordinates, Nether coordinates, biome, dimension, world time, light level, memory, ping, health, armour, held-item durability, XP, fall distance, "looking at" block |
| **Graphics** | 25 | Render distance, simulation distance, entity distance, FPS limit, clouds, cloud range, weather radius, cutout leaves, vignette, transparency, ambient occlusion, chunk fade, mipmaps, biome blend, anisotropic filtering, menu blur, panorama speed |
| **Input** | 11 | Mouse sensitivity, wheel sensitivity, invert axes, raw input, discrete scrolling, cursor changes, auto jump, rotate with minecart, main hand |
| **Chat** | 12 | Visibility, opacity, scale, line spacing, width, height (open/closed), colours, links, message delay, text backdrop |
| **Menu** | 7 | Descriptions, compact rows, FPS in title, remember category, focus search, reset all, copy config path |

Open the menu, pick a category along the top, scroll to page through the list, and
search by name, id or description. Every change is written to
`config/vlox.json` immediately, so settings survive a restart.

## Install

1. Install **Fabric Loader 0.20.1** or newer for Minecraft 26.2.
2. Put **Fabric API 0.161.0+26.2** in your `mods` folder.
3. Put `vlox-1.0.0.jar` in your `mods` folder.
4. Launch the game with Java 25 and press **Right Shift**.

## Build from source

Requires **JDK 25**:

```bash
./gradlew build
```

The jar lands in `build/libs/vlox-1.0.0.jar`. If you don't have a wrapper, any
Gradle 9.x works:

```bash
gradle build
```

## How it is put together

| File | Role |
| --- | --- |
| `feature/Feature.java` | One setting: id, label, description, type, range, default, choices |
| `feature/Features.java` | The registry of all 89 settings, plus forwarding to vanilla options |
| `config/VloxConfig.java` | JSON persistence, clamping and step quantisation |
| `gui/VloxScreen.java` | The menu: category tabs, search, scrolling rows |
| `hud/VloxHud.java` | The HUD lines and their placement |
| `VloxClient.java` | Entrypoint: keybind, HUD element registration, startup |

Values live in one map keyed by feature id, so adding a setting means adding one
line to `Features` — no new class, no new config field. Features that drive a
vanilla option are forwarded through a single switch in `Features.apply`, which
runs on change and once on the first client tick.

## Testing

```bash
gradle test
```

Five registry tests check that there are at least 75 settings, that ids are
unique, that defaults sit inside their ranges, that choices are declared and that
every category has settings. They run in a few seconds and need no game launch.

**Not verified:** the mod has been compiled and unit tested, but it has never
been launched in a real Minecraft client — this build machine has no GPU or
display. The menu layout, HUD placement and every forwarded option are untested
at runtime. If something looks wrong in game, say what you see and it can be
corrected.
