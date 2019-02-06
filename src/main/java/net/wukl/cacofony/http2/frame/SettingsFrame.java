package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.settings.Setting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An HTTP/2 SETTINGS frame containing configuration parameters for the connection it's sent on.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.5">RFC 7540 Section 6.5</a>
 */
public final class SettingsFrame implements Frame {
    /**
     * A prefabricated, reusable acknowledgement frame.
     *
     * This removes the need to instantiate new frames for simple acknowledgements.
     *
     * New acknowledgement frames can still be created using
     * {@link SettingsFrame#SettingsFrame(boolean)}.
     */
    public static final SettingsFrame ACK = new SettingsFrame(true);

    /**
     * The number of bytes used per serialized setting within the frame's payload.
     */
    private static final int BYTES_PER_SETTING = 6;

    /**
     * The settings contained within the frame.
     */
    private final List<Setting> settings;

    /**
     * Whether the frame is a settings acknowledgement or not.
     *
     * This affects or depends on the {@link FrameFlag#ACK} flag.
     */
    private final boolean acknowledgement;

    /**
     * Creates a new settings frame.
     *
     * It is an error to mark the frame as an acknowledgement frame and to pass a non-empty list
     * of settings.
     *
     * @param settings the settings contained within the frame
     * @param acknowledgement whether the frame is an acknowledgement to a previous frame or not
     *
     * @throws IllegalArgumentException if the frame is an acknowledgement and
     *
     * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.5.3">RFC 7540 Section 6.5.3</a>
     */
    public SettingsFrame(final List<Setting> settings, final boolean acknowledgement) {
        this.settings = settings;
        this.acknowledgement = acknowledgement;

        if (this.acknowledgement && !this.settings.isEmpty()) {
            throw new IllegalArgumentException(
                    "A settings acknowledgement frame must not contain any settings."
            );
        }
    }

    /**
     * Creates a new, non-acknowledgement settings frame.
     *
     * The resulting frame does NOT have the ACK flag set, even if the given set is empty.
     *
     * @param settings the settings contained within the frame
     *
     * @see SettingsFrame#SettingsFrame(List, boolean)
     */
    public SettingsFrame(final List<Setting> settings) {
        this(settings, false);
    }

    /**
     * Creates a new settings frame without any actual settings.
     *
     * @param acknowledgement whether the frame is an acknowledgement to a previous frame or not
     *
     * @see SettingsFrame#SettingsFrame(List, boolean)
     */
    public SettingsFrame(final boolean acknowledgement) {
        this(Collections.emptyList(), acknowledgement);
    }

    @Override
    public int getPayloadLength() {
        return this.settings.size() * BYTES_PER_SETTING;
    }

    @Override
    public FrameType getType() {
        return FrameType.SETTINGS;
    }

    @Override
    public Set<FrameFlag> getFlags() {
        if (this.acknowledgement) {
            return Set.of(FrameFlag.ACK);
        }

        return Collections.emptySet();
    }

    /**
     * Returns the stream identifier of the SETTINGS frame, which must be 0x0 according
     * RFC 7540 Section 6.5 paragraph 7.
     *
     * @return the stream identifier
     */
    @Override
    public int getStreamId() {
        return 0;
    }

    @Override
    public void writePayload(final OutputStream out) throws IOException {
        if (this.settings.isEmpty() || this.acknowledgement) {
            return;
        }

        for (final var setting : this.settings) {
            out.write(setting.toBytes());
        }
    }
}
