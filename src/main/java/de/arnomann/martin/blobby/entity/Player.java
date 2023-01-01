package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2d;

import java.util.Map;

/**
 * Represents a player.
 */
public class Player extends Entity {

    private Vector2d position;
    private ITexture texture;
    private int width = 1;
    private int height = 2;
    private boolean allowMovement = true;

    public Player(Vector2d pos, Map<String, String> parameters) {
        super(pos, parameters);
        position = pos;

        this.texture = BlobbyEngine.getTexture(parameters.get("Texture"));
        this.width = Integer.parseInt(parameters.getOrDefault("Width", "1"));
        this.height = Integer.parseInt(parameters.getOrDefault("Height", "2"));
    }

    @Override
    public Vector2d getPosition() {
        return new Vector2d(position);
    }

    /**
     * Sets the player's position.
     * @param x the new x.
     * @param y the new y.
     */
    public void setPosition(double x, double y) {
        if(allowMovement) {
            position.x = x;
            position.y = y;
        }
    }

    /**
     * Sets the player's position.
     * @param v the new position.
     */
    public void setPosition(Vector2d v) {
        if(allowMovement) {
            position.x = v.x;
            position.y = v.y;
        }
    }

    /**
     * Allows player movement.
     */
    public void allowMovement() {
        allowMovement = true;
    }

    /**
     * Prevents player movement.
     */
    public void preventMovement() {
        allowMovement = false;
        System.out.println("MOVEMENT PREVENTED");
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Sets this entity's width.
     * @param width the new width.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Sets this entity's height.
     * @param height the new height.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Sets the player's texture.
     * @param texture the new texture.
     */
    public void setTextureToRender(ITexture texture) {
        this.texture = texture;
    }

    @Override
    public ITexture getTexture() {
        return texture;
    }

}
