# VloxLight

A shader pack for **Minecraft 26.2** that makes the frame look better without
making the GPU work harder to get there. One post-process pass over the finished
image — no shadow maps, no extra G-buffers, no lighting rewrite.

Requires **Sodium 0.9.2** and **Iris 1.11.4** for 26.2.

## Install

1. Drop `VloxLight.zip` into `.minecraft/shaderpacks/`.
2. Video Settings → Shader Packs → select **VloxLight**.

## What it does

| Effect | Default | What it changes |
| --- | --- | --- |
| **Sharpen** | 0.55 | Unsharp mask on the four neighbours. Clamped, so hard edges brighten without ringing. |
| **Bloom** | on, 0.35 | Twelve ring taps; only pixels above 0.72 luma glow. Torches, lava, glowstone, the sun. |
| **Vibrance** | 0.35 | Saturation weighted by distance from grey, so flat greys stay grey. |
| **Tonemap** | 0.30 | ACES fit blended partway in; gentle highlight rolloff. |
| **Vignette** | 0.15 | Slight corner falloff. 0 turns it off. |

All five are sliders; bloom is a toggle.

## If the world looks transparent

`VloxLight-Test.zip` is a diagnostic build: it is the **same pack structure with
no effects**, just copying the finished frame to the screen.

- **Test pack looks normal** → the pass structure is fine, the problem was the
  effect shader. Use the normal pack with the new modern-syntax shader.
- **Test pack is also transparent** → the `final`-only layout is not surviving
  your Iris build. Send me `logs/latest.log` (search for `VloxLight` and
  `shader`) and I will restructure rather than guess again.

## Syntax notes

The fragment shader uses `layout`-free explicit `out vec4 fragColor` and
`texture()`/`textureSize()` rather than `gl_FragColor`/`texture2D`. The vertex
shader is the canonical `ftransform()` fullscreen quad that every pack uses.
Both fragment shaders also compile clean when forced to `#version 330 core`,
which the old `gl_FragColor` version could not do.

## Verified / not verified

- `glslangValidator`: both fragment shaders clean in compatibility **and** core
  mode; a deliberately broken control file fails, so the check is real.
- Never rendered here — no GPU or display in this environment. Whether the
  sharpen reads too strong or the bloom radius suits your resolution is
  unconfirmed; that is what the sliders are for.
