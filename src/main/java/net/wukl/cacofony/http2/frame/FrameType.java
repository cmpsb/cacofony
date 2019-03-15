package net.wukl.cacofony.http2.frame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A HTTP/2 frame type.
 */
public enum FrameType {
    /**
     * A frame containing data to or from the client.
     */
    DATA((byte) 0x0, Map.of(
            FrameFlag.END_STREAM, 0,
            FrameFlag.PADDED, 3
    )),

    /**
     * A frame containing headers to initiate a new stream.
     */
    HEADERS((byte) 0x1, Map.of(
            FrameFlag.END_STREAM, 0,
            FrameFlag.END_HEADERS, 2,
            FrameFlag.PADDED, 3,
            FrameFlag.PRIORITY, 5
    )),

    /**
     * A frame requesting a priority update of an existing stream.
     */
    PRIORITY((byte) 0x2, Map.of()),

    /**
     * A frame indicating that the given stream is shutting down.
     */
    RST_STREAM((byte) 0x3, Map.of()),

    /**
     * A frame containing protocol configuration parameters.
     */
    SETTINGS((byte) 0x4, Map.of(
            FrameFlag.ACK, 0
    )),

    /**
     * A frame indicating that the server intends to initiate a new stream.
     */
    PUSH_PROMISE((byte) 0x5, Map.of(
            FrameFlag.END_HEADERS, 2,
            FrameFlag.PADDED, 3
    )),

    /**
     * A frame used to check for an active connection and to measure latency.
     */
    PING((byte) 0x6, Map.of(
            FrameFlag.ACK, 0
    )),

    /**
     * A frame used to shut down the connnection.
     */
    GOAWAY((byte) 0x7, Map.of()),

    /**
     * A frame adjusting flow control parameters.
     */
    WINDOW_UPDATE((byte) 0x8, Map.of()),

    /**
     * A frame containing additional headers after a previous HEADERS or PUSH_PROMISE frame.
     */
    CONTINUATION((byte) 0x9, Map.of(
            FrameFlag.END_HEADERS, 2
    ));

    /**
     * The mapping between byte values and types.
     */
    private static final FrameType[] BYTE_TO_TYPE_MAP = new FrameType[1 << Byte.SIZE];

    static {
        for (int i = 0; i < (1 << Byte.SIZE); ++i) {
            BYTE_TO_TYPE_MAP[i] = null;
        }

        for (final var value : values()) {
            assert BYTE_TO_TYPE_MAP[value.getValue()] == null : "FrameType value collision";

            BYTE_TO_TYPE_MAP[value.getValue()] = value;
        }
    }

    /**
     * The byte value of the type.
     */
    private final byte value;

    /**
     * The bit positions the type accepts in the flags field.
     */
    private final Map<FrameFlag, Integer> flagPositions;

    /**
     * The flags associated with the given bit position.
     */
    private final Map<Integer, FrameFlag> flagByPosition;

    /**
     * Creates a new frame type.
     *
     * @param value the byte value of the type as carried in a frame header
     * @param flagPositions the bit positions the type accepts in the flags field
     */
    FrameType(final byte value, final Map<FrameFlag, Integer> flagPositions) {
        this.value = value;
        this.flagPositions = flagPositions;

        this.flagByPosition = new HashMap<>();
        for (final var entry : this.flagPositions.entrySet()) {
            this.flagByPosition.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Returns the byte value of the type as carried in the frame header.
     *
     * @return the byte value
     */
    public byte getValue() {
        return this.value;
    }

    /**
     * Returns the entire map of flag positions that are valid for the type.
     *
     * @return the map of flag positions
     */
    public Map<FrameFlag, Integer> getFlagPositions() {
        return this.flagPositions;
    }

    /**
     * Looks up the flag position of the given flag, if it is valid for the type.
     *
     * If the flag is not valid for the type, {@code null} is returned instead.
     *
     * @param flag the flag to look up
     *
     * @return the bit position or {@code null} if the flag is not valid for the type
     */
    public Integer getFlagPosition(final FrameFlag flag) {
        return this.flagPositions.get(flag);
    }

    /**
     * Generates the set of flags based on a serialized byte.
     *
     * Unrecognized flags are ignored.
     *
     * @param bits the byte to extract the flags from
     *
     * @return the flags in the byte
     */
    public Set<FrameFlag> getFlagsFromBitString(final int bits) {
        final var flags = new HashSet<FrameFlag>();

        for (int i = 0; i < 8; ++i) {
            if ((bits & (1 << i)) == 0) {
                continue;
            }

            final var flag = this.flagByPosition.get(i);
            if (flag == null) {
                continue;
            }

            flags.add(flag);
        }

        return flags;
    }

    /**
     * Looks up a frame type by its byte value.
     *
     * If the type is not known, {@code null} is returned instead.
     *
     * @param value the byte value of the frame type to look for
     *
     * @return the frame type or {@code null} if there is no known frame type with that value
     */
    public static FrameType valueOf(final int value) {
        return BYTE_TO_TYPE_MAP[value & 0xFF];
    }
}
