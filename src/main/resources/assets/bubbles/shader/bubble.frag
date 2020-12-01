
#version 120

varying vec3 normal;
uniform sampler2D bgl_RenderedTexture;
uniform int time; // Passed in, see ShaderHelper.java
uniform float partialTicks; // Passed in, see ShaderHelper.java

// iq's 3D noise function
float hash( float n ){
    return fract(sin(n)*43758.5453);
}

float noise( in vec3 x ) {
    vec3 p = floor(x);
    vec3 f = fract(x);

    f = f*f*(3.0-2.0*f);
    float n = p.x + p.y*57.0 + 113.0*p.z;
    return mix(mix(mix( hash(n+  0.0), hash(n+  1.0),f.x),
    mix( hash(n+ 57.0), hash(n+ 58.0),f.x),f.y),
    mix(mix( hash(n+113.0), hash(n+114.0),f.x),
    mix( hash(n+170.0), hash(n+171.0),f.x),f.y),f.z);
}

vec3 noise3(vec3 x) {
    return vec3(
        noise(x+vec3(123.456,.567,.37)),
        noise(x+vec3(.11,47.43,19.17)),
        noise(x)
    );
}

// make it warble
vec3 sdf( vec3 p ) {
    float t = float(time) + partialTicks;
    t = t / 20.0;
    vec3 n = vec3(sin(t * 0.5), sin(t * 0.3), cos(t * 0.2));
    vec3 q = 0.5 * (noise3(p + n) - 0.5);

    return q + p;
}

// placeholder texture
float rand(vec2 co, float seed) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233)) + seed) * 43758.5453);
}

void main() {
    vec2 st = vec2(gl_TexCoord[0]);
    float r = rand(vec2(int(st.x * 16), int(st.y * 16)), 65724.356) * 0.1 + 0.45;
    float g = rand(vec2(int(st.x * 16), int(st.y * 16)), 2305.206) * 0.1 + 0.45;
    float b = rand(vec2(int(st.x * 16), int(st.y * 16)), 76325.4352) * 0.1 + 0.45;

    float incidence = dot(vec3(0.0, 0.0, 1.0), normalize(sdf(normal)));

    gl_FragColor = vec4(r, g, b, 0.8 - pow(abs(incidence) * 0.8, 0.8));
}