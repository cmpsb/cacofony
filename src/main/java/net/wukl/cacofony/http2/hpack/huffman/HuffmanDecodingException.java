package net.wukl.cacofony.http2.hpack.huffman;

/**
 * An exception signaling that a Huffman-encoded string cannot be decoded due to an error in the
 * bit string.
 */
public class HuffmanDecodingException extends RuntimeException {
    /**
     * Creates a new decoding exception.
     */
    public HuffmanDecodingException() {
        super();
    }

    /**
     * Creates a new decoding exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public HuffmanDecodingException(final String message) {
        super(message);
    }

    /**
     * Creates a new decoding exception.
     *
     * @param cause the exception that caused this exception
     */
    public HuffmanDecodingException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new decoding exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public HuffmanDecodingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
