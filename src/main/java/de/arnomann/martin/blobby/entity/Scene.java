package de.arnomann.martin.blobby.entity;

import de.arnomann.martin.blobby.core.BlobbyEngine;
import de.arnomann.martin.blobby.logging.ErrorManagement;
import org.joml.Vector2d;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class Scene extends Entity {

    private final Vector2d position;

    private BaseNPC npc;
    private String sceneFileContent;

    private double startTime;
    private boolean sceneRunning = false;

    public Scene(Vector2d position, Map<String, String> parameters) { // NPCName, SceneFile
        super(position, parameters);

        this.position = position;
        this.sceneFileContent = BlobbyEngine.readFile(new File(BlobbyEngine.SCRIPTS_PATH + "npcs/" +
                parameters.get("SceneFile") + ".txt"));
    }

    public void start() {
        sceneRunning = true;

        new Thread(() -> {
            while(sceneRunning) {
                try {
                    String[] sceneLine = sceneFileContent.split("\n", 2);
                    sceneFileContent = sceneLine[1];

                    String method = sceneLine[0].split("\\(", 2)[0];
                    String sceneLineWithoutMethod = sceneLine[0].split("\\(", 2)[1];
                    sceneLineWithoutMethod = sceneLineWithoutMethod.substring(0, sceneLineWithoutMethod.length() - 1);
                    String[] parameters = sceneLineWithoutMethod.split(", ");
                    Class<?>[] parameterClasses = new Class<?>[parameters.length];
                    for(int i = 0; i < parameterClasses.length; i++)
                        parameterClasses[i] = String.class;

                    try {
                        Class<? extends BaseNPC> npcClass = npc.getClass();
                        Method methodToCall = npcClass.getMethod(method, parameterClasses);
                        methodToCall.invoke(npc, parameters);
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } catch(ArrayIndexOutOfBoundsException e) {
                    sceneRunning = false;
                }
            }
        }, "NPC Thread").start();
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void initialize() {
        Entity npc = BlobbyEngine.getCurrentLevel().findEntityByParameter("Name", getParameters().get("NPCName"));
        if(npc == null)
            ErrorManagement.showErrorMessage(BlobbyEngine.getLogger(), new IllegalStateException(
                    "Can't find entity with 'Name' property of '" + getParameters().get("NPCName") + "'"));
        else if(npc instanceof BaseNPC)
            this.npc = (BaseNPC) npc;
        else
            ErrorManagement.showErrorMessage(BlobbyEngine.getLogger(), new IllegalStateException("Entity " +
                    getParameters().get("NPCName") + "is not of any type extending BaseNPC!"));
    }

}
