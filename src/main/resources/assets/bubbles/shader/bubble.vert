
#version 120

varying vec3 normal;
varying vec3 pos;
uniform int time; // Passed in, see ShaderHelper.java

void main() {
    normal = normalize(gl_NormalMatrix * gl_Normal.xyz);

    pos = gl_Vertex.xyz;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
}