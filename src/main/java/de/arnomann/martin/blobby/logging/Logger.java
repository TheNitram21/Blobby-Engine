package de.arnomann.martin.blobby.logging;

import de.arnomann.martin.blobby.core.BlobbyEngine;

import static de.arnomann.martin.blobby.logging.Logger.LoggingType.*;

/**
 * A basic logger.
 */
public class Logger {

    private final Class<?> callerClass;
    private boolean[] loggingTypes = { true, true, true, true, false };

    /**
     * Creates a new logger.
     */
    public Logger() {
        Class<?> callerClassTemp;

        try {
            callerClassTemp = Class.forName(new Exception().getStackTrace()[1].getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            callerClassTemp = null;
        }

        this.callerClass = callerClassTemp;
    }

    public enum LoggingType {
        INFO, WARNING, ERROR, FATAL, DEBUG
    }

    /**
     * Enables a specific type of logger messages.
     * @param loggingType the type of messages to enable.
     */
    public void enable(LoggingType loggingType) {
        loggingTypes[loggingType.ordinal()] = true;
    }

    /**
     * Disables a specific type of logger messages.
     * @param loggingType the type of messages to disable.
     */
    public void disable(LoggingType loggingType) {
        loggingTypes[loggingType.ordinal()] = false;
    }

    /**
     * Prints an info message.
     * @param message the message.
     */
    public void info(String message) {
        if(loggingTypes[INFO.ordinal()]) {
            send(INFO, message);
        }
    }

    /**
     * Prints a warning message.
     * @param message the message.
     */
    public void warn(String message) {
        if(loggingTypes[WARNING.ordinal()]) {
            send(WARNING, message);
        }
    }

    /**
     * Prints an error message.
     * @param message the message.
     */
    public void error(String message) {
        if(loggingTypes[ERROR.ordinal()]) {
            send(ERROR, message);
        }
    }

    /**
     * Prints an error message with an error.
     * @param message the message.
     * @param e the error / exception.
     */
    public void error(String message, Throwable e) {
        if(loggingTypes[ERROR.ordinal()]) {
            send(ERROR, message);
            e.printStackTrace();
        }
    }

    /**
     * Prints a fatal message with an error. This method will stop the engine.
     * @param message the message.
     * @param e the error / exception.
     */
    public void fatal(String message, Throwable e) {
        if(loggingTypes[FATAL.ordinal()]) {
            send(FATAL, message);
            e.printStackTrace();
        }

        BlobbyEngine.stop();
    }

    /**
     * Prints a debug message.
     * @param message the message.
     */
    public void debug(String message) {
        if(loggingTypes[DEBUG.ordinal()]) {
            send(DEBUG, message);
        }
    }

    protected void send(LoggingType loggingType, String msg) {
        if(loggingType.equals(LoggingType.INFO) || loggingType.equals(LoggingType.WARNING) || loggingType.equals(LoggingType.DEBUG)) {
            System.out.printf("[%s %s]: %s%n", callerClass.getSimpleName(), loggingType.toString(), msg);
        } else {
            System.err.printf("[%s %s]: %s%n", callerClass.getSimpleName(), loggingType.toString(), msg);
        }
    }

}
