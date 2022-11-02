package de.arnomann.martin.blobby.core;

import org.lwjgl.BufferUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

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
