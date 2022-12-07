package de.arnomann.martin.blobby.logging;

import de.arnomann.martin.blobby.core.BlobbyEngine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static de.arnomann.martin.blobby.logging.Logger.LoggingType.*;

/**
 * A basic logger.
 */
public class Logger {

    private final Class<?> callerClass;
    private boolean[] loggingTypes = { true, true, true, true, false };
    private File outputFile;
    private BufferedWriter outputFileWriter;

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
        INFO,
        WARNING,
        ERROR,
        FATAL,
        DEBUG
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

    public void setOutputFile(File outputFile) {
        if(outputFile.exists())
            outputFile.delete();
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            error("An error occurred whilst trying to create the log file!", e);
        }

        this.outputFile = outputFile;
        try {
            outputFileWriter = new BufferedWriter(new FileWriter(outputFile));
            outputFileWriter.write("# Blobby Engine log file\n");
        } catch (IOException e) {
            error("An error occurred whilst trying to create log file file writer!", e);
        }
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
        String output = String.format("[%s %s] %s: %s%n", callerClass.getSimpleName(), loggingType.toString(),
                DateTimeFormatter.ofPattern("HH:mm:ss").format(OffsetDateTime.now()), msg);
        if(loggingType.equals(LoggingType.INFO) || loggingType.equals(LoggingType.WARNING) || loggingType.equals(LoggingType.DEBUG)) {
            System.out.print(output);
        } else {
            System.err.print(output);
        }

        if(outputFileWriter != null) {
            try {
                outputFileWriter.append(output);
                outputFileWriter.flush();
            } catch (IOException e) {
                System.err.println("An error occurred whilst trying to write to the log file!");
            }
        }
    }

    public void destroy() {
        try {
            outputFileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
