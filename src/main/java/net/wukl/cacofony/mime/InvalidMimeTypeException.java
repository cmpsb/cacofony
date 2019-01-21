package net.wukl.cacofony.mime;

import net.wukl.cacofony.http.exception.BadRequestException;

/**
 * An exception sigaling a malformed MIME type.
 *
 * @author Luc Everse
 */
public class InvalidMimeTypeException extends BadRequestException {
    /**
     * Creates a new exception.
     */
    public InvalidMimeTypeException() {
        super();
    }

    /**
     * Create a new exception.
     *
     * @param message the detail message
     */
    public InvalidMimeTypeException(final String message) {
        super(message);
    }

    /**
     * Create a new exception.
     *
     * @param cause the exception that caused this exception
     */
    public InvalidMimeTypeException(final Throwable cause) {
        super(cause);
    }

    /**
     * Create a new exception.
     *
     * @param message the detail message
     * @param cause   the exception that caused this exception
     */
    public InvalidMimeTypeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
