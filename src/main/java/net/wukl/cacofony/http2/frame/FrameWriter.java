package net.wukl.cacofony.http2.frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * Writes HTTP/2 frames onto a Java output stream.
 */
public class FrameWriter {
    private static final Logger logger = LoggerFactory.getLogger(FrameWriter.class);

    /**
     * Specialized frame writers.
     */
    private final SpecFrameWriter[] frameWriters = new SpecFrameWriter[256];

    /**
     * Creates a new frame writer.
     */
    public FrameWriter() {
        for (int i = 0; i < 256; ++i) {
            final var index = i;
            this.frameWriters[i] = (f, o) -> {
                logger.warn("Unrecognized frame type {} ({})", f.getType(), index);
            };
        }

        this.frameWriters[FrameType.SETTINGS.getValue()] = this::writeSettings;
        this.frameWriters[FrameType.WINDOW_UPDATE.getValue()] = this::writeWindowUpdate;
    }

    /**
     * Writes the frame out onto the output stream.
     *
     * @param frame the frame to write
     * @param out the output stream to write the frame to
     *
     * @throws IOException if an I/O error occurs
     */
    public void write(final Frame frame, final OutputStream out) throws IOException {
        final var length = frame.getPayloadLength();
        final var id = frame.getStreamId();

        final var typeValue = frame.getType().getValue();

        final var buf = new byte[] {
                (byte) ((length >>> 16) & 0xFF),
                (byte) ((length >>>  8) & 0xFF),
                (byte) (length & 0xFF),
                typeValue,
                this.flagsToByte(frame.getType(), frame.getFlags()),
                (byte) ((id >>> 24) & 0xFF),
                (byte) ((id >>> 16) & 0xFF),
                (byte) ((id >>>  8) & 0xFF),
                (byte) (id & 0xFF)
        };

        out.write(buf);
        if (!(frame instanceof EmptyFrame) && length > 0) {
            this.frameWriters[typeValue].write(frame, out);
        }
    }

    /**
     * Translates the set of flags into a bit-string.
     *
     * @param type the type of the frame the flags belong to
     * @param flags the flags of the frame
     *
     * @return the bit string representing the flags for that frame
     */
    private byte flagsToByte(final FrameType type, final Set<FrameFlag> flags) {
        byte acc = 0;
        for (final var flag : flags) {
            acc |= 1 << flag.getPosition(type);
        }
        return acc;
    }

    /**
     * Writes a settings frame to the output stream.
     *
     * @param frame the settings frame
     * @param out the output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeSettings(final Frame frame, final OutputStream out) throws IOException {
        assert frame instanceof SettingsFrame : "Non-settings frame passed to writeSettings";

        for (final var setting : ((SettingsFrame) frame).getSettings()) {
            out.write(setting.toBytes());
        }
    }

    /**
     * Writes a WINDOW_UPDATE frame to the output stream.
     *
     * @param frame the WINDOW_UPDATE frame
     * @param out the output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeWindowUpdate(final Frame frame, final OutputStream out) throws IOException {
        assert frame instanceof WindowUpdateFrame
                : "Non-WINDOW_UPDATE frame passed to writeWindowUpdate";

        final var increment = ((WindowUpdateFrame) frame).getIncrement();
        out.write((int) (increment >>> 24) & 0xFF);
        out.write((int) (increment >>> 16) & 0xFF);
        out.write((int) (increment >>> 8) & 0xFF);
        out.write((int) (increment & 0xFF));
    }

    /**
     * A payload writer for a specific frame type.
     */
    @FunctionalInterface
    private interface SpecFrameWriter {
        /**
         * Writes the payload of the frame to the output stream.
         *
         * @param frame the frame of which the payload should be written
         * @param out the target output stream
         *
         * @throws IOException if an I/O error occurs
         */
        void write(Frame frame, OutputStream out) throws IOException;
    }
}
