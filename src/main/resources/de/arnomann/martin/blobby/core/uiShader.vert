#version 330 core

in vec3 position;
in vec2 textures;

uniform mat4 viewProjectionMatrix;

out vec2 texCoords;

void main() {
   texCoords = textures;
   gl_Position = viewProjectionMatrix * vec4(position, 1.0);
}
