
#version 120

varying vec3 normal;
uniform int time; // Passed in, see ShaderHelper.java

void main() {
    normal = normalize(gl_NormalMatrix * gl_Normal.xyz);

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
}