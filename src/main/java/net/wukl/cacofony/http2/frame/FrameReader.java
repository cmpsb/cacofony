package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.Http2ProtocolError;
import net.wukl.cacofony.http2.settings.Setting;
import net.wukl.cacofony.http2.settings.SettingIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * Reads HTTP/2 frames from an input stream.
 */
public class FrameReader {
    private static final Logger logger = LoggerFactory.getLogger(FrameReader.class);

    /**
     * Specialized frame readers for each of the possible frame types.
     */
    private final SpecFrameReader[] frameReaders = new SpecFrameReader[256];

    /**
     * Creates a new frame reader.
     */
    public FrameReader() {
        for (int i = 0; i < (1 << Byte.SIZE); ++i) {
            final var index = i;
            this.frameReaders[i] = (p, in) -> {
                logger.warn("Unrecognized frame type {} ({})", p.getType(), index);
                return p;
            };
        }

        this.frameReaders[FrameType.SETTINGS.getValue()] = this::readSettingsFrame;
        this.frameReaders[FrameType.WINDOW_UPDATE.getValue()] = this::readWindowUpdateFrame;
    }

    /**
     * Reads a frame from an input stream.
     *
     * @param in the input stream
     *
     * @return the frame that has been read
     *
     * @throws IOException if an I/O error occurs
     * @throws net.wukl.cacofony.http2.Http2ConnectionError if the frame is invalid
     */
    public Frame read(final InputStream in) throws IOException {
        final var header = in.readNBytes(9);

        final var length = (header[0] << 16) | (header[1] << 8) | header[2];
        final var rawType = header[3] & 0xFF;
        final var flagsByte = header[4];
        final var streamId = (header[5] << 24) | (header[6] << 16) | (header[7] << 8) | header[8];

        final var type = FrameType.valueOf(rawType);
        final Set<FrameFlag> flags;
        if (type != null) {
            flags = type.getFlagsFromBitString(flagsByte);
        } else {
            flags = Collections.emptySet();
        }

        final var protoFrame = new EmptyFrame(length, type, flags, streamId);
        return this.frameReaders[rawType].read(protoFrame, in);
    }

    /**
     * Reads a SETTINGS frame from the input stream.
     *
     * @param proto the prototype containing the frame header
     * @param in the input stream to read the frame from
     *
     * @return the SETTINGS frame
     *
     * @throws IOException if an I/O error occurs
     *
     * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.5">RFC 7540 Section 6.5</a>
     */
    private Frame readSettingsFrame(final Frame proto, final InputStream in) throws IOException {
        if (proto.getPayloadLength() % SettingsFrame.BYTES_PER_SETTING != 0) {
            throw new Http2FrameSizeError("The payload size is not a multiple of 6 bytes");
        }

        final var isAck = proto.getFlags().contains(FrameFlag.ACK);
        if (isAck && proto.getPayloadLength() != 0) {
            throw new Http2FrameSizeError("Settings acknowledgements may not contain any payload");
        }

        if (proto.getStreamId() != 0) {
            throw new Http2ProtocolError("SETTINGS frames cannot be applied to streams");
        }

        if (isAck) {
            return new SettingsFrame(true);
        }

        final var settings = new ArrayList<Setting>();
        for (int i = 0; i < proto.getPayloadLength();) {
            final var rawSetting = in.readNBytes(6);
            i += 6;

            final var rawId = (rawSetting[0] << 8) | rawSetting[1];
            final var id = SettingIdentifier.valueOf(rawId);
            if (id == null) {
                continue;
            }

            final var value = (long) (rawSetting[2] << 24) | (rawSetting[3] << 16)
                    | (rawSetting[4] << 8) | (rawSetting[5]);

            settings.add(new Setting(id, value));
        }

        return new SettingsFrame(settings);
    }

    /**
     * Reads a WINDOW_UPDATE frame from the input stream.
     *
     * @param proto the prototype containing the frame header
     * @param in the input stream to read the frame from
     *
     * @return the frame
     *
     * @throws IOException if an I/O error occurs
     */
    private Frame readWindowUpdateFrame(final Frame proto, final InputStream in)
            throws IOException {
        if (proto.getPayloadLength() != 4) {
            throw new Http2FrameSizeError("The payload size is not exactly 4 for a WINDOW_UPDATE");
        }

        final var bytes = in.readNBytes(4);
        final long value = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);

        return new WindowUpdateFrame(proto.getStreamId(), value);
    }

    /**
     * A function reading a frame of a specific type from the input stream.
     */
    @FunctionalInterface
    private interface SpecFrameReader {
        /**
         * Reads a frame from the input stream.
         *
         * @param proto the frame prototype containing header information
         * @param in the input stream to read the frame from
         *
         * @return the read frame
         *
         * @throws IOException if an I/O error occurs
         */
        Frame read(Frame proto, InputStream in) throws IOException;
    }
}