package net.cmpsb.cacofony.http.cookie;

import net.cmpsb.cacofony.util.Ob;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A HTTP cookie.
 *
 * @author Luc Everse
 */
public final class Cookie {
    /**
     * The cookie's name.
     */
    private final String name;

    /**
     * The cookie's value.
     */
    private final String value;

    /**
     * Any attributes.
     */
    private final Map<String, String> attributes = new HashMap<>();

    /**
     * Creates a new cookie.
     *
     * @param name  the name of the cookie
     * @param value the value of the cookie
     */
    public Cookie(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the cookie's name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the cookie's value.
     *
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the cookie's attributes.
     *
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    /**
     * Sets the expiry date.
     * <p>
     * Note: this will only set the Expires attribute. Use {@link #setMaxAge(long)} to set both
     * Expires and Max-Age.
     *
     * @param datetime the datetime the cookie should expire
     */
    public void setExpires(final ZonedDateTime datetime) {
        this.attributes.put("Expires", DateTimeFormatter.RFC_1123_DATE_TIME.format(datetime));
    }

    /**
     * Sets the cookie's maximum age and expiry date.
     *
     * @param maxAge the cookie's maximum age in seconds
     */
    public void setMaxAge(final long maxAge) {
        this.attributes.put("Max-Age", String.valueOf(maxAge));
        this.setExpires(ZonedDateTime.now().plusSeconds(maxAge));
    }

    /**
     * Sets the cookie domain.
     *
     * @param domain the domain
     */
    public void setDomain(final String domain) {
        this.attributes.put("Domain", domain);
    }

    /**
     * Sets the cookie path.
     *
     * @param path the path
     */
    public void setPath(final String path) {
        this.attributes.put("Path", path);
    }

    /**
     * Sets the Secure attribute.
     */
    public void setSecure() {
        this.attributes.put("Secure", null);
    }

    /**
     * Sets the HttpOnly attribute.
     */
    public void setHttpOnly() {
        this.attributes.put("HttpOnly", null);
    }

    /**
     * Checks whether this object is equal to another.
     *
     * @param o the other object
     *
     * @return {@code true} if the objects are equal, otherwise {@code false}
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Cookie)) {
            return false;
        }

        final Cookie other = (Cookie) o;

        return Ob.multiEquals(
            this.name, other.name,
            this.value, other.value,
            this.attributes, other.attributes
        );
    }

    /**
     * Calculates the object's hash code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value, this.attributes);
    }
}
