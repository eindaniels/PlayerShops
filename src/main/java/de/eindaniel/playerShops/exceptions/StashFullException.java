package de.eindaniel.playerShops.exceptions;

public class StashFullException extends Exception {
    public StashFullException(String message) {
        super(message);
    }

    public StashFullException(String message, Throwable cause) {
        super(message, cause);
    }
}
