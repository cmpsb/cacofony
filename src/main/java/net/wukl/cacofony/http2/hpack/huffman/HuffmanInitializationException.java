package net.wukl.cacofony.http2.hpack.huffman;

/**
 * An exception generated if the Huffman pre-processing stage fails.
 */
public class HuffmanInitializationException extends RuntimeException {
    /**
     * Creates a new bad Huffman table exception.
     */
    public HuffmanInitializationException() {
        super();
    }

    /**
     * Creates a new bad Huffman table exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public HuffmanInitializationException(final String message) {
        super(message);
    }

    /**
     * Creates a new bad Huffman table exception.
     *
     * @param cause the exception that caused this exception
     */
    public HuffmanInitializationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new bad Huffman table exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public HuffmanInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
