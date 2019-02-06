package net.wukl.cacofony.http2.settings;

/**
 * An HTTP/2 SETTINGS frame setting.
 */
public class Setting {
    /**
     * The identifier for the setting.
     */
    private final SettingIdentifier identifier;

    /**
     * The value of the setting.
     */
    private final long value;

    /**
     * Creates a new setting.
     *
     * @param id the identifier of the setting
     * @param value the value of the setting
     */
    public Setting(final SettingIdentifier id, final long value) {
        this.identifier = id;
        this.value = value;
    }

    /**
     * Returns the identifier of the setting.
     *
     * @return the identifier
     */
    public SettingIdentifier getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the value of the setting.
     *
     * @return the value
     */
    public long getValue() {
        return this.value;
    }

    /**
     * Translates the setting into the HTTP/2 format.
     *
     * @return the bytes
     *
     * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.5.1">RFC 7540 Section 6.5.1</a>
     */
    public byte[] toBytes() {
        final var bytes = new byte[6];

        bytes[0] = (byte) (this.identifier.getValue() >>> 8);
        bytes[1] = (byte) (this.identifier.getValue());

        bytes[2] = (byte) (this.value >>> 24);
        bytes[3] = (byte) (this.value >>> 16);
        bytes[4] = (byte) (this.value >>>  8);
        bytes[5] = (byte) (this.value);

        return bytes;
    }
}
