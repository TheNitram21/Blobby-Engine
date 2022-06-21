package de.arnomann.martin.blobby.core;

import de.arnomann.martin.blobby.RunConfigurations;
import de.arnomann.martin.blobby.core.texture.AnimatedTexture;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.core.texture.Texture;
import de.arnomann.martin.blobby.entity.Entity;
import de.arnomann.martin.blobby.entity.Player;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.levels.Level;
import de.arnomann.martin.blobby.levels.LevelLoader;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import de.arnomann.martin.blobby.logging.Logger;
import de.arnomann.martin.blobby.sound.SoundPlayer;
import de.arnomann.martin.blobby.ui.Menu;
import org.joml.*;
import org.json.JSONObject;

import java.io.*;
import java.lang.Math;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public final class BlobbyEngine {

    public static final String TEXTURES_PATH = "textures/";
    public static final String MAPS_PATH = "maps/";
    public static final String SOUNDS_PATH = "sounds/";

    private static Window window;
    private static Map<String, ITexture> textures;
    private static Logger logger;
    private static Level currentLevel;

    public static Menu menu;
    public static boolean showMenu = false;

    private static double unitMultiplier;

    private static Player player;
    public static boolean renderPlayer = false;

    public static boolean paused = false;

    static boolean transitioningScreen = false;

    private static String[] consoleArguments;

    private BlobbyEngine() {}

    public static void run(RunConfigurations runConfig, String[] arguments) {
        textures = new HashMap<>();
        logger = new Logger();
        consoleArguments = arguments;

        if(runConfig.width / 16 != runConfig.height / 9) {
            throw new RuntimeException("Window size ratio not 16:9");
        }

        if(!glfwInit()) {
            ErrorManagement.showErrorMessage(logger, new IllegalStateException("GLFW couldn't be initialized!"));
            stop();
        }

        window = new Window(runConfig);
        unitMultiplier = window.getWidth() / 16d;

        Input.initialize();
        SoundPlayer.initialize();

        window.start();
    }

    static void onWindowOpen() {
        checkArguments(consoleArguments);
    }

    private static void checkArguments(String[] arguments) {
        for(String argument : arguments) {
            String[] split = argument.split(":", 1);

            String key = split[0];
            String value = split[1];

            switch(key) {
                case "map":
                    currentLevel = LevelLoader.loadLevel(value);
                    break;
                default:
                    break;
            }
        }
    }

    public static Entity instantiateEntity(String classname, Vector2d position, ITexture texture, Map<String, Object> properties)
            throws Exception { /* We could throw:
                                    ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
                                    But that would be a bit much. */
        JSONObject entitiesJSON = loadJSON(new File("bin/entities.json"));
        String classnameWithPackage = "";

        for(Object entityObj : entitiesJSON.getJSONArray("Entities")) {
            JSONObject entityJSON = (JSONObject) entityObj;

            if(entityJSON.getString("ClassName").equals(classname)) {
                classnameWithPackage = entityJSON.getString("InternalClassName");
            }
        }

        if(classnameWithPackage.equals(""))
            throw new IllegalStateException("Couldn't find an entity with the classname " + classname + " in 'entities.json'!");

        Class<?> entityClass = Class.forName(classnameWithPackage);
        Entity entity = (Entity) entityClass.getConstructor(Vector2d.class, ITexture.class, Map.class).newInstance(position, texture, properties);
        ListenerManager.registerEventListener(entity);
        return entity;
    }

    public static JSONObject loadJSON(File path) {
        JSONObject json;

        if(!path.exists()) {
            ErrorManagement.showErrorMessage(logger, new FileNotFoundException("File " + path + " doesn't exist!"));
        }

        StringBuilder jsonTextBuilder = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonTextBuilder.append(line).append("\n");
            }
        } catch(IOException e) {
            ErrorManagement.showErrorMessage(logger, e);
        }

        json = new JSONObject(jsonTextBuilder.toString());
        return json;
    }

    public static boolean isTransitioningBetweenScreens() {
        return transitioningScreen;
    }

    public static double unitMultiplier() {
        return unitMultiplier;
    }

    public static void debugMode() {
        logger.enable(Logger.LoggingType.DEBUG);
        logger.debug("Debug mode activated");
    }

    public static Vector2i getEntityScreen(Entity e) {
        return new Vector2i((int) Math.floor((e.getPosition().x + e.getWidth() / 2d) / 16), (int) Math.floor((e.getPosition().y + e.getHeight() / 2d) / 9));
    }

    public static ITexture loadTexture(String filename) {
        ITexture texture;
        File file = new File(filename);

        if(file.exists()) {
            double animTimeSecs = 1;
            try(BufferedReader r = new BufferedReader(new FileReader(filename + "/animTime.txt"))) {
                animTimeSecs = Double.parseDouble(r.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }

            texture = new AnimatedTexture(animTimeSecs, filename);
        } else {
            texture = new Texture(filename);
        }

        logger.debug("Loaded texture " + filename + " with size " + texture.getWidth() + ", " + texture.getHeight() + ".");

        textures.put(filename, texture);
        return texture;
    }

    public static ITexture getTexture(String name) {
        name = TEXTURES_PATH + name;
        ITexture texture = textures.get(name);
        if(texture == null) {
            texture = loadTexture(name);
        }

        return texture;
    }

    public static void setLevel(Level level) {
        currentLevel = level;
    }

    public static Level getCurrentLevel() {
        return currentLevel;
    }

    public static Player getPlayer() {
        return player;
    }

    public static void setPlayer(Player newPlayer) {
        player = newPlayer;
    }

    public static Window getWindow() {
        return window;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void stop() {
        if(window != null)
            glfwSetWindowShouldClose(window.getId(), true);
    }

}
