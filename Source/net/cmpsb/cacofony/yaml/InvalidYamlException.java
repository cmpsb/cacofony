package net.cmpsb.cacofony.yaml;

/**
 * @author Luc Everse
 */
public class InvalidYamlException extends RuntimeException {

    /**
     * Creates a new invalid yaml exception.
     */
    public InvalidYamlException() {
    }

    /**
     * Creates a new invalid yaml exception.
     *
     * @param message the detail message about the error
     */
    public InvalidYamlException(final String message) {
        super(message);
    }

    /**
     * Creates a new invalid yaml exception.
     *
     * @param message the detail message about the error
     * @param cause   the exception that caused this exception
     */
    public InvalidYamlException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new invalid yaml exception.
     *
     * @param cause the exception that caused this exception
     */
    public InvalidYamlException(final Throwable cause) {
        super(cause);
    }
}
