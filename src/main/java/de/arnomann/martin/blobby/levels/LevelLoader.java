package de.arnomann.martin.blobby.levels;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.core.texture.ITexture;
import de.arnomann.martin.blobby.entity.Block;
import de.arnomann.martin.blobby.entity.Entity;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import de.arnomann.martin.blobby.logging.Logger;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * A class for loading levels.
 */
public class LevelLoader {

    private static final Logger logger = new Logger();

    private LevelLoader() {}

    /**
     * Loads a level.
     * @param filename the path of the level.
     * @return the loaded level.
     */
    public static Level loadLevel(String filename) {
        try {
            BlobbyEngine.renderPlayer = false;

            filename = BlobbyEngine.MAPS_PATH + filename + ".json";
            JSONObject json = BlobbyEngine.loadJSON(new File(filename));
            Level level;

            logger.info("Starting loading level '" + filename + "'...");

            Map<Vector2i, Screen> screens = new HashMap<>();
            for (Object screenObj : json.getJSONArray("Screens")) {
                JSONObject screenJson = (JSONObject) screenObj;
                List<Entity> entities = new ArrayList<>();

                int screenX = screenJson.getInt("X");
                int screenY = screenJson.getInt("Y");

                for (Object blockObj : screenJson.getJSONArray("Blocks")) {
                    JSONObject blockJson = (JSONObject) blockObj;
                    int x = blockJson.getInt("X");
                    int y = blockJson.getInt("Y");

                    Map<String, Object> blockProperties = new HashMap<>();
                    blockProperties.put("Texture", BlobbyEngine.getTexture(blockJson.getString("Path")));
                    Block block = new Block(new Vector2d(x + screenX * 16, y + screenY * 9), blockProperties);

                    entities.add(block);
                }

                try {
                    for (Object entityObj : screenJson.getJSONArray("Entities")) {
                        JSONObject entityJson = (JSONObject) entityObj;
                        int x = entityJson.getInt("X");
                        int y = entityJson.getInt("Y");
                        Entity e = null;

                        Map<String, Object> entityParameters = new HashMap<>();

                        Iterator<String> keys = entityJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            entityParameters.put(key, entityJson.get(key));
                        }

                        try {
                            e = BlobbyEngine.instantiateEntity(entityJson.getString("ClassName"), new Vector2d(x + screenX * 16, y + screenY * 9),
                                    entityParameters);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        entities.add(e);
                    }
                } catch(JSONException ignored) {}

                screens.put(new Vector2i(screenX, screenY), new Screen(entities));
            }

            BlobbyEngine.getPlayer().setPosition(json.getDouble("PlayerStartX"), json.getDouble("PlayerStartY"));
            String title = json.getString("Name");

            ITexture backgroundTexture = null;

            try {
                backgroundTexture = BlobbyEngine.getTexture(json.getString("BackgroundTexture"));
            } catch(JSONException ignored) {}

            BlobbyEngine.renderPlayer = true;

            level = new Level(title, screens, backgroundTexture);

            logger.info("Level '" + filename + "' loaded!");

            return level;
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
