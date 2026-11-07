# VLOX

Two small, dependency-free projects live here.

1. **VLOX Racing** — a browser racing simulator whose optimiser learns the
   fastest line around a Baku-style street circuit, cutting its own lap time.
2. **VLOX AUTO** — a Windows key repeater for pressing the same keys over and
   over in a game.

---

## VLOX Racing — lap-time optimiser

Open `index.html` in any browser. No build step, no server, no packages.

A car laps the circuit. To begin with it follows the centreline, which is the
slow reference line. The optimiser then repeatedly reshapes the line toward its
minimum-curvature form: corners get straightened and apexes get cut, but the
line is never allowed outside the width of the track. Every candidate line is
driven by the same driver in the same car and timed over a full lap, so when
the lap time drops, the line is genuinely better — the car has not changed.

What you see:

- **green line** — the best line found so far, and the line the car is driving
- **blue line** — the candidate the optimiser is currently testing
- **grey dashed line** — the original centreline
- **yellow car** — the AI lap, live

Press **Space** to pause or resume, **R** to restart the search.

### Measured result

```
node tools/bench.js
```

```
track length      : 2399 m
centreline lap    : 68.000 s
iterations        : 194 (converged)
solve time        : 417 ms
best lap          : 58.322 s
best off track    : 0.00 s
improvement       : 9.678 s (14.23%)
PASS: optimiser beat the baseline and stayed on track
```

The benchmark fails with a non-zero exit code if the optimiser does not beat the
centreline or if its best line leaves the track, so the claim above is checked
rather than assumed.

### How it works

| File | Contents |
| --- | --- |
| `racing.js` | The whole simulation: circuit geometry, car physics, the driver, the optimiser. Runs in the browser and in Node. |
| `index.html` | Canvas rendering and the HUD. Loads `racing.js` with a plain `<script>` tag. |
| `tools/bench.js` | Headless benchmark that prints the measured improvement. |

The circuit is a closed centripetal Catmull-Rom spline resampled to 480 equally
spaced points. The car is a kinematic bicycle model with limits on acceleration,
braking, top speed and lateral grip. The AI driver steers with pure pursuit and
takes its target speed from the curvature profile of the line it is following,
including a backward braking pass so it never arrives at a corner too fast.

The optimiser is a relaxation sweep: each point on the line drifts toward the
midpoint of its neighbours, which lowers curvature, and is then clamped to the
width of the track. Each sweep produces a candidate line, which is driven and
timed. The best line ever seen is kept, and the search stops once it has gone 70
sweeps without improving.

---

## VLOX AUTO — key repeater

A single-file Windows tool that repeats key presses for you in a game.

Requirements: Windows and [AutoHotkey v2](https://www.autohotkey.com).

1. Install AutoHotkey v2, then double-click `apex-auto.ahk`.
2. Focus the game window.
3. Press **F7** to start, **F7** to stop, **F9** to quit.

The `CFG` block at the top of `apex-auto.ahk` sets the keys, how long each is
held, the gap between keys, and the gap between cycles. By default it only sends
input while the Roblox window is focused, so it cannot type into other apps.

If the game ignores the keys, run the script as administrator, use windowed
mode, or change `SendMode "Input"` to `SendMode "Event"`.

> Automating gameplay is against the Roblox Terms of Use and anti-cheat systems
> can flag repeated identical input. Using it can get an account banned. It is a
> plain input repeater with no anti-detection code.

### Why this is not a Roblox auto-driver

Driving a lap in the live game would need to watch the screen and steer in real
time, and an adaptive driver would need to play against other people. That is a
cheat in a multiplayer game, and it cannot be built or verified without the game
itself. VLOX Racing instead puts the same optimisation problem — racing line,
braking points, apex speed — in a simulator where the result is measurable.
