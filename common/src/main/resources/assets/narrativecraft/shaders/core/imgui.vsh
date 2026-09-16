#version 450 core

layout(std140, binding = 0) uniform ProjMtx {
    mat4 Value;
};

in vec2 Position;
in vec2 UV;
in vec4 Color;

out vec2 Frag_UV;
out vec4 Frag_Color;

void main() {
    Frag_UV = UV;
    Frag_Color = Color;
    gl_Position = Value * vec4(Position.xy, 0, 1);
}
