import de.arnomann.martin.blobby.MathUtil;
import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.Input;
import de.arnomann.martin.blobby.core.Renderer;
import de.arnomann.martin.blobby.core.texture.ITexture;
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
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

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

        BlobbyEngine.getWindow().maxFramerate = -1;

        BlobbyEngine.setPlayer(new Player(new Vector2d(0, 0), Map.of("Texture", "player", "Width", "1")));
        LevelLoader.loadLevel("blobby_debug", BlobbyEngine::setLevel);

        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(new Vector2f(0.025f, 0.1f), new Vector2f(0.225f, 0.18f),
                BlobbyEngine.getTexture("button"), BlobbyEngine::stop));
        BlobbyEngine.menu = new Menu(buttons, BlobbyEngine.getTexture("menuBack"));

        Renderer.setScreenTransitionDuration(0.5);
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
                if(Input.keyPressed(GLFW_KEY_A) && canGoLeft) {
                    playerVelocity.x = -maxSpeed;
                } else if(Input.keyPressed(GLFW_KEY_D) && canGoRight) {
                    playerVelocity.x = maxSpeed;
                }

                if(Input.keyPressed(GLFW_KEY_SPACE) && playerOnGround) {
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

            if(playerOnGround && (BlobbyEngine.isTransitioningBetweenScreens() || (!Input.keyPressed(GLFW_KEY_A) &&
                    !Input.keyPressed(GLFW_KEY_D)))) {
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
            }

            if(headCollision && playerVelocity.y < 0) {
                playerVelocity.y = 0;
            }

            p.getPosition().add(playerVelocity.x * event.deltaTime, playerVelocity.y * event.deltaTime);
        }

//        System.out.println(Physics.raycast(p.getPosition(), new Vector2d(p.getPosition()).add(0, 4), "Block"));
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

        if(event.key == GLFW_KEY_V)
            BlobbyEngine.getWindow().setVSyncEnabled(!BlobbyEngine.getWindow().isVSyncEnabled());

        if(event.key == GLFW_KEY_L) {
            BlobbyEngine.getCurrentLevel().screens.forEach((screenPos, screen) -> screen.entities.forEach(entity -> {
                if(!entity.getClass().getSimpleName().equals("Block"))
                    return;
                Vector4f color = entity.getTexture().getColorModifiers();
                entity.getTexture().setColorModifiers(color.x + 0.005f, color.y + 0.005f, color.z + 0.005f, 1f);
            }));
        }
        if(event.key == GLFW_KEY_K) {
            BlobbyEngine.getCurrentLevel().screens.forEach((screenPos, screen) -> screen.entities.forEach(entity -> {
                if(!entity.getClass().getSimpleName().equals("Block"))
                    return;
                Vector4f color = entity.getTexture().getColorModifiers();
                entity.getTexture().setColorModifiers(color.x - 0.005f, color.y - 0.005f, color.z - 0.005f, 1f);
            }));
        }
    }
}
