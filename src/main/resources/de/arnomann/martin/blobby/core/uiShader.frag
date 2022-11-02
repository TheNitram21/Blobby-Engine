#version 330 core

layout(location = 0) out vec4 outColor;
uniform sampler2D texture;

in vec2 texCoords;

void main() {
   vec4 color = texture2D(texture, texCoords);
   outColor = color;
}
