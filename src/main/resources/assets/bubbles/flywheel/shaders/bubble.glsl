
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

#define PI 3.1415926538
#define TWO_PI 6.28318530718
#define WAVELENGTHS 6				 // number of wavelengths, not a free parameter

#use "flywheel:core/diffuse.glsl"
#use "flywheel:data/modelvertex.glsl"

struct Instance {
    vec2 light;
    vec4 color;
    mat4 transform;
    mat3 normalMat;
};

struct BubbleFrag {
    vec3 pos;
    vec3 normal;
    vec4 color;
    vec2 bubbleMap;
};


#if defined(VERTEX_SHADER)
BubbleFrag vertex(Vertex v, Instance i) {
    vec4 worldPos = i.transform * vec4(v.pos, 1.);

    vec3 norm = i.normalMat * v.normal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BubbleFrag b;
    b.pos = worldPos.xyz;
    b.normal = norm;
    b.bubbleMap = v.texCoords;
    //b.light = i.light;
    #if defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = i.color;
    #endif
    return b;
}
#elif defined(FRAGMENT_SHADER)

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
    float t = uTime;
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
    vec3 cube0 = vec3(
        dot(texCubeSampleWeights(wl0.x), vec3(0.5)),
        dot(texCubeSampleWeights(wl0.y), vec3(0.7)),
        dot(texCubeSampleWeights(wl0.z), vec3(0.3))
    );
    vec3 cube1 = vec3(
        dot(texCubeSampleWeights(wl1.x), vec3(0.5)),
        dot(texCubeSampleWeights(wl1.y), vec3(0.7)),
        dot(texCubeSampleWeights(wl1.z), vec3(0.3))
    );

    vec3 intensity0 = filmic_gamma_inverse(cube0) + refl0;
    vec3 intensity1 = filmic_gamma_inverse(cube1) + refl1;
    #endif
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

vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec2 mod289(vec2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec3 permute(vec3 x) { return mod289(((x*34.0)+1.0)*x); }

float snoise(vec3 v) {
    const vec4 C = vec4(
        0.211324865405187,  // (3.0-sqrt(3.0))/6.0
        0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
       -0.577350269189626, // -1.0 + 2.0 * C.x
        0.024390243902439   // 1.0 / 41.0
    );
    vec3 i  = floor(v + dot(v, C.yyy) );
    vec3 x0 = v -   i + dot(i, C.xxx);
    vec2 i1;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;
    i = mod289(i); // Avoid truncation effects in permutation
    vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
    + i.x + vec3(0.0, i1.x, 1.0 ));

    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
    //m = m*m ;
    m = m*m ;
    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;
    m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );
    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130. * dot(m, g);
}

#define OCTAVES 3
float turbulence (in vec3 st) {
    // Initial values
    float value = 0.0;
    float amplitude = .3;
    float frequency = 0.;
    //
    // Loop of octaves
    for (int i = 0; i < OCTAVES; i++) {
        value += amplitude * abs(snoise(st));
        st *= 2.;
        amplitude *= .5;
    }
    return value;
}

float filmThickness(vec3 pos) {
//
//    pos *= 0.2;
//
//    float DF = 0.0;
//
//    float t = uTime;
//
//    // Add a random position
//    float a = snoise(pos*vec2(cos(t * 0.150),sin(snoise(vec2(t * 0.092, 1.)))) * 0.1) * PI;
//    vec2 vel = vec2(cos(a), sin(a));
//
//    return turbulence(vec2(snoise(pos+vel)) + vec2(t * 0.1))*0.778;
    return 0.5 + noise(vec3(uTime / 200.0) + pos) * 0.5;
    //return 0.5 + turbulence(noise3(vec3(uTime / 200.0) + pos)) * 0.5;
}

void fragment(BubbleFrag bubble) {
    vec3 pos = bubble.pos;
    vec3 normal = bubble.normal;

    vec3 color = vec3(0.0);
    float incidence = 0.0;

    vec3 wavelengths0 = vec3(1.0, 0.8, 0.6);
    vec3 wavelengths1 = vec3(0.4, 0.2, 0.0);
    vec3 iors0 = IOR + iorCurve(wavelengths0) * DISPERSION;
    vec3 iors1 = IOR + iorCurve(wavelengths1) * DISPERSION;

    vec3 rds[WAVELENGTHS];

    vec3 norm = normalize(abs(sdf(normal))) * 0.8 + vec3(0.2);

    float dh = (0.666 / uWindowSize.y);

    const float rads = TWO_PI / float(AA_SAMPLES);
    for (int samp = 0; samp < AA_SAMPLES; samp++) {
        vec2 dxy = dh * vec2(cos(float(samp) * rads), sin(float(samp) * rads));
        vec3 ray = normalize(vec3(dxy, 1.5));// 1.5 is the lens length
        incidence += abs(dot(ray, norm));

        float filmThickness = filmThickness(vec3(pos.xy + dxy, pos.z));

        vec3 att0 = attenuation(filmThickness, wavelengths0, norm, ray);
        vec3 att1 = attenuation(filmThickness, wavelengths1, norm, ray);

        vec3 f0 = (1.0 - FRESNEL_RATIO) + FRESNEL_RATIO * fresnel(ray, norm, 1.0 / iors0);
        vec3 f1 = (1.0 - FRESNEL_RATIO) + FRESNEL_RATIO * fresnel(ray, norm, 1.0 / iors1);

        vec3 reflectedRay = reflect(ray, norm);

        vec3 col = vec3(0.5);// normalize(sdf(ray) * 0.6 + 0.4);

        vec3 cube0 = REFLECTANCE_GAMMA_SCALE * att0 * vec3(
        dot(texCubeSampleWeights(wavelengths0.x), col),
        dot(texCubeSampleWeights(wavelengths0.y), col),
        dot(texCubeSampleWeights(wavelengths0.z), col)
        );
        vec3 cube1 = REFLECTANCE_GAMMA_SCALE * att1 * vec3(
        dot(texCubeSampleWeights(wavelengths1.x), col),
        dot(texCubeSampleWeights(wavelengths1.y), col),
        dot(texCubeSampleWeights(wavelengths1.z), col)
        );

        vec3 refl0 = REFLECTANCE_SCALE * filmic_gamma_inverse(mix(vec3(0), cube0, f0));
        vec3 refl1 = REFLECTANCE_SCALE * filmic_gamma_inverse(mix(vec3(0), cube1, f1));

        rds[0] = refract(ray, norm, iors0.x);
        rds[1] = refract(ray, norm, iors0.y);
        rds[2] = refract(ray, norm, iors0.z);
        rds[3] = refract(ray, norm, iors1.x);
        rds[4] = refract(ray, norm, iors1.y);
        rds[5] = refract(ray, norm, iors1.z);

        color += resampleColor(rds, refl0, refl1, wavelengths0, wavelengths1);
    }

    color /= float(AA_SAMPLES);
    incidence /= float(AA_SAMPLES);

    vec2 st = vec2(bubble.bubbleMap);
    vec4 tex = texture2D(uBlockAtlas, st);
    float white = tex.r;
    float incidenceMult = tex.g;

    vec4 finalColor = vec4(contrast(color * 0.4) + vec3(0.3 + white), 0.3 - pow(incidence, 5.) * 0.29 * incidenceMult);

    FLWFinalizeColor(finalColor);
}
#endif // defined(FRAGMENT_SHADER)