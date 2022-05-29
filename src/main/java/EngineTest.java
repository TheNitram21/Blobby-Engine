import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.Input;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.core.Sound;
import de.arnomann.martin.blobby.event.*;
import de.arnomann.martin.blobby.levels.LevelLoader;
import de.arnomann.martin.blobby.logging.Logger;
import de.arnomann.martin.blobby.physics.Physics;
import de.arnomann.martin.blobby.ui.Button;
import de.arnomann.martin.blobby.ui.Menu;
import de.arnomann.martin.blobby.ui.UI;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class EngineTest implements EventListener {

    public static final int[] windowSize = { 960, 540 };
    public static final Logger logger = new Logger();

    public static void main(String[] args) {
        logger.enable(Logger.LoggingType.DEBUG);
        ListenerManager.registerEventListener(new EngineTest());
        BlobbyEngine.run(new RunConfigurations("Blobby Engine Test", windowSize[0], windowSize[1]));
    }

    @Override
    public void onStart(StartEvent event) {
        BlobbyEngine.debugMode();

        BlobbyEngine.setPlayer(new Player(new Vector2d(0, 0), BlobbyEngine.getTexture("player"), null));
        Player p = BlobbyEngine.getPlayer();
        p.setTextureToRender(BlobbyEngine.getTexture("player"));
        BlobbyEngine.setLevel(LevelLoader.loadLevel("blobby_debug.json"));


        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(new Vector2f(0.025f, 0.1f), new Vector2f(0.225f, 0.18f), BlobbyEngine.getTexture("button"), () -> {}));
        BlobbyEngine.menu = new Menu(buttons, BlobbyEngine.getTexture("menuBack"));
    }

    private double playerYVelocity = 0;
    private final double fallSpeed = 9.81f;

    @Override
    public void onUpdate(UpdateEvent event) {
        BlobbyEngine.getWindow().setTitle("Blobby Engine Test - " + (int) Math.floor(event.fps) + " FPS");
        UI.drawUI(new Vector2f(0.025f, 0.025f), new Vector2f(0.3f, 0.15f), BlobbyEngine.getTexture("uiTopLeft"));

        if(!BlobbyEngine.paused) {
            Player p = BlobbyEngine.getPlayer();
            boolean playerOnGround = Physics.objectInBox(new Vector2d(p.getPosition()).add(0, p.getHeight() / 2d), 1, 0.2,
                    "Block");

            Vector2d move = new Vector2d();
            if(Input.keyPressed(GLFW_KEY_A))
                move.add(-5 * event.deltaTime, 0);
            else if(Input.keyPressed(GLFW_KEY_D))
                move.add(5 * event.deltaTime, 0);

            if(Input.keyPressed(GLFW_KEY_SPACE))
                playerYVelocity = .01f * -fallSpeed;
            if(playerOnGround && playerYVelocity > 0) {
                playerYVelocity = 0;
            }

            playerYVelocity = playerYVelocity + .02f * fallSpeed * event.deltaTime;
            if(!playerOnGround || playerYVelocity < 0)
                move.add(0, playerYVelocity);
            p.setPosition(p.getPosition().add(move));
        }
    }

    @Override
    public void onKeyPressed(KeyPressedEvent event) {
        if(event.key == GLFW_KEY_ESCAPE) {
            if(BlobbyEngine.showMenu) {
                BlobbyEngine.showMenu = false;
                BlobbyEngine.paused = false;
            } else {
                BlobbyEngine.showMenu = true;
                BlobbyEngine.paused = true;
            }
        }

        if(event.key == GLFW_KEY_M)
            Sound.playSound("hi.ogg");
        if(event.key == GLFW_KEY_X)
            Sound.stopAllSounds();
    }
}
