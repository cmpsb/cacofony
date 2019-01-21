package net.wukl.cacofony.mime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A mime type.
 *
 * @author Luc Everse
 */
public class MimeType implements Comparable<MimeType> {
    /**
     * Returns a MIME type denoting a format-less octet stream.
     *
     * @return a MIME type denoting a format-less octet stream
     */
    public static MimeType octetStream() {
        return new MimeType("application", "octet-stream");
    }

    /**
     * Returns a MIME type denoting plain text.
     *
     * @return a MIME type denoting plain text
     */
    public static MimeType text() {
        return new MimeType("text", "plain");
    }

    /**
     * Returns a MIME type denoting HTML.
     *
     * @return a MIME type denoting HTML
     */
    public static MimeType html() {
        return new MimeType("text", "html");
    }

    /**
     * Returns a MIME type matching any other MIME type.
     *
     * @return a MIME type matching any other MIME type
     */
    public static MimeType any() {
        return new MimeType("*", "*");
    }

    /**
     * Returns the MIME type multipart/byteranges.
     *
     * @return the MIME type multipart/byteranges
     */
    public static MimeType byteranges() {
        return new MimeType("multipart", "byteranges");
    }

    /**
     * The head type.
     */
    private final String mainType;

    /**
     * The subtype.
     */
    private final String subType;

    /**
     * Any parameters.
     */
    private final Map<String, String> parameters;

    /**
     * The accept type's quality rank.
     * Lower is better.
     */
    private double rank = 0;

    /**
     * Creates a new MIME type with explicit values.
     *
     * @param mainType the main type
     * @param subType  the subtype
     */
    public MimeType(final String mainType, final String subType) {
        this.mainType = mainType;
        this.subType = subType;
        this.parameters = new HashMap<>();
    }

    /**
     * Creates a new MIME type as a copy of another.
     * <p>
     * Everything, including rank and parameters, is copied.
     *
     * @param original the type to copy
     */
    public MimeType(final MimeType original) {
        this.mainType = original.mainType;
        this.subType = original.subType;
        this.parameters = new HashMap<>(original.parameters);
        this.rank = original.rank;
    }

    /**
     * @return the type's main type
     */
    public String getMainType() {
        return this.mainType;
    }

    /**
     * @return the type's subtype
     */
    public String getSubType() {
        return this.subType;
    }

    /**
     * @return the type's parameters, if any
     */
    public Map<String, String> getParameters() {
        return this.parameters;
    }

    /**
     * Builds the type's simple form, following the pattern [type]/[subtype].
     * <p>
     * The subtype includes any tree and suffix, but no parameters.
     * To get the full version, see {@link #toString()}.
     *
     * @return the type's simple form
     */
    public String getSimpleForm() {
        return this.mainType + "/" + this.subType;
    }

    /**
     * Recalculates the accept type's rank.
     */
    public void recalculateRank() {
        final String quality = this.getParameters().get("q");

        if (quality == null) {
            this.rank = 0;
        } else {
            this.rank = 1 - Double.parseDouble(quality);
        }
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     */
    @Override
    public int compareTo(final MimeType o) {
        return (int) (this.rank - o.rank);
    }

    /**
     * Checks whether two mime types can be considered equal.
     * <p>
     * This performs a wildcard-aware check, so the other object does not have to be unique.
     *
     * @param other the other object to check
     *
     * @return true if the mime types are equal, false otherwise
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof MimeType)) {
            return false;
        }

        final MimeType otherMimetype = (MimeType) other;

        return this.typesEqual(this.mainType, otherMimetype.mainType)
            && this.typesEqual(this.subType,  otherMimetype.subType);
    }

    /**
     * Calculates the type's hash code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.mainType, this.subType);
    }

    /**
     * Checks whether both types match.
     * <p>
     * A match can also be made if one or both are the wildcard type, *.
     *
     * @param etaoin the one type
     * @param shrdlu the other
     *
     * @return true if they can be considered equal, false otherwise
     */
    private boolean typesEqual(final String etaoin, final String shrdlu) {
        return etaoin.equals("*") || shrdlu.equals("*") || Objects.equals(etaoin, shrdlu);
    }

    /**
     * Serializes the type as a string.
     * @return the type as a string
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append(this.mainType);
        builder.append('/');
        builder.append(this.subType);

        for (final String parameterName : this.parameters.keySet()) {
            final String parameterValue = this.parameters.get(parameterName);

            builder.append("; ");
            builder.append(parameterName);
            builder.append("=");
            builder.append(parameterValue);
        }

        return builder.toString();
    }
}
