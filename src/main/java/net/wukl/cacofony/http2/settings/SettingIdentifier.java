package net.wukl.cacofony.http2.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * The various setting identifiers.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">RFC 7540 Section 6.5.2</a>
 */
public enum SettingIdentifier {
    /**
     * The maximum table size the decoder of the peer is willing to accept.
     */
    HEADER_TABLE_SIZE(0x1),

    /**
     * Whether the client accepts server pushes.
     */
    ENABLE_PUSH(0x2),

    /**
     * The maximum number of concurrent streams the sender will allow.
     */
    MAX_CONCURRENT_STREAMS(0x3),

    /**
     * The initial window size in octets for stream-level control flow.
     */
    INITIAL_WINDOW_SIZE(0x4),

    /**
     * The largest frame payload the sender is willing to receive, in octets.
     */
    MAX_FRAME_SIZE(0x5),

    /**
     * An advisory indicating the maximum size of the header list the sender is prepared to accept.
     */
    MAX_HEADER_LIST_SIZE(0x6);

    /**
     * The 16-bit value corresponding the identifier.
     */
    private final int value;

    /**
     * A mapping between the numeric values and the actual identifiers.
     */
    private static final Map<Integer, SettingIdentifier> INT_TO_ID_MAP = new HashMap<>();

    static {
        for (final var value : values()) {
            assert !INT_TO_ID_MAP.containsKey(value.getValue())
                    : "Duplicate setting identifier value";

            INT_TO_ID_MAP.put(value.getValue(), value);
        }
    }

    /**
     * Looks up an identifier by its 16-bit value.
     *
     * If there is no known identifier for the given value, {@code null} is returned instead.
     *
     * @param value the value
     *
     * @return the identifier or {@code null} if the value is not recognized
     */
    public static SettingIdentifier valueOf(final int value) {
        return INT_TO_ID_MAP.get(value);
    }

    /**
     * Creates a new setting identifier.
     *
     * @param value the 16-bit value corresponding to the identifier
     */
    SettingIdentifier(final int value) {
        this.value = value;
    }

    /**
     * Returns the 16-bit value corresponding to the identifier.
     *
     * @return the value
     */
    public int getValue() {
        return this.value;
    }
}
