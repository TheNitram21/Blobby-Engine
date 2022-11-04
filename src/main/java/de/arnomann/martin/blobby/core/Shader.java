package de.arnomann.martin.blobby.core;

import java.io.File;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    public static final String DEFAULT_VERTEX = "#version 330 core\n" +
            "in vec3 position;\n" +
            "in vec2 textures;\n" +
            "uniform mat4 viewProjectionMatrix;\n" +
            "out vec2 texCoords;\n" +
            "void main() {\n" +
            "   texCoords = textures;\n" +
            "   gl_Position = viewProjectionMatrix * vec4(position, 1.0);\n" +
            "}\n";
    public static final String DEFAULT_FRAGMENT = "#version 330 core\n" +
            "#define MAX_LIGHTS 768\n" +
            "layout(location = 0) out vec4 outColor;\n" +
            "uniform sampler2D texture;\n" +
            "uniform mat4 viewMatrix;\n" +
            "uniform mat4 projectionMatrix;\n" +
            "uniform mat4 viewProjectionMatrix;\n" +
            "uniform vec3 lights[MAX_LIGHTS]; // XY is the light position, Z is the radius\n" +
            "uniform int lightCount;\n" +
            "uniform float unitMultiplier;\n" +
            "uniform float cameraWidth;\n" +
            "uniform float cameraHeight;\n" +
            "uniform int screenWidth;\n" +
            "uniform int screenHeight;\n" +
            "in vec2 texCoords;\n" +
            "void main() {\n" +
            "    float smallestDistance = 1.0;\n" +
            "    for(int i = 0; i < MAX_LIGHTS; i++) {\n" +
            "        if(i >= lightCount) break;\n" +
            "        vec4 lightPosition = viewMatrix * vec4(lights[i].x / (16.0 / 3.0), lights[i].y / 3.0, 0.0, 1.0);\n" +
            "        float distance = distance(lightPosition.xy * vec2(-unitMultiplier * (16.0 / 3.0), unitMultiplier * 3.0),\n" +
            "                vec2(0.0, screenHeight) - gl_FragCoord.xy) / unitMultiplier / lights[i].z;\n" +
            "        smallestDistance = min(smallestDistance, distance);\n" +
            "    }\n" +
            "    vec4 color = texture2D(texture, texCoords);\n" +
            "    float brightness = 1.0 - smallestDistance * 0.6;\n" +
            "    outColor = vec4(brightness, brightness, brightness, 1.0) * color;\n" +
            "}\n";
    public static final String UI_VERTEX = "#version 330 core\n" +
            "in vec3 position;\n" +
            "in vec2 textures;\n" +
            "uniform mat4 viewProjectionMatrix;\n" +
            "out vec2 texCoords;\n" +
            "void main() {\n" +
            "   texCoords = textures;\n" +
            "   gl_Position = viewProjectionMatrix * vec4(position, 1.0);\n" +
            "}\n";
    public static final String UI_FRAGMENT = "#version 330 core\n" +
            "layout(location = 0) out vec4 outColor;\n" +
            "uniform sampler2D texture;\n" +
            "in vec2 texCoords;\n" +
            "void main() {\n" +
            "   vec4 color = texture2D(texture, texCoords);\n" +
            "   outColor = color;\n" +
            "}\n";

    public final int id;

    public Shader(String vertexShader, String fragmentShader) {
        id = createShader(vertexShader, fragmentShader);
        if(id == -1) {
            BlobbyEngine.getLogger().error("An error occurred whilst trying to create the shader!");
            return;
        }
    }

    public Shader(File vertexShaderPath, File fragmentShaderPath) {
        id = createShader(BlobbyEngine.readFile(vertexShaderPath), BlobbyEngine.readFile(fragmentShaderPath));
        if(id == -1) {
            BlobbyEngine.getLogger().error("An error occurred whilst trying to create the shader!");
            return;
        }
    }

    private int createShader(String vertShader, String fragShader) {
        int programId = glCreateProgram();

        int vertId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertId, vertShader);
        glCompileShader(vertId);
        if(glGetShaderi(vertId, GL_COMPILE_STATUS) == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(vertId);
            BlobbyEngine.getLogger().error("Could not create the vertex shader! Log:\n" + infoLog);
            glDeleteShader(vertId);
            return -1;
        }

        int fragId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragId, fragShader);
        glCompileShader(fragId);
        if(glGetShaderi(fragId, GL_COMPILE_STATUS) == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(fragId);
            BlobbyEngine.getLogger().error("Could not create the fragment shader! Log:\n" + infoLog);
            glDeleteShader(fragId);
            return -1;
        }

        glAttachShader(programId, vertId);
        glAttachShader(programId, fragId);
        glBindAttribLocation(programId, 0, "position");
        glBindAttribLocation(programId, 1, "textures");
        glLinkProgram(programId);
        glValidateProgram(programId);

        glDeleteShader(vertId);
        glDeleteShader(fragId);

        return programId;
    }

    public void bind() {
        glUseProgram(id);
    }

    public void unbind() {
        glUseProgram(0);
    }

}
