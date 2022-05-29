package de.arnomann.martin.blobby.logging;

public class ErrorManagement {

    private ErrorManagement() {}

    public static void showErrorMessage(Logger logger, Throwable e) {
        String errorClassName = e.getClass().getSimpleName();
        boolean startsWithVocal = errorClassName.startsWith("/[aeiou]/i");
        logger.error((startsWithVocal ? "An " : "A ") + errorClassName + " occurred:", e);
    }

}
