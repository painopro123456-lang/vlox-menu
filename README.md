# VLOX

A client-side settings menu for **Minecraft 26.2** on **Fabric**, with 92 settings
in one in-game screen. Press **Right Shift** to open it.

No mixins, no server-side code, nothing that changes combat or movement: every
setting either draws VLOX's own HUD or forwards to a vanilla option that already
exists in the Minecraft options screen.

## Features

| Category | Count | Examples |
| --- | --- | --- |
| **HUD** | 38 | FPS, coordinates, Nether coordinates, biome, dimension, world time, light level, memory, ping, health, **armour as the worn items with durability bars**, held item with its icon, **keystrokes**, **CPS**, **PvP item counts** (totems, pearls, arrows, gapples), **potion effect timers**, XP, fall distance, "looking at" block |
| **Graphics** | 24 | Render distance, simulation distance, entity distance, FPS limit, clouds, cloud range, weather radius, cutout leaves, vignette, transparency, ambient occlusion, chunk fade, mipmaps, biome blend, anisotropic filtering, menu blur, panorama speed |
| **Input** | 11 | Mouse sensitivity, wheel sensitivity, invert axes, raw input, discrete scrolling, cursor changes, auto jump, rotate with minecart, main hand |
| **Chat** | 12 | Visibility, opacity, scale, line spacing, width, height (open/closed), colours, links, message delay, text backdrop |
| **Menu** | 7 | Descriptions, compact rows, FPS in title, remember category, focus search, reset all, copy config path |

The menu is drawn by hand rather than assembled from vanilla widgets:

- a dark side rail with the VLOX wordmark, one item per category, each showing how
  many settings it holds, with an accent bar on the selected one
- rounded row cards that fade in under the cursor, with the accent bar sliding in
- animated pill switches whose knob glides between off and on
- value chips for cycles, and `- value +` steppers for VLOX's own numbers
- a search field with a blinking caret
- a footer that shows the full description of whatever row you hover
- a thin scrollbar, and a soft fade at the top and bottom of the list

The HUD uses the same language: a rounded translucent card with the same accent
bar down its left edge. Its rows can hold text, item icons or the keystroke
block, so armour, the held item and your own clicks are shown rather than
described.

Pick a category on the left, scroll to page through the list, and search by name,
id or description.

Settings come in two kinds:

- **47 wrap a vanilla option.** These rows render Minecraft's own control widget,
  so the range, the step size, the clamping and the saving are exactly the
  options screen's, and the value lives in `options.txt` where the game already
  keeps it. VLOX does not reinterpret them.
- **45 are VLOX's own.** HUD lines and menu behaviour, stored in `config/vlox.json`
  and written within half a second of a change rather than on every pixel of a
  slider drag.

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
| `feature/Features.java` | The registry of all 92 settings, plus the map from a setting to its vanilla option |
| `config/VloxConfig.java` | JSON persistence, clamping and step quantisation |
| `gui/VloxScreen.java` | The menu: rail, hand-drawn rows, controls, search, scrolling |
| `gui/Ui.java` | Drawing helpers: rounded panels with shadow, scaled text, truncation |
| `hud/VloxHud.java` | The HUD rows: text, item icons, keystrokes, and the panel layout |
| `hud/CombatInput.java` | Samples held keys and counts clicks per second |
| `VloxClient.java` | Entrypoint: keybind, HUD element registration, startup |

Values for VLOX's own settings live in one map keyed by feature id, so adding a
setting means adding one line to `Features` — no new class, no new config field.
Option-backed settings are resolved by a single switch in `Features.optionFor`,
which returns the live `OptionInstance`; the menu then asks that instance to
build its own button. Nothing is copied, clamped or rewritten by VLOX, and no
vanilla setting is touched until the player changes it.

## Performance

The HUD draws every frame but rebuilds rarely:

- the row list, the measured width and the panel size are cached and refreshed at
  most every 50 ms, or immediately after a setting changes — the config keeps a
  revision counter that the HUD compares against, so a toggle shows up on the next
  frame instead of waiting for the tick
- text is assembled in one reused `StringBuilder`, and decimals are appended with
  integer maths instead of `String.format`, which costs a locale lookup and an
  allocation on every call
- a HUD with every line switched off returns before measuring or allocating
  anything
- settings are written to disk at most every 400 ms while a slider is dragged,
  instead of one full JSON rewrite per pixel, and are flushed on the next tick
  and on reset

This removes per-frame work; it is not a claim about a measured framerate, since
no framerate has been observed on this machine.

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
- The **38 HUD lines** are the real unknown: they are VLOX's own drawing and its
  own reading of game state, and none of that has been seen running. The item
  icons in particular rely on `GuiGraphicsExtractor.item` honouring the pose VLOX
  pushes for scaling, which cannot be checked without a client.
- The **7 menu settings** are simple toggles, but the menu itself is untested.

If something looks wrong in game, say what you see and it can be corrected.
