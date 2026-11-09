#version 330 compatibility

/*
	VloxLight - a post-process only shader pack.

	The whole pack is this one pass over the finished frame. It deliberately does
	not touch world geometry, lighting, shadows or water, so there is nothing it
	can break and nothing it can make slower than the vanilla frame plus a few
	texture fetches. Colour maths only: no gamma conversion, no buffer swaps.
*/

uniform sampler2D colortex0;

uniform float viewWidth;
uniform float viewHeight;

in vec2 texcoord;

// Options, all overridable from the shader menu (see shaders.properties).
const float SHARPEN = 0.55;
const float TONEMAP_STRENGTH = 0.30;
const bool BLOOM = true;
const float BLOOM_STRENGTH = 0.35;
const float VIBRANCE = 0.35;
const float VIGNETTE = 0.15;

// Strength of the bloom's bright pass: nothing dimmer than this glows.
const float BLOOM_THRESHOLD = 0.72;

vec3 fetch(vec2 uv) {
	return texture2D(colortex0, uv).rgb;
}

float luma(vec3 color) {
	return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

// ACES fit. Only ever blended in partway, so highlights roll off without the
// whole image going flat.
vec3 tonemap(vec3 color) {
	vec3 x = max(color, vec3(0.0));
	return clamp((x * (2.51 * x + 0.03)) / (x * (2.43 * x + 0.59) + 0.14), 0.0, 1.0);
}

void main() {
	vec2 texel = 1.0 / vec2(viewWidth, viewHeight);
	vec3 color = fetch(texcoord);

	// Unsharp mask against the four direct neighbours. Clamped, so a hard edge
	// brightens but cannot ring.
	if (SHARPEN > 0.0) {
		vec3 blur = fetch(texcoord + vec2(texel.x, 0.0))
				+ fetch(texcoord - vec2(texel.x, 0.0))
				+ fetch(texcoord + vec2(0.0, texel.y))
				+ fetch(texcoord - vec2(0.0, texel.y));
		blur *= 0.25;
		color = clamp(color + (color - blur) * SHARPEN, 0.0, 1.0);
	}

	// Cheap bloom: twelve taps on a ring around the pixel, only counting pixels
	// bright enough to be a light source. Torches, lava and the sun pick up a
	// soft halo for the cost of one loop.
	if (BLOOM) {
		vec3 glow = vec3(0.0);
		for (int i = 0; i < 12; i++) {
			float angle = float(i) * 0.5235988;
			vec2 offset = vec2(cos(angle), sin(angle)) * texel * 5.0;
			vec3 sampleColor = fetch(texcoord + offset);
			glow += sampleColor * smoothstep(BLOOM_THRESHOLD, 1.0, luma(sampleColor));
		}
		color += glow * (BLOOM_STRENGTH / 12.0);
	}

	// Vibrance rather than plain saturation: flat greys stay grey and already
	// vivid pixels are barely touched, so it does not tint the whole world.
	float brightness = luma(color);
	color = mix(vec3(brightness), color, 1.0 + VIBRANCE * (1.0 - smoothstep(0.5, 1.0, brightness)));

	color = mix(color, tonemap(color), TONEMAP_STRENGTH);

	// Very gentle corner falloff; 0 turns it off completely.
	vec2 centered = texcoord - 0.5;
	color *= clamp(1.0 - VIGNETTE * dot(centered, centered) * 1.6, 0.0, 1.0);

	gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
