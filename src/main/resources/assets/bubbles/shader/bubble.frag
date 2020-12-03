
#version 120

// performance and raymarching options
#define INTERSECTION_PRECISION 0.01  // raymarcher intersection precision
#define ITERATIONS 20				 // max number of iterations
#define AA_SAMPLES 1				 // anti aliasing samples
#define BOUND 6.0					 // cube bounds check
#define DIST_SCALE 0.9   			 // scaling factor for raymarching position update

// optical properties
#define DISPERSION 0.05				 // dispersion amount
#define IOR 0.9     				 // base IOR value specified as a ratio
#define THICKNESS_SCALE 32.0		 // film thickness scaling factor
#define THICKNESS_CUBEMAP_SCALE 0.1  // film thickness cubemap scaling factor
#define REFLECTANCE_SCALE 3.0        // reflectance scaling factor
#define REFLECTANCE_GAMMA_SCALE 2.0  // reflectance gamma scaling factor
#define FRESNEL_RATIO 0.7			 // fresnel weight for reflectance
#define SIGMOID_CONTRAST 8.0         // contrast enhancement

#define TWO_PI 6.28318530718
#define WAVELENGTHS 6				 // number of wavelengths, not a free parameter


varying vec3 pos;
varying vec3 normal;
uniform sampler2D bgl_RenderedTexture;
uniform int time; // Passed in, see ShaderHelper.java
uniform float partialTicks; // Passed in, see ShaderHelper.java
uniform vec2 windowSize;
uniform vec3 cameraPos;

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

vec3 fresnel( vec3 rd, vec3 normal, vec3 n2 ) {
    vec3 r0 = pow((1.0-n2)/(1.0+n2), vec3(2));
    return r0 + (1. - r0)*pow(clamp(1. + dot(rd, normal), 0.0, 1.0), 5.);
}

    #define GAMMA_CURVE 50.0
    #define GAMMA_SCALE 4.5
vec3 filmic_gamma(vec3 x) {
    return log(GAMMA_CURVE * x + 1.0) / GAMMA_SCALE;
}

vec3 filmic_gamma_inverse(vec3 y) {
    return (1.0 / GAMMA_CURVE) * (exp(GAMMA_SCALE * y) - 1.0);
}

    // sample weights for the cubemap given a wavelength i
    // room for improvement in this function
    #define GREEN_WEIGHT 2.8
vec3 texCubeSampleWeights(float i) {
    vec3 w = vec3((1.0 - i) * (1.0 - i), GREEN_WEIGHT * i * (1.0 - i), i * i);
    return w / dot(w, vec3(1.0));
}

vec3 sampleCubeMap(vec3 i, vec3 rd) {
    vec3 t = vec3(cos((float(time) + partialTicks) * 0.1231) * 0.5 + 0.5);
    vec3 col = vec3(0.5);//normalize(sdf(rd + t) * 0.6 + 0.4);
    return vec3(
    dot(texCubeSampleWeights(i.x), col),
    dot(texCubeSampleWeights(i.y), col),
    dot(texCubeSampleWeights(i.z), col)
    );
}

vec3 sampleCubeMap(vec3 i, vec3 rd0, vec3 rd1, vec3 rd2) {
    return vec3(
    dot(texCubeSampleWeights(i.x), vec3(0.5)),
    dot(texCubeSampleWeights(i.y), vec3(0.7)),
    dot(texCubeSampleWeights(i.z), vec3(0.3))
    );
}


vec3 sampleWeights(float i) {
    return vec3((1.0 - i) * (1.0 - i), GREEN_WEIGHT * i * (1.0 - i), i * i);
}

vec3 resample(vec3 wl0, vec3 wl1, vec3 i0, vec3 i1) {
    vec3 w0 = sampleWeights(wl0.x);
    vec3 w1 = sampleWeights(wl0.y);
    vec3 w2 = sampleWeights(wl0.z);
    vec3 w3 = sampleWeights(wl1.x);
    vec3 w4 = sampleWeights(wl1.y);
    vec3 w5 = sampleWeights(wl1.z);

    return i0.x * w0 + i0.y * w1 + i0.z * w2
    + i1.x * w3 + i1.y * w4 + i1.z * w5;
}

// downsample to RGB
vec3 resampleColor(vec3[WAVELENGTHS] rds, vec3 refl0, vec3 refl1, vec3 wl0, vec3 wl1) {

    #ifdef REFLECTANCE_ONLY
    vec3 intensity0 = refl0;
    vec3 intensity1 = refl1;
    #else
    vec3 cube0 = sampleCubeMap(wl0, rds[0], rds[1], rds[2]);
    vec3 cube1 = sampleCubeMap(wl1, rds[3], rds[4], rds[5]);

    vec3 intensity0 = filmic_gamma_inverse(cube0) + refl0;
    vec3 intensity1 = filmic_gamma_inverse(cube1) + refl1;
    #endif
    vec3 col = resample(wl0, wl1, intensity0, intensity1);

    return 1.4 * filmic_gamma(col / float(WAVELENGTHS));
}

vec3 resampleColorSimple(vec3 rd, vec3 wl0, vec3 wl1) {
    vec3 cube0 = sampleCubeMap(wl0, rd);
    vec3 cube1 = sampleCubeMap(wl1, rd);

    vec3 intensity0 = filmic_gamma_inverse(cube0);
    vec3 intensity1 = filmic_gamma_inverse(cube1);
    vec3 col = resample(wl0, wl1, intensity0, intensity1);

    return 1.4 * filmic_gamma(col / float(WAVELENGTHS));
}

// compute the wavelength/IOR curve values.
vec3 iorCurve(vec3 x) {
    return x;
}

vec3 attenuation(float filmThickness, vec3 wavelengths, vec3 normal, vec3 rd) {
    return 0.5 + 0.5 * cos(((THICKNESS_SCALE * filmThickness)/(wavelengths + 1.0)) * dot(normal, rd));
}

vec3 contrast(vec3 x) {
    return 1.0 / (1.0 + exp(-SIGMOID_CONTRAST * (x - 0.5)));
}

void main() {
    vec2 p = (-windowSize.xy + 2.0*gl_FragCoord.xy)/windowSize.y;
    vec2 st = vec2(gl_TexCoord[0]);

    vec3 col = vec3(0.0);
    float incidence = 0.0;

    vec3 wavelengths0 = vec3(1.0, 0.8, 0.6);
    vec3 wavelengths1 = vec3(0.4, 0.2, 0.0);
    vec3 iors0 = IOR + iorCurve(wavelengths0) * DISPERSION;
    vec3 iors1 = IOR + iorCurve(wavelengths1) * DISPERSION;

    vec3 rds[WAVELENGTHS];

    vec3 norm = normalize(sdf(abs(normal))) * 0.8 + 0.2;

    float dh = (0.666 / windowSize.y);
    const float rads = TWO_PI / float(AA_SAMPLES);
    for (int samp = 0; samp < AA_SAMPLES; samp++) {
        vec2 dxy = dh * vec2(cos(float(samp) * rads), sin(float(samp) * rads));
        vec3 ray = normalize(vec3(dxy, 1.5));// 1.5 is the lens length
        incidence += abs(dot(ray, norm));

        float filmThickness = 0.5 + noise(vec3((float(time) + partialTicks) / 40.0) + cameraPos - pos) * 0.5;

        vec3 att0 = attenuation(filmThickness, wavelengths0, norm, ray);
        vec3 att1 = attenuation(filmThickness, wavelengths1, norm, ray);

        vec3 f0 = (1.0 - FRESNEL_RATIO) + FRESNEL_RATIO * fresnel(ray, norm, 1.0 / iors0);
        vec3 f1 = (1.0 - FRESNEL_RATIO) + FRESNEL_RATIO * fresnel(ray, norm, 1.0 / iors1);

        vec3 reflectedRay = reflect(ray, norm);

        vec3 cube0 = REFLECTANCE_GAMMA_SCALE * att0 * sampleCubeMap(wavelengths0, reflectedRay);
        vec3 cube1 = REFLECTANCE_GAMMA_SCALE * att1 * sampleCubeMap(wavelengths1, reflectedRay);

        vec3 refl0 = REFLECTANCE_SCALE * filmic_gamma_inverse(mix(vec3(0), cube0, f0));
        vec3 refl1 = REFLECTANCE_SCALE * filmic_gamma_inverse(mix(vec3(0), cube1, f1));

        rds[0] = refract(ray, norm, iors0.x);
        rds[1] = refract(ray, norm, iors0.y);
        rds[2] = refract(ray, norm, iors0.z);
        rds[3] = refract(ray, norm, iors1.x);
        rds[4] = refract(ray, norm, iors1.y);
        rds[5] = refract(ray, norm, iors1.z);

        col += resampleColor(rds, refl0, refl1, wavelengths0, wavelengths1);
    }

    col /= float(AA_SAMPLES);
    incidence /= float(AA_SAMPLES);

    vec2 edge1 = step(vec2(0.05), st);
    vec2 edge2 = step(vec2(0.05), 1.0 - st);

    float edge = 1.0 - edge1.x * edge1.y * edge2.x * edge2.y;

    gl_FragColor = vec4(contrast(col * 0.4) + vec3(0.3 + edge), 0.3 - pow(incidence, 5.) * 0.29);
}