import de.arnomann.martin.blobby.MathUtil;
import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.Input;
import de.arnomann.martin.blobby.core.Renderer;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.event.*;
import de.arnomann.martin.blobby.levels.LevelLoader;
import de.arnomann.martin.blobby.logging.Logger;
import de.arnomann.martin.blobby.physics.Physics;
import de.arnomann.martin.blobby.ui.Button;
import de.arnomann.martin.blobby.ui.Menu;
import de.arnomann.martin.blobby.ui.UI;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EngineTest implements EventListener {

    public static final int[] windowSize = { 960, 540 };
    public static final Logger logger = new Logger();

    public static void main(String[] args) {
        logger.enable(Logger.LoggingType.DEBUG);
        ListenerManager.registerEventListener(new EngineTest());
        BlobbyEngine.run(new RunConfigurations("Blobby Engine Test", windowSize[0], windowSize[1], null,
                false), args);
    }

    @Override
    public void onStart(StartEvent event) {
        BlobbyEngine.debugMode();
        logger.setOutputFile(new File("output.log"));
        logger.info("Test for logging to files.");

        BlobbyEngine.getWindow().maxFramerate = -1;
        BlobbyEngine.getWindow().setVSyncEnabled(false);

        BlobbyEngine.setPlayer(new Player(new Vector2d(0, 0), Map.of("Texture", "player", "Width", "1")));
//        LevelLoader.loadLevel("blobby_debug", BlobbyEngine::setLevel);
        LevelLoader.loadLevel("npc_test", BlobbyEngine::setLevel);

        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(new Vector2f(0.025f, 0.1f), new Vector2f(0.225f, 0.18f),
                BlobbyEngine.getTexture("button"), BlobbyEngine::stop));
        BlobbyEngine.menu = new Menu(buttons, BlobbyEngine.getTexture("menuBack"));

        BlobbyEngine.getWindow().setWindowSize(1280, 720);
        Renderer.setScreenTransitionDuration(0.5);
    }

    @Override
    public void onStop(StopEvent event) {
        logger.destroy();
    }

    public static Vector2d playerVelocity = new Vector2d();
    public static final float fallSpeed = 20f;
    public static final float jumpHeight = 7f;
    public static final float maxSpeed = 6.25f;
    public static final float speedFalloff = 1f;
    private boolean onGroundLastFrame = false;

    @Override
    public void onUpdate(UpdateEvent event) {
        BlobbyEngine.getWindow().setTitle("Blobby Engine Test - " + (int) Math.floor(event.fps) + " FPS");
        UI.drawUI(new Vector2f(0.025f, 0.025f), new Vector2f(0.3f, 0.15f), BlobbyEngine.getTexture("uiTopLeft"));

        String error = BlobbyEngine.checkForGLError();
        if(error != null)
            System.out.println("A GL error occurred!\n" + error);
        error = BlobbyEngine.checkForALError();
        if(error != null)
            System.out.println("An AL error occurred!\n" + error);

        if(!BlobbyEngine.paused && !BlobbyEngine.isTransitioningBetweenScreens()) {
            Player p = BlobbyEngine.getPlayer();

            boolean playerOnGround = Physics.objectInBox(new Vector2d(p.getPosition()).add(p.getWidth() / 4d, 0),
                    p.getWidth() * 0.25, 0.05, "Block");
            boolean headCollision = Physics.objectInBox(new Vector2d(p.getPosition()).add(p.getWidth() / 4d, -p.getHeight()),
                    p.getWidth() * 0.25, 0.05, "Block");

            boolean canGoRight = !Physics.objectInBox(new Vector2d(p.getPosition()).add(p.getWidth(), -p.getHeight() * 0.9),
                    0.05, p.getHeight() * 0.75, "Block");
            boolean canGoLeft = !Physics.objectInBox(new Vector2d(p.getPosition()).add(0, -p.getHeight() * 0.9),
                    0.05, p.getHeight() * 0.75, "Block");

            if(!BlobbyEngine.isTransitioningBetweenScreens()) {
                if(Input.keyPressed(Input.KEY_A) && !Input.keyPressed(Input.KEY_D) && canGoLeft) {
                    playerVelocity.x = -maxSpeed;
                } else if(Input.keyPressed(Input.KEY_D) && !Input.keyPressed(Input.KEY_A) && canGoRight) {
                    playerVelocity.x = maxSpeed;
                }

                if(Input.keyPressed(Input.KEY_SPACE) && playerOnGround) {
                    playerVelocity.y = -jumpHeight;
                }
            }

            if((playerVelocity.x > 0 && !canGoRight) || (playerVelocity.x < 0 && !canGoLeft)) {
                playerVelocity.x = 0;
            }

            if(playerVelocity.x > 0) {
                p.getTexture().setFlipped(true);
            } else if(playerVelocity.x < 0) {
                p.getTexture().setFlipped(false);
            }

            if(playerOnGround && (BlobbyEngine.isTransitioningBetweenScreens() || (!Input.keyPressed(Input.KEY_A) &&
                    !Input.keyPressed(Input.KEY_D)))) {
                if(playerVelocity.x < -speedFalloff) {
                    playerVelocity.x = playerVelocity.x * speedFalloff * 0.75;
                } else if(playerVelocity.x > speedFalloff) {
                    playerVelocity.x = playerVelocity.x * speedFalloff * 0.75;
                } else if(MathUtil.inclusiveBetween(-speedFalloff, speedFalloff, playerVelocity.x)) {
                    playerVelocity.x = 0;
                }
            }

            if(!playerOnGround) {
                playerVelocity.y += fallSpeed * event.deltaTime;
            } else if(playerVelocity.y > 0) {
                playerVelocity.y = 0;
                p.getPosition().set(p.getPosition().x, MathUtil.roundWithMaxDifference((float) p.getPosition().y,
                        0.3f));
            }

            if(headCollision && playerVelocity.y < 0) {
                playerVelocity.y = 0;
            }

            p.getPosition().add(playerVelocity.x * event.deltaTime, playerVelocity.y * event.deltaTime);
        }
    }

    @Override
    public void onKeyPressed(KeyPressedEvent event) {
        if(event.key == Input.KEY_ESCAPE) {
            if(BlobbyEngine.isMenuShown()) {
                BlobbyEngine.hideMenu();
                BlobbyEngine.paused = false;
            } else {
                BlobbyEngine.showMenu();
                BlobbyEngine.paused = true;
            }
        }

        if(event.key == Input.KEY_F)
            BlobbyEngine.getWindow().setFullscreen(!BlobbyEngine.getWindow().getFullscreen());
        if(event.key == Input.KEY_G)
            BlobbyEngine.getWindow().setWindowSize(1280, 720);
        if(event.key == Input.KEY_T)
            BlobbyEngine.getWindow().setWindowSize(854, 480);

        if(event.key == Input.KEY_V)
            BlobbyEngine.getWindow().setVSyncEnabled(!BlobbyEngine.getWindow().isVSyncEnabled());
    }
}
