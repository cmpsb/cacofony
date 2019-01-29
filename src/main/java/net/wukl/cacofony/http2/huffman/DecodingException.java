package net.wukl.cacofony.http2.huffman;

/**
 * An exception signaling that a Huffman-encoded string cannot be decoded due to an error in the
 * bit string.
 */
public class DecodingException extends RuntimeException {
    /**
     * Creates a new decoding exception.
     */
    public DecodingException() {
        super();
    }

    /**
     * Creates a new decoding exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public DecodingException(final String message) {
        super(message);
    }

    /**
     * Creates a new decoding exception.
     *
     * @param cause the exception that caused this exception
     */
    public DecodingException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new decoding exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public DecodingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
