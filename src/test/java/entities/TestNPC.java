package entities;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.entity.BaseNPC;
import de.arnomann.martin.blobby.sound.SoundPlayer;
import org.joml.Vector2d;

import java.util.Map;

public class TestNPC extends BaseNPC {

    public TestNPC(Vector2d position, Map<String, String> parameters) {
        super(position, parameters);
    }

    @Override
    public void moveTo(String x, String y, String unitsPerSecond) {
        System.out.println("MOVETO");
        setPosition(new Vector2d(Float.parseFloat(x), Float.parseFloat(y)));
    }

    @Override
    public void say(String text) {
        System.out.println(text);
        SoundPlayer.playSound("sound.ogg");
    }

    @Override
    public void wait(String seconds) {
        System.out.println("WAIT");
        try {
            Thread.sleep((long) (Float.parseFloat(seconds) * 1000));
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void die() {}

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public ITexture getTexture() {
        return BlobbyEngine.getTexture("player");
    }

}
