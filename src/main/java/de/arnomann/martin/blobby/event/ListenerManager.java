package de.arnomann.martin.blobby.event;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all event listeners.
 */
public class ListenerManager {

    private static List<EventListener> listeners = new ArrayList<>();

    private ListenerManager() {}

    /**
     * Calls an event.
     * @param event the event.
     */
    public static void callEvent(Event event) {
//        List<EventListener> listenersCopy = new ArrayList<>(listeners);
        EventListener[] listenersCopy = listeners.toArray(new EventListener[0]);

        for(EventListener listener : listenersCopy) {
            try {
                String eventMethod = "on" + event.getClass().getSimpleName().replace("Event", "");

                listener.getClass().getMethod(eventMethod, event.getClass()).invoke(listener, event);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        }
    }

    /**
     * Registers a new event listener.
     * @param listener the new listener.
     */
    public static void registerEventListener(EventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes an event listener from the list of listeners.
     * @param listener the listener to remove.
     */
    public static void removeEventListener(EventListener listener) {
        listeners.remove(listener);
    }

}
