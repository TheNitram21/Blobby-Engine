import de.arnomann.martin.blobby.MathUtil;
import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.SaveManager;
import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.Input;
import de.arnomann.martin.blobby.core.Renderer;
import de.arnomann.martin.blobby.core.texture.Particle;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.event.*;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.levels.LevelLoader;
import de.arnomann.martin.blobby.logging.Logger;
import de.arnomann.martin.blobby.physics.Physics;
import de.arnomann.martin.blobby.ui.Button;
import de.arnomann.martin.blobby.ui.Menu;
import de.arnomann.martin.blobby.ui.UI;
import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.io.File;
import java.io.IOException;
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
                false, 4), args);
    }

    @Override
    public void onStart(StartEvent event) {
        BlobbyEngine.debugMode();
        logger.setOutputFile(new File("output.log"));
        logger.info("Test for logging to files.");

        BlobbyEngine.getWindow().maxFramerate = -1;
        BlobbyEngine.getWindow().setVSyncEnabled(false);

        BlobbyEngine.setPlayer(new Player(new Vector2d(0, 0), Map.of("Texture", "player", "Width", "1")));
        LevelLoader.loadLevel("blobby_debug", this::changeLevel);
//        LevelLoader.loadLevel("npc_test", this::changeLevel);

        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(new Vector2f(0.025f, 0.1f), new Vector2f(0.225f, 0.18f),
                BlobbyEngine.getTexture("button"), BlobbyEngine::stop));
        BlobbyEngine.menu = new Menu(buttons, BlobbyEngine.getTexture("menuBack"));

        BlobbyEngine.getWindow().setWindowSize(1280, 720);
        Renderer.setScreenTransitionDuration(0.5);
        Renderer.setAmbientLight(0.55f);
        Renderer.defaultCamera.setZoom(0.9f);
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
        Renderer.addLightEmitter(new Vector2f(0.5f, 0.5f), 0.5f);
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

            if(playerOnGround && !onGroundLastFrame) {
                Vector2d particlePos = new Vector2d(p.getPosition());
                particlePos.add(p.getWidth() / 2d, 0);
                for(int i = 0; i < 3; i++) {
                    new Particle("dust", particlePos, new Vector2f((float) Math.random() * 2 - 1f, Math.min(
                            (float) -Math.random(), -0.5f)), new Vector2f(0.15f, 0.15f), new Vector2f(0.015f,
                            0.015f), 30f, 1110f, 1.5d);
                }
            }

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
                p.setPosition(p.getPosition().set(p.getPosition().x, MathUtil.roundWithMaxDifference((float) p.getPosition().y,
                        0.3f)));
            }

            if(headCollision && playerVelocity.y < 0) {
                playerVelocity.y = 0;
            }

            p.setPosition(p.getPosition().add(playerVelocity.x * event.deltaTime, playerVelocity.y * event.deltaTime));
            SaveManager.savedValues.put("PlayerX", p.getPosition().x);
            SaveManager.savedValues.put("PlayerY", p.getPosition().y);

            onGroundLastFrame = playerOnGround;
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

        if(event.key == Input.KEY_F6) {
            try {
                SaveManager.save();
                SaveManager.save("save_latest");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        if(event.key == Input.KEY_F7) {
            try {
                SaveManager.load();
                String levelName = (String) SaveManager.savedValues.get("CurrentLevel");
                Vector2d playerPos = new Vector2d((double) SaveManager.savedValues.get("PlayerX"), (double) SaveManager
                        .savedValues.get("PlayerY"));

                LevelLoader.loadLevel(levelName, this::changeLevel);
                BlobbyEngine.getPlayer().setPosition(playerPos);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        if(event.key == Input.KEY_F11) {
            BlobbyEngine.getWindow().setFullscreen(!BlobbyEngine.getWindow().getFullscreen());
            System.out.println(BlobbyEngine.getWindow().getWindowSize());
        }
    }

    private void changeLevel(Level level) {
        BlobbyEngine.setLevel(level);
        SaveManager.savedValues.put("CurrentLevel", level.fileName.substring(0, level.fileName.length() - 5));
        SaveManager.savedValues.put("PlayerX", BlobbyEngine.getPlayer().getPosition().x);
        SaveManager.savedValues.put("PlayerY", BlobbyEngine.getPlayer().getPosition().y);
    }

}
