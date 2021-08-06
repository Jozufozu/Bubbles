#flwbuiltins
#flwinclude <"flywheel:core/quaternion.glsl">

#[InstanceData]
struct Bubble {
    vec3 pos;
    vec4 rotation;

};

#[VertexData]
struct Vertex {
    vec3 pos;
    vec3 normal;
    vec2 bubbleMap;
};

#[Fragment]
struct BubbleFrag {
    vec3 pos;
    vec3 normal;
    vec4 color;
    vec2 bubbleMap;
};

BubbleFrag FLWMain(Vertex v, Bubble instance) {
    vec3 rotated = rotateVertexByQuat(v.pos - .5, instance.rotation) + instance.pos + .5;

    vec4 worldPos = vec4(rotated, 1.);

    vec3 norm = rotateVertexByQuat(v.normal, instance.rotation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BubbleFrag b;
    b.pos = uCameraPos + worldPos.xyz;
    b.normal = norm;
    b.bubbleMap = v.bubbleMap;

    #if defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = vec4(1.);
    #endif

    return b;
}
