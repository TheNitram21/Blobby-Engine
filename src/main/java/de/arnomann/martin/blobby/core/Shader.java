package de.arnomann.martin.blobby.core;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.File;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/** An OpenGL shader. */
public class Shader {

    private static final String glslVersion = "#version 330 core";
    /** The default vertex shader. */
    public static final String DEFAULT_VERTEX = glslVersion + "\n" +
            "layout(location = 0) in vec3 in_Position;\n" +
            "layout(location = 1) in vec2 in_TextureCoords;\n" +
            "uniform mat4 viewProjectionMatrix;\n" +
            "out vec2 textureCoords;\n" +
            "void main() {\n" +
            "    textureCoords = in_TextureCoords;\n" +
            "    gl_Position = viewProjectionMatrix * vec4(in_Position, 1.0);\n" +
            "}\n";
    /** The default fragment shader. */
    public static final String DEFAULT_FRAGMENT = glslVersion + "\n" +
            "#define MAX_LIGHTS 768\n" +
            "in vec2 textureCoords;\n" +
            "uniform sampler2D texture;\n" +
            "uniform mat4 viewMatrix;\n" +
            "uniform mat4 projectionMatrix;\n" +
            "uniform mat4 viewProjectionMatrix;\n" +
            "uniform vec3 lights[MAX_LIGHTS]; // XY is the light position, Z is the radius\n" +
            "uniform int lightCount;\n" +
            "uniform float ambientLight;\n" +
            "uniform float unitMultiplier;\n" +
            "uniform float cameraWidth;\n" +
            "uniform float cameraHeight;\n" +
            "uniform int screenWidth;\n" +
            "uniform int screenHeight;\n" +
            "uniform int flipped;\n" +
            "out vec4 outColor;\n" +
            "void main() {\n" +
            "    vec2 textureCoordinates = textureCoords;\n" +
            "    float smallestDistance = 1.0;\n" +
            "    for(int i = 0; i < MAX_LIGHTS; i++) {\n" +
            "        if(i >= lightCount) break;\n" +
            "        vec2 pixelDivider = vec2(screenWidth / 256.0, screenHeight / -144.0);\n" +
            "        vec4 lightPosition = viewMatrix * vec4(lights[i].x / 5.0, lights[i].y / -5.0," +
            "                0.0, 1.0);\n" +
            "        float distance = distance(lightPosition.xy * vec2(-unitMultiplier * pixelDivider.x, unitMultiplier *" +
            "                pixelDivider.y), vec2(0.0, screenHeight) - gl_FragCoord.xy) / unitMultiplier / lights[i].z;\n" +
            "        smallestDistance = min(smallestDistance, distance);\n" +
            "    }\n" +
            "    if(flipped == 1) {\n" +
            "        textureCoordinates.x *= -1;\n" +
            "    }\n" +
            "    vec4 color = texture2D(texture, textureCoordinates);\n" +
            "    float brightness = 1.0 - smallestDistance * ambientLight;\n" +
            "    outColor = vec4(brightness, brightness, brightness, 1.0) * color;\n" +
            "}\n";
    /** The UI vertex shader. */
    public static final String UI_VERTEX = glslVersion + "\n" +
            "layout(location = 0) in vec3 in_Position;\n" +
            "layout(location = 1) in vec2 in_TextureCoords;\n" +
            "uniform mat4 viewProjectionMatrix;\n" +
            "out vec2 textureCoords;\n" +
            "void main() {\n" +
            "    textureCoords = in_TextureCoords;\n" +
            "    gl_Position = viewProjectionMatrix * vec4(in_Position, 1.0);\n" +
            "}\n";
    /** The UI fragment shader. */
    public static final String UI_FRAGMENT = glslVersion + "\n" +
            "in vec2 textureCoords;\n" +
            "uniform sampler2D texture;\n" +
            "uniform int flipped;\n" +
            "out vec4 outColor;\n" +
            "void main() {\n" +
            "    vec2 textureCoordinates = textureCoords;\n" +
            "    if(flipped == 1) {\n" +
            "        textureCoordinates.x *= -1;\n" +
            "    }\n" +
            "    vec4 color = texture2D(texture, textureCoordinates);\n" +
            "    outColor = color;\n" +
            "}\n";

    /** The shader's id. */
    public final int id;

    /**
     * Creates a new shader program.
     * @param vertexShader the vertex shader source.
     * @param fragmentShader the fragment shader source.
     */
    public Shader(String vertexShader, String fragmentShader) {
        id = createShader(vertexShader, fragmentShader);
        if(id == -1) {
            BlobbyEngine.getLogger().error("An error occurred whilst trying to create the shader!");
            return;
        }
    }

    /**
     * Creates a new shader program.
     * @param vertexShaderPath the path to the vertex shader file.
     * @param fragmentShaderPath the path to the fragment shader file.
     */
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

    /**
     * Sets a 4x4 float matrix in the shader.
     * @param name the uniform variable name.
     * @param matrix the matrix to set in the shader.
     */
    public void setUniformMatrix4f(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(id, name);
        try(MemoryStack stack = MemoryStack.stackPush()) {
            if(location != -1)
                glUniformMatrix4fv(location, false, matrix.get(stack.mallocFloat(16)));
        }
    }

    /**
     * Sets a 3d float vector in the shader.
     * @param name the uniform variable name.
     * @param x the x value.
     * @param y the y value.
     * @param z the z value.
     */
    public void setUniform3f(String name, float x, float y, float z) {
        int location = glGetUniformLocation(id, name);
        if(location != -1)
            glUniform3f(location, x, y, z);
    }

    /**
     * Sets an integer in the shader.
     * @param name the uniform variable name.
     * @param value the integer to set in the shader.
     */
    public void setUniform1i(String name, int value) {
        int location = glGetUniformLocation(id, name);
        if(location != -1)
            glUniform1i(location, value);
    }

    /**
     * Sets a float in the shader.
     * @param name the uniform variable name.
     * @param value the float to set in the shader.
     */
    public void setUniform1f(String name, float value) {
        int location = glGetUniformLocation(id, name);
        if(location != -1)
            glUniform1f(location, value);
    }

    /**
     * Binds the shader for usage.
     */
    public void bind() {
        glUseProgram(id);
    }

    /**
     * Unbinds the shader.
     */
    public void unbind() {
        glUseProgram(0);
    }

}

