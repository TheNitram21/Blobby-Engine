package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.texture.AnimatedTexture;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.core.texture.Texture;
import de.arnomann.martin.blobby.entity.Entity;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.StopEvent;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.levels.LevelLoader;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import de.arnomann.martin.blobby.logging.Logger;
import de.arnomann.martin.blobby.sound.SoundPlayer;
import de.arnomann.martin.blobby.ui.Button;
import de.arnomann.martin.blobby.ui.Menu;
import org.joml.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.Math;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * The main class of the engine.
 */
public final class BlobbyEngine {

    /** The path to textures. */
    public static final String TEXTURES_PATH = "textures/";
    /** The path to maps. */
    public static final String MAPS_PATH = "maps/";
    /** The path to sounds. */
    public static final String SOUNDS_PATH = "sounds/";
    /** The path to scripts. **/
    public static final String SCRIPTS_PATH = "scripts/";
    /** The path to shaders. */
    public static final String SHADERS_PATH = "shaders/";

    private static Window window;
    private static Map<String, ITexture> textures;
    private static Logger logger;
    private static Level currentLevel;
    private static Thread mainThread;

    /** The currently shown menu. */
    public static Menu menu;
    private static boolean showMenu = false;

    private static double unitMultiplier;

    private static Player player;
    /** Whether the player should be rendered or not. */
    public static boolean renderPlayer = false;

    /** Whether the game is paused or not. Can be set from the outside. */
    public static boolean paused = false;

    static boolean transitioningScreen = false;

    private static String[] consoleArguments;

    private static Texture loadingScreenTexture;

    private static Profiler profiler;
    private static boolean debugMode = false;

    private BlobbyEngine() {}

    /**
     * Starts the engine.
     * @param runConfig the configuration for running.
     * @param arguments the console arguments.
     */
    public static void run(RunConfigurations runConfig, String[] arguments) {
        textures = new HashMap<>();
        logger = new Logger();
        mainThread = Thread.currentThread();
        consoleArguments = arguments;

        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> logger.error("An uncaught exception occurred!", e));

        if(runConfig.width / 16 != runConfig.height / 9) {
            throw new RuntimeException("Window size ratio not 16:9");
        }

        if(!glfwInit()) {
            ErrorManagement.showErrorMessage(logger, new IllegalStateException("GLFW couldn't be initialized!"));
            stop();
        }

        window = new Window(runConfig);
        recalculateUnitMultiplier();

        Input.initialize();
        SoundPlayer.initialize();

        window.start();
    }

    static void onWindowOpen() {
        checkArguments(consoleArguments);
    }

    private static void checkArguments(String[] arguments) {
        for(String argument : arguments) {
            try {
                String[] split = argument.split(":", 2);

                String key = split[0];
                String value = split[1];

                switch(key) {
                    case "map":
                        LevelLoader.loadLevel(value, BlobbyEngine::setLevel);
                        break;
                    default:
                        break;
                }
            } catch(ArrayIndexOutOfBoundsException ignored) {} // Means that this argument is not in Blobby Engine's
                                                               // argument format. Could be JOML.
        }
    }

    /**
     * Recalculates the unit multiplier.
     */
    public static void recalculateUnitMultiplier() {
        unitMultiplier = window.getWidth() / 16d;
    }

    /**
     * Parses the given arguments.
     * @param args the arguments to parse.
     * @return the parsed arguments.
     */
    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();

        for(String argument : args) {
            try {
                String[] split = argument.split(":", 2);

                arguments.put(split[0], split[1]);
            } catch(ArrayIndexOutOfBoundsException ignored) {} // Means that this argument is not in Blobby Engine's
                                                               // argument format. Probably JOML.
        }

        return arguments;
    }

    /**
     * Creates a new entity.
     * @param classname the classname of the entity (as defined in the <i>bin/entities.json</i> file).
     * @param position the position where the entity is created.
     * @param properties the entity properties.
     * @return the created entity.
     * @throws Exception if the entity class is not found, there is no valid constructor or the constructor couldn't be called.
     */
    public static Entity instantiateEntity(String classname, Vector2d position, Map<String, String> properties)
            throws Exception { /* We could throw:
                                    ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
                                    But that would be a bit much. */
        JSONObject entitiesJSON = loadJSON(new File("bin/entities.json"));
        String classnameWithPackage = "";

        for (Object entityObj : entitiesJSON.getJSONArray("Entities")) {
            JSONObject entityJSON = (JSONObject) entityObj;

            if (entityJSON.getString("ClassName").equals(classname)) {
                try {
                    classnameWithPackage = entityJSON.getString("InternalClassName");
                } catch (JSONException e) {
                    return null;
                }
            }
        }

        if (classnameWithPackage.equals(""))
            throw new IllegalStateException("Couldn't find an entity with the classname " + classname + " in 'entities.json'!");

        Class<?> entityClass = Class.forName(classnameWithPackage);
        Entity entity = (Entity) entityClass.getConstructor(Vector2d.class, Map.class).newInstance(position, properties);
        ListenerManager.registerEventListener(entity);
        return entity;
    }

    /**
     * Checks OpenGL for errors.
     * @return the error, or {@code null} if no error occurred.
     */
    public static String checkForGLError() {
        int error = glGetError();
        if(error != GL_NO_ERROR)
            return glGetString(error);
        else
            return null;
    }

    /**
     * Checks OpenAL for errors.
     * @return the error, or {@code null} if no error occurred.
     */
    public static String checkForALError() {
        int error = alGetError();
        if(error != AL_NO_ERROR)
            return alGetString(error);
        else
            return null;
    }

    /**
     * Reads the content of a file.
     * @param path the path to the file.
     * @return the file content.
     */
    public static String readFile(File path) {
        if(!path.exists()) {
            ErrorManagement.showErrorMessage(logger, new FileNotFoundException("File " + path + " doesn't exist!"));
        }

        StringBuilder textBuilder = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append("\n");
            }
        } catch(IOException e) {
            ErrorManagement.showErrorMessage(logger, e);
        }

        return textBuilder.toString();
    }

    static String readInternalFile(String filename) {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(new File(BlobbyEngine.class
                    .getResource(filename).toURI())));
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            return content.toString();
        } catch(URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads a JSON from a file.
     * @param path the path to the JSON file.
     * @return the loaded JSON.
     */
    public static JSONObject loadJSON(File path) {
        return new JSONObject(readFile(path));
    }

    /**
     * Returns the amount of monitors connected to the computer.
     * @return the monitor count.
     */
    public static int getMonitorCount() {
        PointerBuffer monitors = glfwGetMonitors();
        return monitors.position() + monitors.remaining();
    }

    /**
     * Returns the id of the primary monitor.
     * @return the primary monitor's id;
     */
    public static long getPrimaryMonitorId() {
        return glfwGetPrimaryMonitor();
    }

    /**
     * Returns the width of the primary monitor.
     * @return the primary monitor's width;
     */
    public static int getPrimaryMonitorWidth() {
        GLFWVidMode videoMode = glfwGetVideoMode(getPrimaryMonitorId());
        return videoMode.width();
    }

    /**
     * Returns the height of the primary monitor.
     * @return the primary monitor's height;
     */
    public static int getPrimaryMonitorHeight() {
        GLFWVidMode videoMode = glfwGetVideoMode(getPrimaryMonitorId());
        return videoMode.height();
    }

    /**
     * Returns whether we are transitioning between to screens or not.
     * @return {@code true} if the game is transitioning between two screens, {@code false} otherwise.
     */
    public static boolean isTransitioningBetweenScreens() {
        return transitioningScreen;
    }

    /**
     * Returns the unit multiplier. Every world position is multiplied by this to get the screen position.
     * @return the unit multiplier.
     */
    public static double unitMultiplier() {
        return unitMultiplier;
    }

    /**
     * Enables the debug mode.
     */
    public static void debugMode() {
        debugMode = true;
        logger.enable(Logger.LoggingType.DEBUG);
        logger.debug("Debug mode activated");
        profiler = new Profiler();
    }

    /**
     * Returns the engine profiler. Will be <code>null</code> if debug mode is deactivated.
     * @return the profiler.
     * @see BlobbyEngine#debugMode()
     */
    public static Profiler getProfiler() {
        return profiler;
    }

    /**
     * Returns whether the engine is in debug mode.
     * @return <code>true</code> if the engine is in debug mode, <code>false</code> otherwise.
     */
    public static boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Returns the screen of a specific entity.
     * @param e the entity.
     * @return the screen.
     */
    public static Vector2i getEntityScreen(Entity e) {
        return new Vector2i((int) Math.floor((e.getPosition().x + e.getWidth() / 2d) / 16), (int) Math.floor((e.getPosition().y - e.getHeight() / 2d) / 9));
    }

    /**
     * Loads a texture from a path.
     * @param filename the texture path.
     * @return the texture.
     */
    public static ITexture loadTexture(String filename) {
        ITexture texture = getTextureWithoutCaching(filename);
        logger.debug("Loaded texture " + filename + " with size " + texture.getWidth() + ", " + texture.getHeight() + ".");
        textures.put(filename, texture);
        return texture;
    }

    /**
     * Gets a texture from a path. Loads the texture only if it hasn't been loaded yet.
     * @param name the path to the texture.
     * @return the texture.
     */
    public static ITexture getTexture(String name) {
        ITexture texture = textures.get(name);
        if(texture == null) {
            texture = loadTexture(name);
        }

        return texture;
    }

    /**
     * Loads a texture from a path. DOES NOT cache it.
     * @param filename the path of the texture.
     * @return the texture.
     */
    public static ITexture getTextureWithoutCaching(String filename) {
        ITexture texture;
        filename = TEXTURES_PATH + filename;
        File file = new File(filename);

        if(file.exists()) {
            double animTimeSecs = 1;
            try (BufferedReader r = new BufferedReader(new FileReader(filename + "/animTime.txt"))) {
                animTimeSecs = Double.parseDouble(r.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }

            texture = new AnimatedTexture(animTimeSecs, filename);
        } else {
            texture = new Texture(filename);
        }

        return texture;
    }

    static Texture getInternalTexture(String filename) {
        Texture texture = null;

        try {
            InputStream stream = BlobbyEngine.class.getResourceAsStream(filename + ".png");
            texture = new Texture(ImageIO.read(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return texture;
    }

    /**
     * Shows the menu.
     */
    public static void showMenu() {
        showMenu = true;
        menu.getButtons().forEach(Button::show);
    }

    /**
     * Hides the menu.
     */
    public static void hideMenu() {
        showMenu = false;
        menu.getButtons().forEach(Button::hide);
    }

    /**
     * Returns whether the menu is currently shown.
     * @return {@code true} if the menu is visible, {@code false} otherwise.
     */
    public static boolean isMenuShown() {
        return showMenu;
    }

    /**
     * Sets the current level. The old level has to be reloaded if you want to use it again.
     * @param level the new current level.
     * @see BlobbyEngine#getCurrentLevel()
     */
    public static void setLevel(Level level) {
        try {
            currentLevel.screens.forEach((screenPos, screen) -> screen.entities.forEach(ListenerManager::removeEventListener));
        } catch(NullPointerException ignored) {} // No level is loaded yet
        SoundPlayer.stopAllSounds();

        currentLevel = level;
        Renderer.defaultCamera.setPosition(new Vector2f(getEntityScreen(player).x * Renderer.defaultCamera.getWidth(),
                -getEntityScreen(player).y * Renderer.defaultCamera.getHeight()));
    }

    /**
     * Returns the current level.
     * @return the current level.
     * @see BlobbyEngine#setLevel(Level)
     */
    public static Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Returns the player.
     * @return the player.
     */
    public static Player getPlayer() {
        return player;
    }

    /**
     * Sets the player.
     * @param newPlayer the new player.
     */
    public static void setPlayer(Player newPlayer) {
        player = newPlayer;
    }

    /**
     * Returns the window so that you can perform operations like resizing.
     * @return the window.
     * @see Window
     */
    public static Window getWindow() {
        return window;
    }

    /**
     * Returns the logger. You should create your own logger though.
     * @return the logger.
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Returns the main thread used for rendering.
     * @return the main thread.
     */
    public static Thread getMainThread() {
        return mainThread;
    }

    /**
     * Shuts down the engine.
     */
    public static void stop() {
        if(window != null)
            glfwSetWindowShouldClose(window.getId(), true);

        logger.destroy();
        LevelLoader.logger.destroy();
        ListenerManager.callEvent(new StopEvent());
    }

    /**
     * Returns the default window icon for use when no window icon is provided.
     * @return the default window icon.
     */
    public static Texture getDefaultWindowIcon() {
        return getInternalTexture("defaultIcon");
    }

    /**
     * Shows a loading screen.
     */
    public static void showLoadingScreen() {
        if(loadingScreenTexture == null)
            loadingScreenTexture = getInternalTexture("loadingScreen");

        Renderer.setWindow(window);
        Renderer.renderUV(new Vector2f(-1, 1), new Vector2f(1, -1), loadingScreenTexture, Renderer.uiShader);
        Renderer.finishRendering();
    }

}
