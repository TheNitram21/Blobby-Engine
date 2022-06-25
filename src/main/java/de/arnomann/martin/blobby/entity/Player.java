package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2d;

import java.util.Map;

/**
 * Represents a player.
 */
public class Player extends Entity {

    private Vector2d position;
    private ITexture texture;

    public Player(Vector2d pos, Map<String, Object> parameters) {
        super(pos, parameters);
        position = pos;
        this.texture = (ITexture) parameters.get("Texture");
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    /**
     * Sets the player's position.
     * @param x the new x.
     * @param y the new y.
     */
    public void setPosition(double x, double y) {
        position = new Vector2d(x, y);
    }

    /**
     * Sets the player's position.
     * @param v the new position.
     */
    public void setPosition(Vector2d v) {
        position = v;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 2;
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
