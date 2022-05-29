package de.arnomann.martin.blobby.logging;

import static de.arnomann.martin.blobby.logging.Logger.LoggingType.*;

public class Logger {

    private final Class<?> callerClass;
    private boolean[] loggingTypes = { true, true, true, true, false };

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

    public void enable(LoggingType loggingType) {
        loggingTypes[loggingType.ordinal()] = true;
    }

    public void disable(LoggingType loggingType) {
        loggingTypes[loggingType.ordinal()] = false;
    }

    public void info(String message) {
        if(loggingTypes[INFO.ordinal()]) {
            send(INFO, message);
        }
    }

    public void warn(String message) {
        if(loggingTypes[WARNING.ordinal()]) {
            send(WARNING, message);
        }
    }

    public void error(String message) {
        if(loggingTypes[ERROR.ordinal()]) {
            send(ERROR, message);
        }
    }

    public void error(String message, Throwable e) {
        if(loggingTypes[ERROR.ordinal()]) {
            send(ERROR, message);
            e.printStackTrace();
        }
    }

    public void fatal(String message, int errorCode) {
        if(loggingTypes[FATAL.ordinal()]) {
            send(FATAL, message);
        }
    }

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
