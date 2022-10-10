package de.arnomann.martin.blobby.levels;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.core.texture.Texture;
import de.arnomann.martin.blobby.entity.Block;
import de.arnomann.martin.blobby.entity.Entity;
import de.arnomann.martin.blobby.logging.Logger;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * A class for loading levels.
 */
public class LevelLoader {

    private static final Logger logger = new Logger();
    private static boolean loadingLevel = false;

    private LevelLoader() {}

    /**
     * Loads a level.
     * @param name the path of the level.
     * @param whenDone called when the level is loaded.
     */
    public static void loadLevel(String name, Consumer<Level> whenDone) {
        String fileName = BlobbyEngine.MAPS_PATH + name + ".json";

        BlobbyEngine.showLoadingScreen();

        try {
            BlobbyEngine.renderPlayer = false;

            JSONObject json = BlobbyEngine.loadJSON(new File(fileName));
            Level level;

            logger.info("Starting loading level '" + fileName + "'...");
            loadingLevel = true;

            Map<Vector2i, Screen> screens = new HashMap<>();
            for(Object screenObj : json.getJSONArray("Screens")) {
                JSONObject screenJson = (JSONObject) screenObj;
                List<Entity> entities = new ArrayList<>();

                int screenX = screenJson.getInt("X");
                int screenY = screenJson.getInt("Y");

                for(Object blockObj : screenJson.getJSONArray("Blocks")) {
                    JSONObject blockJson = (JSONObject) blockObj;
                    int x = blockJson.getInt("X");
                    int y = blockJson.getInt("Y");

                    Map<String, String> blockProperties = new HashMap<>();
                    blockProperties.put("Texture", blockJson.getString("Path"));
                    Block block = new Block(new Vector2d(x + screenX * 16, y + screenY * 9), blockProperties);

                    entities.add(block);
                }

                try {
                    for(Object entityObj : screenJson.getJSONArray("Entities")) {
                        JSONObject entityJson = (JSONObject) entityObj;
                        int x = entityJson.getInt("X");
                        int y = entityJson.getInt("Y");
                        Entity e = null;

                        Map<String, String> entityParameters = new HashMap<>();

                        Iterator<String> keys = entityJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (key.equals("X") || key.equals("Y") || key.equals("ClassName"))
                                continue;

                            entityParameters.put(key, entityJson.getString(key));
                        }

                        try {
                            e = BlobbyEngine.instantiateEntity(entityJson.getString("ClassName"), new Vector2d(x + screenX * 16, y + screenY * 9),
                                    entityParameters);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        if(e != null)
                            entities.add(e);
                    }
                } catch(JSONException ignored) {
                }

                screens.put(new Vector2i(screenX, screenY), new Screen(entities));
            }

            BlobbyEngine.getPlayer().setPosition(json.getDouble("PlayerStartX"), json.getDouble("PlayerStartY"));
            String title = json.getString("Name");

            ITexture backgroundTexture = null;

            try {
                backgroundTexture = BlobbyEngine.getTexture(json.getString("BackgroundTexture"));
            } catch(JSONException ignored) {}

            Texture lightMapTexture = new Texture(BlobbyEngine.MAPS_PATH + name);

            BlobbyEngine.renderPlayer = true;

            level = new Level(title, screens, backgroundTexture, lightMapTexture, name);

            logger.info("Level '" + fileName + "' loaded!");

            whenDone.accept(level);
            level.screens.forEach((pos, screen) -> screen.entities.forEach(Entity::initialize));
        } catch(JSONException e) {
            e.printStackTrace();
        } finally {
            loadingLevel = false;
        }
    }

    public static boolean isLoadingLevel() {
        return loadingLevel;
    }

}
