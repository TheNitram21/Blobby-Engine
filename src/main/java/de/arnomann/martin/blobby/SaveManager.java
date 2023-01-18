package de.arnomann.martin.blobby;

import de.arnomann.martin.blobby.core.BlobbyEngine;

import java.io.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class SaveManager {

    public static final HashMap<String, Object> savedValues = new HashMap<>();

    private SaveManager() {}

    public static void save() throws IOException {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
        String currentTime = OffsetDateTime.now().format(timeFormatter);
        save("save_" + currentTime);
    }

    public static void save(String saveFileName) throws IOException {
        File file = new File(BlobbyEngine.SAVES_PATH + saveFileName + ".sav");
        if(file.exists())
            file.delete();
        file.createNewFile();

        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(savedValues);

        outputStream.close();
    }

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
        return savedValues;
    }

}
