# VLOX AUTO

A small Windows tool that repeats key presses for you, made for F1 Apex Racing
(Baku City Circuit) in Roblox — or any other game, since the keys and timings
are fully configurable.

## What it does

Press **F7** and the script repeatedly sends the keys you configured, at the
timing you configured, until you press **F7** again. **F9** quits it.

It is a plain input repeater: it presses keys you could press yourself. It does
not read the game's memory, inject into Roblox, or try to hide itself.

## Requirements

- Windows (AutoHotkey is Windows-only — Mac and mobile are not supported)
- [AutoHotkey v2](https://www.autohotkey.com) installed

## Setup

1. Install AutoHotkey v2.
2. Double-click `apex-auto.ahk` — a green **H** appears in the system tray.
3. Launch Roblox and join the Baku track.
4. Click the game window so it has focus.
5. Press **F7** to start. Press **F7** again to stop.

By default the script does nothing unless the Roblox window is focused, so it
cannot type into Discord or your browser by accident.

## Configuration

Open `apex-auto.ahk` in Notepad and edit the `CFG` block at the top:

| Setting | Meaning |
| --- | --- |
| `Keys` | Keys sent every cycle, in order. `["w"]` repeats W; `["w", "a"]` sends W then A. |
| `HoldMs` | How long each key is held down. `60` is a normal tap; `500` is a firm hold. |
| `KeyGapMs` | Pause between keys inside one cycle. |
| `CycleGapMs` | Pause after a full cycle before starting the next one. |
| `StartDelayMs` | Countdown before the first press, so you can get ready. |
| `OnlyWhenRoblox` | `true` = only send keys while Roblox is focused. |
| `RobloxExe` | The Roblox process name to look for. |

Change `TOGGLE_KEY` / `QUIT_KEY` if F7 or F9 does something in-game.

After editing, save and reload from the tray icon (right-click → *Reload*) or
just double-click the file again.

Useful starting points:

- **Constant throttle:** `Keys: ["w"]`, `HoldMs: 200`, `CycleGapMs: 0`
- **Periodic action (e.g. one press every second):** `Keys: ["e"]`, `HoldMs: 60`, `CycleGapMs: 940`
- **Simple weave:** `Keys: ["w", "d", "w", "a"]`, `HoldMs: 200`, `CycleGapMs: 200`

## If the keys do not register in-game

Synthetic input does not always reach a game window. Try, in this order:

1. Run Roblox in **windowed** or borderless mode rather than exclusive fullscreen.
2. Right-click `apex-auto.ahk` → *Run as administrator*.
3. In the script, change `SendMode "Input"` to `SendMode "Event"`, or to
   `SendMode "Play"`, and reload.

## Warning

Automation is against the Roblox Terms of Use, and anti-cheat systems can
detect repeated identical input patterns. Using this can get your account
warned or banned. That risk is yours; the script exists for offline testing,
accessibility, and learning, and there is deliberately no anti-detection
code in it.

## Status

Written and syntax-checked by an AI assistant without a Windows machine
available, so it has **not** been run against the actual game. If something
misbehaves, describe what happens and it can be adjusted.
