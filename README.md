# VLOX

A client-side settings menu for **Minecraft 26.2** on **Fabric**, with 89 settings
in one in-game screen. Press **Right Shift** to open it.

No mixins, no server-side code, nothing that changes combat or movement: every
setting either draws VLOX's own HUD or forwards to a vanilla option that already
exists in the Minecraft options screen.

## Features

| Category | Count | Examples |
| --- | --- | --- |
| **HUD** | 35 | FPS, coordinates, Nether coordinates, biome, dimension, world time, light level, memory, ping, health, armour, held-item durability, XP, fall distance, "looking at" block |
| **Graphics** | 24 | Render distance, simulation distance, entity distance, FPS limit, clouds, cloud range, weather radius, cutout leaves, vignette, transparency, ambient occlusion, chunk fade, mipmaps, biome blend, anisotropic filtering, menu blur, panorama speed |
| **Input** | 11 | Mouse sensitivity, wheel sensitivity, invert axes, raw input, discrete scrolling, cursor changes, auto jump, rotate with minecart, main hand |
| **Chat** | 12 | Visibility, opacity, scale, line spacing, width, height (open/closed), colours, links, message delay, text backdrop |
| **Menu** | 7 | Descriptions, compact rows, FPS in title, remember category, focus search, reset all, copy config path |

Open the menu, pick a category along the top, scroll to page through the list, and
search by name, id or description.

Settings come in two kinds:

- **47 wrap a vanilla option.** These rows render Minecraft's own control widget,
  so the range, the step size, the clamping and the saving are exactly the
  options screen's, and the value lives in `options.txt` where the game already
  keeps it. VLOX does not reinterpret them.
- **42 are VLOX's own.** HUD lines and menu behaviour, stored in `config/vlox.json`
  and written the moment you change them.

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
| `feature/Features.java` | The registry of all 89 settings, plus the map from a setting to its vanilla option |
| `config/VloxConfig.java` | JSON persistence, clamping and step quantisation |
| `gui/VloxScreen.java` | The menu: category tabs, search, scrolling rows |
| `hud/VloxHud.java` | The HUD lines and their placement |
| `VloxClient.java` | Entrypoint: keybind, HUD element registration, startup |

Values for VLOX's own settings live in one map keyed by feature id, so adding a
setting means adding one line to `Features` — no new class, no new config field.
Option-backed settings are resolved by a single switch in `Features.optionFor`,
which returns the live `OptionInstance`; the menu then asks that instance to
build its own button. Nothing is copied, clamped or rewritten by VLOX, and no
vanilla setting is touched until the player changes it.

## Testing

```bash
gradle test
```

Five registry tests check that there are at least 75 settings, that ids are
unique, that defaults sit inside their ranges, that choices are declared and that
every category has settings. They run in a few seconds and need no game launch.

**Not verified:** the mod has been compiled and unit tested, but it has never
been launched in a real Minecraft client — this build machine has no display and
no X server, so the client cannot start here. Nothing about the on-screen result
has been observed: not the menu layout, not the HUD placement, not a single
option changing in game.

What that means per kind of setting:

- The **47 option-backed rows** are low risk by construction: the widget and the
  value are vanilla's, and the only new code is which row to put them on.
- The **35 HUD lines** are the real unknown: they are VLOX's own drawing and its
  own reading of game state, and none of that has been seen running.
- The **7 menu settings** are simple toggles, but the menu itself is untested.

If something looks wrong in game, say what you see and it can be corrected.
