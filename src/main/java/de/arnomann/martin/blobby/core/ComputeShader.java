package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.core.texture.Texture;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.File;

import static org.lwjgl.opengl.GL46.*;

public class ComputeShader {

    /** The compute shader's id */
    public final int id;

    /**
     * Creates a new compute shader from the given source code.
     * @param computeShader the compute shader's source code.
     */
    public ComputeShader(String computeShader) {
        id = create(computeShader);
        if(id == -1) {
            BlobbyEngine.getLogger().error("An error occurred whilst trying to create the shader!");
        }
    }

    /**
     * Creates a new compute shader from the given source code path.
     * @param computeShaderPath the path to the compute shader's source code.
     */
    public ComputeShader(File computeShaderPath) {
        id = create(BlobbyEngine.readFile(computeShaderPath));
        if(id == -1) {
            BlobbyEngine.getLogger().error("An error occurred whilst trying to create the shader!");
        }
    }

    private int create(String computeSource) {
        int programId = glCreateProgram();

        int computeId = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(computeId, computeSource);
        glCompileShader(computeId);
        if(glGetShaderi(computeId, GL_COMPILE_STATUS) == GL_FALSE) {
            String infoLog = glGetShaderInfoLog(computeId);
            BlobbyEngine.getLogger().error("Could not create the compute shader! Log:\n" + infoLog);
            glDeleteShader(computeId);
            return -1;
        }

        glAttachShader(programId, computeId);
        glLinkProgram(programId);
        glValidateProgram(programId);

        glDeleteShader(computeId);
        return programId;
    }

    /**
     * Sets a 4x4 float matrix in the compute shader.
     * @param name the uniform variable name.
     * @param matrix the matrix to set in the compute shader.
     */
    public void setUniformMatrix4f(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(id, name);
        try(MemoryStack stack = MemoryStack.stackPush()) {
            if(location != -1)
                glUniformMatrix4fv(location, false, matrix.get(stack.mallocFloat(16)));
        }
    }

    /**
     * Sets a 3d float vector in the compute shader.
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
     * Sets an integer in the compute shader.
     * @param name the uniform variable name.
     * @param value the integer to set in the compute shader.
     */
    public void setUniform1i(String name, int value) {
        int location = glGetUniformLocation(id, name);
        if(location != -1)
            glUniform1i(location, value);
    }

    /**
     * Sets a float in the compute shader.
     * @param name the uniform variable name.
     * @param value the float to set in the compute shader.
     */
    public void setUniform1f(String name, float value) {
        int location = glGetUniformLocation(id, name);
        if(location != -1)
            glUniform1f(location, value);
    }

    public void setOutputTexture(Texture texture) {
        glBindImageTexture(0, texture.getId(), 0, false, 0, GL_READ_WRITE, GL_RGBA8);
    }

    /**
     * Runs the compute shader with the specified amount of work groups.
     * @param workGroupsX the amount of x work groups.
     * @param workGroupsY the amount of x work groups.
     * @param workGroupsZ the amount of x work groups.
     */
    public void run(int workGroupsX, int workGroupsY, int workGroupsZ, boolean waitUntilDone) {
        glDispatchCompute(workGroupsX, workGroupsY, workGroupsZ);
        if(waitUntilDone)
            glMemoryBarrier(GL_ALL_BARRIER_BITS);
    }

    /**
     * Binds the shader for compute usage.
     */
    public void bind() {
        glUseProgram(id);
    }

    /**
     * Unbinds the compute shader.
     */
    public void unbind() {
        glUseProgram(0);
    }

}
