package de.arnomann.martin.blobby.logging;

/**
 * Manages errors.
 */
public class ErrorManagement {

    private ErrorManagement() {}

    /**
     * Shows an error message.
     * @param logger the logger.
     * @param e the error / exception.
     */
    public static void showErrorMessage(Logger logger, Throwable e) {
        String errorClassName = e.getClass().getSimpleName();
        boolean startsWithVocal = errorClassName.startsWith("/[aeiou]/i");
        logger.error((startsWithVocal ? "An " : "A ") + errorClassName + " occurred:", e);
    }

}
