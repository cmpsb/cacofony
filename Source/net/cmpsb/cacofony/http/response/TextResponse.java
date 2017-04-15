package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.mime.MimeType;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

/**
 * A response containing text.
 * <p>
 * The default content type is {@code text/plain}.
 * <p>
 * The default character set is UTF-8, but can be changed through the constructors
 * {@link #TextResponse(String, Charset)} and {@link #TextResponse(ResponseCode, String, Charset)},
 * or through {@link #setContent(String, Charset)}.
 * When preparing, the response will set the {@code charset} parameter for its content type, so by
 * default the sent content type will be {@code text/plain; charset=UTF-8}.
 * <p>
 * To make usage easier the response uses a {@link StringBuilder} internally and exposes a subset
 * of its interface. The functionality is thus equivalent.
 * <p>
 * This response also implements {@link CharSequence}, meaning that you can use this response type
 * as a parameter to many string operations. Why you would do this, though, is beyond me.
 *
 * @author Luc Everse
 */
public class TextResponse extends Response implements CharSequence, Appendable {
    /**
     * The content to send.
     */
    private byte[] bytes;

    /**
     * The content of the response.
     */
    private StringBuilder content;

    /**
     * The character set of the response.
     */
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * Creates a new plain text response.
     *
     * @param content the text to send
     * @param charset the charset to encode the text with
     */
    public TextResponse(final String content, final Charset charset) {
        this.content = new StringBuilder(content);
        this.charset = charset;
    }

    /**
     * Creates a new plain text response with a status code.
     *
     * @param status  the status code
     * @param content the text to send
     * @param charset the charset to encode the text with
     */
    public TextResponse(final ResponseCode status, final String content, final Charset charset) {
        super(status);

        this.content = new StringBuilder(content);
        this.charset = charset;
    }

    /**
     * Creates a new plain text response.
     * <p>
     * The default charset is UTF-8.
     *
     * @param content the text to send
     */
    public TextResponse(final String content) {
        this.content = new StringBuilder(content);
    }

    /**
     * Creates a new plain text response with a status code.
     * <p>
     * The default charset is UTF-8.
     *
     * @param status  the status code
     * @param content the text to send
     */
    public TextResponse(final ResponseCode status, final String content) {
        super(status);

        this.content = new StringBuilder(content);
    }

    /**
     * Creates a new plain text response with an empty string as its content.
     * <p>
     * Use {@link #setContent(String)} or {@link #setContent(String, Charset)} to set the content.
     */
    public TextResponse() {
        this.content = new StringBuilder();
    }

    /**
     * Sets the content.
     *
     * @param data    the text to send
     * @param charset the charset to encode the text with
     */
    public void setContent(final String data, final Charset charset) {
        this.content = new StringBuilder(data);
        this.charset = charset;
    }

    /**
     * Sets the content.
     *
     * @param data the text to send
     */
    public void setContent(final String data) {
        this.content = new StringBuilder(data);
    }

    /**
     * Returns the content.
     *
     * @return the content
     */
    public String getContent() {
        return this.content.toString();
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        out.write(this.bytes);
    }

    /**
     * Calculates the length, in bytes, of the content to send.
     * <p>
     * If {@code -1}, then a collection of transfer encodings are applied. This allows for
     * big responses that don't fit entirely within memory.
     *
     * @return the length, in bytes, of the content to send or {@code -1} if it's unknown
     */
    @Override
    public long getContentLength() {
        return this.bytes.length;
    }

    /**
     * Serializes the content as a set of bytes to send.
     * <p>
     * Afterwards, the content field is invalidated and any changes will result in exceptions.
     *
     * @param request the request that triggered this response
     */
    @Override
    public void prepare(final Request request) {
        if (this.getContentType() == null) {
            final MimeType type = MimeType.text();
            type.getParameters().put("charset", this.charset.name());
            this.setContentType(type);
        }


        this.bytes = this.content.toString().getBytes(this.charset);
        this.content = null;
        this.charset = null;

        super.prepare(request);
    }

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit <code>char</code>s in the sequence.
     *
     * @return the number of <code>char</code>s in this sequence
     */
    @Override
    public int length() {
        return this.content.length();
    }

    /**
     * Returns {@code true} if, and only if, {@link #length()} is {@code 0}.
     *
     * @return {@code true} if {@link #length()} is {@code 0}, otherwise
     * {@code false}
     */
    public boolean isEmpty() {
        return this.content.toString().isEmpty();
    }

    /**
     * Returns the <code>char</code> value at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first <code>char</code> value of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing.
     * <p>
     * If the <code>char</code> value specified by the index is a
     * <a href="{@docRoot}/java/lang/Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param index the index of the <code>char</code> value to be returned
     * @return the specified <code>char</code> value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is negative or not less than
     *                                   <tt>length()</tt>
     */
    @Override
    public char charAt(final int index) {
        return this.content.charAt(index);
    }

    /**
     * Returns a <code>CharSequence</code> that is a subsequence of this sequence.
     * The subsequence starts with the <code>char</code> value at the specified index and
     * ends with the <code>char</code> value at index <tt>end - 1</tt>.  The length
     * (in <code>char</code>s) of the
     * returned sequence is <tt>end - start</tt>, so if <tt>start == end</tt>
     * then an empty sequence is returned.
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if <tt>start</tt> or <tt>end</tt> are negative,
     *                                   if <tt>end</tt> is greater than <tt>length()</tt>,
     *                                   or if <tt>start</tt> is greater than <tt>end</tt>
     */
    @Override
    public CharSequence subSequence(final int start, final int end) {
        return this.content.subSequence(start, end);
    }

    /**
     * Returns a stream of {@code int} zero-extending the {@code char} values
     * from this sequence.  Any char which maps to a <a
     * href="{@docRoot}/java/lang/Character.html#unicode">surrogate code
     * point</a> is passed through uninterpreted.
     *
     * <p>If the sequence is mutated while the stream is being read, the
     * result is undefined.
     *
     * @return an IntStream of char values from this sequence
     */
    @Override
    public IntStream chars() {
        return this.content.chars();
    }

    /**
     * Returns a stream of code point values from this sequence.  Any surrogate
     * pairs encountered in the sequence are combined as if by {@linkplain
     * Character#toCodePoint Character.toCodePoint} and the result is passed
     * to the stream. Any other code units, including ordinary BMP characters,
     * unpaired surrogates, and undefined code units, are zero-extended to
     * {@code int} values which are then passed to the stream.
     *
     * <p>If the sequence is mutated while the stream is being read, the result
     * is undefined.
     *
     * @return an IntStream of Unicode code points from this sequence
     * @since 1.8
     */
    @Override
    public IntStream codePoints() {
        return this.content.codePoints();
    }

    /**
     * Returns the content of the response, which also follows the CharSequence contract.
     * <p>
     * Equivalent to {@link #getContent()}.
     *
     * @return  a string consisting of exactly this sequence of characters
     */
    @Override
    public String toString() {
        return this.content.toString();
    }

    /**
     * Appends another sequence to this sequence.
     *
     * @param charSequence the sequence to append
     *
     * @return this response
     */
    @Override
    public TextResponse append(final CharSequence charSequence) {
        this.content.append(charSequence);
        return this;
    }

    /**
     * Appends a subsequence of the given {@code charSequence} to this sequence.
     *
     * @param charSequence the sequence to append
     * @param start        the start index of the subsequence to append
     * @param end          the end index of the subsequence to append
     *
     * @return this response
     */
    @Override
    public TextResponse append(final CharSequence charSequence, final int start, final int end) {
        this.content.append(charSequence, start, end);
        return this;
    }

    /**
     * Appends the character to this sequence.
     *
     * @param character the character to append
     *
     * @return this response
     */
    @Override
    public TextResponse append(final char character) {
        this.content.append(character);
        return this;
    }

    /**
     * Appends the the string representation of the given object to this sequence.
     *
     * @param obj the object to append
     *
     * @return this response
     */
    public TextResponse append(final Object obj) {
        this.content.append(obj);
        return this;
    }
}
