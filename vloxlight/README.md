# VloxLight

A shader pack for **Minecraft 26.2** that makes the frame look better without
making the GPU work harder to get there. It is one post-process pass over the
finished image — no shadow maps, no extra G-buffers, no lighting rewrite.

## Install

1. Fabric Loader, **Sodium 0.9.2** and **Iris 1.11.4** for 26.2.
2. Drop `VloxLight.zip` into `.minecraft/shaderpacks/`.
3. Video Settings → Shader Packs → select **VloxLight**.

## What it does

| Effect | Default | What it changes |
| --- | --- | --- |
| **Sharpen** | 0.55 | Unsharp mask on the four neighbours. Textures, block edges and UI-ish details read crisper. Clamped, so hard edges brighten without ringing. |
| **Bloom** | on, 0.35 | Twelve taps in a ring; only pixels above 0.72 luma glow. Torches, lava, glowstone and the sun get a soft halo. |
| **Vibrance** | 0.35 | Saturation applied to the *distance from grey*, weighted so flat greys stay grey and already-vivid pixels barely move. Richer without the "everything is neon" look. |
| **Tonemap** | 0.30 | ACES fit blended in partway, so highlights roll off gently instead of clipping to flat white. |
| **Vignette** | 0.15 | Very slight corner falloff. Set to 0 for none. |

Every one of those is a slider in the shader menu, and bloom is a toggle. Set
sharpen and bloom to 0 and the pack costs about one texture fetch per pixel.

## Why it is cheap

It reads `colortex0` — the frame Minecraft and Sodium already produced — and
writes the screen. There is no `shadow`, `shadowcomp`, `prepare`, `deferred` or
`gbuffers` program, so nothing about world rendering, lighting or geometry
changes. The cost is roughly 15 texture fetches per pixel in one pass.

That also means it cannot produce the usual shader-pack failures: no missing
shadow map, no broken lightmap, no wrong fog, no glowstone that stops glowing.
The flip side is equally honest: **it adds no shadows, no water reflections, no
volumetrics and no ambient occlusion.** Those need the deferred pipeline and cost
far more than this pack does.

## Compatibility notes

- 26.2 uses a reversed-Z projection. This pack never reads `depthtex0`, so that
  change cannot invert anything.
- No gamma or colour-space conversion is applied, so it cannot wash the image out
  on a linear framebuffer.
- Works with VLOX and VloxBoost installed; it touches none of the same things.

## Verified / not verified

- Both shaders compile clean under `glslangValidator` (a deliberately broken
  control file fails, so the check is real).
- Never rendered: this machine has no GPU or display. The visual result — how
  strong the sharpen reads, whether the bloom radius suits your resolution — is
  unconfirmed. The sliders exist so you can tune it in-game rather than guess.

## Tuning

- Too crisp / aliasing? Sharpen 0.2–0.3.
- Halo around torches too big? Bloom 0.15, or toggle it off.
- Night too dark? Tonemap 0.1.
- Want the cheapest possible version? Sharpen 0, Bloom off — the rest is a
  handful of ALU per pixel.
