#version 330 compatibility

// VloxLight TEST pack: no effects at all. Copies the finished frame to the
// screen. If the world still looks transparent with this selected, the problem
// is the pack's pass structure in Iris, not the colour maths.

uniform sampler2D colortex0;

in vec2 texcoord;

out vec4 fragColor;

void main() {
	fragColor = vec4(texture(colortex0, texcoord).rgb, 1.0);
}
