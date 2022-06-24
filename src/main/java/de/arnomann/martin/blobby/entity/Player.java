package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.texture.ITexture;
import org.joml.Vector2d;

import java.util.Map;

public class Player extends Entity {

    private Vector2d position;
    private ITexture texture;

    public Player(Vector2d pos, ITexture texture, Map<String, Object> parameters) {
        super(pos, texture, parameters);
        position = pos;
        this.texture = texture;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    public void setPosition(double x, double y) {
        position = new Vector2d(x, y);
    }

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

    public void setTextureToRender(ITexture texture) {
        this.texture = texture;
    }

    @Override
    public ITexture getTexture() {
        return texture;
    }

}
