#version 330 core

#define MAX_LIGHTS 768

layout(location = 0) out vec4 outColor;

uniform sampler2D texture;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewProjectionMatrix;

uniform vec3 lights[MAX_LIGHTS]; // XY is the light position, Z is the radius
uniform int lightCount;
uniform float unitMultiplier;

uniform float cameraWidth;
uniform float cameraHeight;

uniform int screenWidth;
uniform int screenHeight;

in vec2 texCoords;

void main() {
    float smallestDistance = 1.0;

    for(int i = 0; i < MAX_LIGHTS; i++) {
        if(i >= lightCount)
            break;

        vec4 lightPosition = viewMatrix * vec4(lights[i].x / (16.0 / 3.0), lights[i].y / 3.0, 0.0, 1.0);
        float distance = distance(lightPosition.xy * vec2(-unitMultiplier * (16.0 / 3.0), unitMultiplier * 3.0),
                vec2(0.0, screenHeight) - gl_FragCoord.xy) / unitMultiplier / lights[i].z;
        smallestDistance = min(smallestDistance, distance);
    }

    vec4 color = texture2D(texture, texCoords);

    float brightness = 1.0 - smallestDistance * 0.6;
    outColor = vec4(brightness, brightness, brightness, 1.0) * color;
}
