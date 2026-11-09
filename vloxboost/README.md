# VloxBoost

A separate, client-side performance mod for **Minecraft 26.2** on **Fabric**. It is
independent of VLOX: its own jar, its own config, no Fabric API requirement, and it
works fine with VLOX installed or without it.

Four things, all of them real work removal rather than settings rearrangement:

| Feature | Default | What it removes |
| --- | --- | --- |
| **Distant entity culling** | on, 64 m | Entities past the limit are never extracted, so no model, pose, or buffer is built for them. |
| **Distant block entity culling** | on, 48 m | Chests, signs, banners, beds, spawners and other block entities past the limit are skipped. |
| **Particle cap** | on, 2000 | Once the cap is reached new particles are dropped. A single explosion or a campfire wall can otherwise keep tens of thousands alive. |
| **Adaptive render distance** | on, target 75 fps | Every 4 s the render distance moves one step down when frames are short, or back up when there is headroom. |

## How the adaptive distance behaves

- It only ever takes work away: the distance never rises above the value you had
  when the world loaded.
- It moves one chunk at a time and only when frames are clearly off target
  (below 85% or above 120%), so it does not oscillate.
- It writes the live option and **never saves it**, so `options.txt` keeps your own
  render distance and the next launch starts where you left it.
- Turning it off restores your original distance immediately.

## Install

1. Fabric Loader 0.20.1+ for Minecraft 26.2 and Java 25.
2. Drop `vloxboost-1.0.0.jar` in `mods/`.
3. That is all. No Fabric API, no other dependencies.

Start once to generate `config/vloxboost.json`, then edit it and restart:

```json
{
  "entityCull": true,
  "entityDistance": 64.0,
  "blockEntityCull": true,
  "blockEntityDistance": 48.0,
  "particleCap": true,
  "maxParticles": 2000,
  "dynamicRenderDistance": true,
  "targetFps": 75,
  "minRenderDistance": 4,
  "maxRenderDistance": 16
}
```

## Tuning

- **Seeing players vanish at range in PvP?** Raise `entityDistance` to 96–128, or
  set `entityCull` to false.
- **Chests popping in late while flying?** Raise `blockEntityDistance`.
- **Frames already fine?** Lower `targetFps` to 60 so it stops climbing to the cap,
  or set `dynamicRenderDistance` to false and leave the rest.
- **Weak machine?** `entityDistance` 48, `blockEntityDistance` 32,
  `maxParticles` 800, `targetFps` 60.

## How it hooks the game

No Fabric API, four mixins, every target read out of the 26.2 client jar:

| Mixin | Target | Reason |
| --- | --- | --- |
| `EntityCullMixin` | `EntityRenderDispatcher.shouldRender` | Returns false past the limit. |
| `BlockEntityCullMixin` | `BlockEntityRenderDispatcher.tryExtractRenderState` | Returns null past the limit; the caller already null-checks. |
| `ParticleCapMixin` | `ParticleEngine.add` | Counts live particles and cancels past the cap. |
| `MinecraftTickMixin` | `Minecraft.tick` | Runs the distance governor. |

`src/test/java/com/vloxboost/MixinTargetTest.java` reflects over the real classes and
fails if any of those methods, the shadowed `particles` field, or the option
accessor changes shape. Run it with `gradle test`.

## Build

```bash
export JAVA_HOME="$HOME/.local/jdk25"
export PATH="$JAVA_HOME/bin:$HOME/.local/gradle/gradle-9.9.0/bin:$PATH"
cd vloxboost
gradle build
```

The jar lands in `vloxboost/build/libs/`.

## Not verified

The mod has never been launched: this build machine has no display. The mixin
targets and their descriptors are verified against the jar and by test, and the
compiler accepts every call, but nothing here proves the game runs cleanly or that
the culling looks right in motion.
