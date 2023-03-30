package de.arnomann.martin.blobby;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.event.ListenerManager;
import de.arnomann.martin.blobby.event.LoadEvent;
import de.arnomann.martin.blobby.event.SaveEvent;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * A utility class for saving and loading save files.
 */
public class SaveManager {

    public static final HashMap<String, Object> savedValues = new HashMap<>();

    private SaveManager() {}

    /**
     * Saves the savedValues hash map to a file on disk.
     * @throws IOException if an error occurred while saving.
     */
    public static void save() throws IOException {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
        String currentTime = OffsetDateTime.now().format(timeFormatter);
        save("save_" + currentTime);
    }

    /**
     * Saves the savedValues hash map to a file on disk.
     * @param saveFileName the name of the save file.
     * @throws IOException if an error occurred while saving.
     */
    public static void save(String saveFileName) throws IOException {
        File file = new File(BlobbyEngine.SAVES_PATH + saveFileName + ".sav");
        if(file.exists())
            file.delete();
        file.createNewFile();

        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(savedValues);

        outputStream.close();

        ListenerManager.callEvent(new SaveEvent());
    }

    /**
     * Loads the latest save file.
     * @return the loaded values.
     * @throws IOException if an error occurred while reading the save file.
     * @throws ClassNotFoundException shouldn't happen.
     */
    public static HashMap<String, Object> load() throws IOException, ClassNotFoundException {
        File[] filesInDir = new File(BlobbyEngine.SAVES_PATH).listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        String lastModifiedFileName = null;

        if(filesInDir != null) {
            for(File file : filesInDir) {
                if(file.lastModified() > lastModifiedTime) {
                    lastModifiedFileName = file.getName();
                    lastModifiedTime = file.lastModified();
                }
            }

            return load(lastModifiedFileName.substring(0, lastModifiedFileName.length() - 4));
        }

        return null;
    }

    /**
     * Loads the latest save file.
     * @param saveFileName the name of the save file.
     * @return the loaded values.
     * @throws IOException if an error occurred while reading the save file.
     * @throws ClassNotFoundException shouldn't happen.
     */
    public static HashMap<String, Object> load(String saveFileName) throws IOException, ClassNotFoundException {
        File file = new File(BlobbyEngine.SAVES_PATH + saveFileName + ".sav");
        if(!file.exists())
            return null;

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
        Object input = inputStream.readObject();
        if(!(input instanceof HashMap))
            return null;

        inputStream.close();
        savedValues.clear();
        savedValues.putAll((HashMap<String, Object>) input);

        ListenerManager.callEvent(new LoadEvent());
        return savedValues;
    }

}
