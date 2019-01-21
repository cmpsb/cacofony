package net.wukl.cacofony.http.response.file;

import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.http.response.ResponseCode;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Indicates that a response is cachable.
 *
 * @author Luc Everse
 */
public abstract class CachableResponse extends Response {
    /**
     * The date the file was last modified.
     */
    private final long lastModified;

    /**
     * The file's ETag.
     */
    private final String etag;

    /**
     * The response's maximum age. Defaults to one day.
     */
    private long maxAge = 86400;

    /**
     * Creates a new cachable response.
     *
     * @param code         the HTTP status code
     * @param lastModified the datetime the response was last modified
     */
    public CachableResponse(final ResponseCode code, final long lastModified) {
        super(code);
        this.lastModified = lastModified;
        this.etag = this.generateEtag();
    }

    /**
     * Creates a new cachable response.
     *
     * @param lastModified the datetime the response was last modified
     */
    public CachableResponse(final long lastModified) {
        this.lastModified = lastModified;
        this.etag = this.generateEtag();
    }

    /**
     * Returns the number of milliseconds since the UNIX epoch this file was last modified.
     *
     * @return the modification datetime
     */
    public long getLastModified() {
        return this.lastModified;
    }

    /**
     * Returns the ETag representing the file.
     *
     * @return the ETag
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Sets the maximum age in seconds of the response.
     * <p>
     * Set the age to {@code 0} to discourage any caching.
     *
     * @param maxAge the max age in seconds
     */
    public void setMaxAge(final long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Generates an ETag based on the file's modification date.
     *
     * @return an ETag
     */
    private String generateEtag() {
        // It's too large as a decimal value, so binary I guess?
        final long basis = 0b1100101111110010100111001110010010000100001000100010001100100101L;
        final long prime = 1099511628211L;

        long hash = basis;
        for (int i = 0; i < Long.BYTES; ++i) {
            final long b = (this.lastModified >> (i * 8)) & 0xFF;
            hash ^= b;
            hash *= prime;
        }

        return '"' + Long.toString(Math.abs(hash), 32) + '"';
    }

    /**
     * Prepare the response for transport.
     *
     * @param request the request that triggered this response
     */
    @Override
    public void prepare(final Request request) {
        // Calculate the ETag based on the modification date.
        if (this.getLastModified() != Long.MAX_VALUE) {
            this.setHeader("ETag", this.getEtag());
        }

        // Add caching headers to the response if the resource is allowed to expire.
        // Otherwise explicitly disallow caching.
        if (this.maxAge > 0) {
            final ZonedDateTime expires = ZonedDateTime.ofInstant(
                    Instant.now().plusSeconds(this.maxAge),
                    ZoneOffset.UTC
            );
            this.setHeader("Cache-Control", "max-age=" + this.maxAge);
            this.setHeader("Expires", DateTimeFormatter.RFC_1123_DATE_TIME.format(expires));
        } else {
            this.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        }

        super.prepare(request);
    }
}
