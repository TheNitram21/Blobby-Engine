package entities;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.entity.Entity;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.Map;


public class Ladder extends Entity {

    private Vector2d position;

    public Ladder(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
        this.position = position;
    }

    public void printMessage() {
        System.out.println("Message printed!");
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public Vector2d getRenderingOffset() {
        return new Vector2d(0, Math.sin(GLFW.glfwGetTime()) * 0.1);
    }

    @Override
    public ITexture getTexture() {
        return null;
    }

    @Override
    public boolean renderInFrontOfPlayer() {
        return false;
    }

}
