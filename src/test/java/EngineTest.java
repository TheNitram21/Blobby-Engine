import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.Input;
import de.arnomann.martin.blobby.core.texture.Particle;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.sound.Sound;
import de.arnomann.martin.blobby.sound.SoundPlayer;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class EngineTest implements EventListener {

    public static final int[] windowSize = { 960, 540 };
    public static final Logger logger = new Logger();

    public static void main(String[] args) {
        logger.enable(Logger.LoggingType.DEBUG);
        ListenerManager.registerEventListener(new EngineTest());
        BlobbyEngine.run(new RunConfigurations("Blobby Engine Test", windowSize[0], windowSize[1], null, false), args);
    }

    @Override
    public void onStart(StartEvent event) {
        BlobbyEngine.debugMode();

        BlobbyEngine.getWindow().maxFramerate = -1;

        Map<String, String> playerParameters = new HashMap<>();
        playerParameters.put("Texture", "player");
        BlobbyEngine.setPlayer(new Player(new Vector2d(0, 0), playerParameters));
        LevelLoader.loadLevel("blobby_debug", BlobbyEngine::setLevel);

        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(new Vector2f(0.025f, 0.1f), new Vector2f(0.225f, 0.18f),
                BlobbyEngine.getTexture("button"), BlobbyEngine::stop));
        BlobbyEngine.menu = new Menu(buttons, BlobbyEngine.getTexture("menuBack"));
    }

    private double playerYVelocity = 0;
    private final double fallSpeed = 9.81f;
    private boolean onGroundLastFrame = false;

    @Override
    public void onUpdate(UpdateEvent event) {
        BlobbyEngine.getWindow().setTitle("Blobby Engine Test - " + (int) Math.floor(event.fps) + " FPS");
        UI.drawUI(new Vector2f(0.025f, 0.025f), new Vector2f(0.3f, 0.15f), BlobbyEngine.getTexture("uiTopLeft"));

        if(!BlobbyEngine.paused) {
            Player p = BlobbyEngine.getPlayer();
            boolean playerOnGround = Physics.objectInBox(new Vector2d(p.getPosition()).add(p.getWidth() / 4d, 0), 0.5, 0.05,
                    "Block");

            if(playerOnGround && !onGroundLastFrame) {
                new Particle("dust", new Vector2d(p.getPosition()).sub(0, 1));
            }

            Vector2d move = new Vector2d();

            if(!BlobbyEngine.isTransitioningBetweenScreens()) {
                if (Input.keyPressed(GLFW_KEY_A) && !Physics.objectInBox(new Vector2d(p.getPosition()).add(-p.getWidth() / 5, -p.getHeight()),
                        0.05, 1.5, "Block"))
                    move.add(-5 * event.deltaTime, 0);
                else if (Input.keyPressed(GLFW_KEY_D) && !Physics.objectInBox(new Vector2d(p.getPosition()).add(p.getWidth(), -p.getHeight()),
                        0.05, 1.5, "Block"))
                    move.add(5 * event.deltaTime, 0);

                if (Input.keyPressed(GLFW_KEY_SPACE) && playerOnGround)
                    playerYVelocity = .01f * -fallSpeed;
            }

            if(playerOnGround && playerYVelocity > 0) {
                playerYVelocity = 0;
            }

            playerYVelocity = playerYVelocity + .02f * fallSpeed * event.deltaTime;
            if(!playerOnGround || playerYVelocity < 0)
                move.add(0, playerYVelocity);
            p.setPosition(p.getPosition().add(move));

            onGroundLastFrame = playerOnGround;
        }

//        System.out.println(Physics.raycast(p.getPosition(), new Vector2d(p.getPosition()).add(0, 4), "Block"));
    }

    Sound hiSound;

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

        if(event.key == GLFW_KEY_M) {
            if(hiSound == null)
                hiSound = SoundPlayer.createSound("hi.ogg");
            SoundPlayer.playSound(hiSound);
        }
        if(event.key == GLFW_KEY_X)
            SoundPlayer.stopSound(hiSound);

        if(event.key == GLFW_KEY_V)
            BlobbyEngine.getWindow().setVSyncEnabled(!BlobbyEngine.getWindow().isVSyncEnabled());
    }
}
