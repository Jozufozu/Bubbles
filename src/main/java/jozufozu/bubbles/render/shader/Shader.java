package jozufozu.bubbles.render.shader;

public enum Shader {
    BUBBLE("shader/bubble.vert", "shader/bubble.glsl");

    public final String vert;
    public final String frag;

    Shader(String vert, String frag) {
        this.vert = vert;
        this.frag = frag;
    }
}
