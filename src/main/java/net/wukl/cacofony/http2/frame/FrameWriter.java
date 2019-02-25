package net.wukl.cacofony.http2.frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Writes HTTP/2 frames onto a Java output stream.
 */
public class FrameWriter {
    private static final Logger logger = LoggerFactory.getLogger(FrameWriter.class);

    /**
     * A simple buffer full of zeros to write as padding.
     */
    private static final byte[] NULL_BYTES = new byte[256];

    static {
        for (int i = 0; i < NULL_BYTES.length; ++i) {
            NULL_BYTES[i] = 0;
        }
    }

    /**
     * Specialized frame writers.
     */
    private final SpecFrameWriter[] frameWriters = new SpecFrameWriter[256];

    /**
     * The random number generator used to generate padding.
     */
    private final SecureRandom random;

    /**
     * Creates a new frame writer.
     *
     * @param random the random number generator used to generate padding
     */
    public FrameWriter(final SecureRandom random) {
        this.random = random;

        for (int i = 0; i < 256; ++i) {
            final var index = i;
            this.frameWriters[i] = (f, o) -> {
                logger.warn("Unrecognized frame type {} ({})", f.getType(), index);
            };
        }

        this.addWriter(FrameType.SETTINGS, this::writeSettings);
        this.addWriter(FrameType.WINDOW_UPDATE, this::writeWindowUpdate);
        this.addWriter(FrameType.PRIORITY, this::writePriority);
        this.addWriter(FrameType.HEADERS, this::writeHeaders);
        this.addWriter(FrameType.DATA, this::writeData);
    }

    /**
     * Installs a specialized frame writer in the writer table.
     *
     * @param type the frame type the writer is specialized for
     * @param writer the writer
     */
    private void addWriter(final FrameType type, final SpecFrameWriter writer) {
        this.frameWriters[type.getValue()] = writer;
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
        final var flags = new HashSet<>(frame.getFlags());

        final var padded = frame.getType().getFlagPositions().containsKey(FrameFlag.PADDED);
        final int numPadBytes;
        if (padded) {
            flags.add(FrameFlag.PADDED);
            numPadBytes = this.random.nextInt(255) + 1;
        } else {
            numPadBytes = 0;
        }

        final var length = frame.getPayloadLength() + numPadBytes;
        final var id = frame.getStreamId();

        final var typeValue = frame.getType().getValue();

        final var buf = new byte[] {
                (byte) ((length >>> 16) & 0xFF),
                (byte) ((length >>>  8) & 0xFF),
                (byte) (length & 0xFF),
                typeValue,
                this.flagsToByte(frame.getType(), flags),
                (byte) ((id >>> 24) & 0xFF),
                (byte) ((id >>> 16) & 0xFF),
                (byte) ((id >>>  8) & 0xFF),
                (byte) (id & 0xFF)
        };

        out.write(buf);

        if (padded) {
            out.write(numPadBytes & 0xFF);
        }

        if (!(frame instanceof EmptyFrame) && length > 0) {
            this.frameWriters[typeValue].write(frame, out);
        }

        if (padded) {
            out.write(NULL_BYTES, 0, numPadBytes - 1);
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
        this.writeUnsignedInt(increment, out);
    }

    /**
     * Writes a PRIORITY frame to the output stream.
     *
     * @param frame the PRIORITY frame
     * @param out the output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writePriority(final Frame frame, final OutputStream out) throws IOException {
        assert frame instanceof PriorityFrame : "Non-PRIORITY frame passed to writePriority";

        final var priorityFrame = (PriorityFrame) frame;
        final long exclusiveMask;
        if (priorityFrame.isExclusive()) {
            exclusiveMask = 1 << 31;
        } else {
            exclusiveMask = 0;
        }

        final var dependencyId = priorityFrame.getDependencyId();
        final var weight = priorityFrame.getWeight();

        this.writeUnsignedInt(dependencyId | exclusiveMask, out);
        out.write(weight & 0xFF);
    }

    /**
     * Writes a HEADERS frame to the output stream.
     *
     * @param frame the HEADERS frame
     * @param out the output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeHeaders(final Frame frame, final OutputStream out) throws IOException {
        assert frame instanceof HeadersFrame : "Non-HEADERS frame passed to writeHeaders";

        final var headersFrame = (HeadersFrame) frame;
        final var priorityFrame = headersFrame.getPriorityFrame();
        if (priorityFrame != null) {
            this.writePriority(priorityFrame, out);
        }

        out.write(headersFrame.getHeaderBlock());
    }

    /**
     * Writes a DATA frame to the output stream.
     *
     * @param frame the DATA frame
     * @param out the output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeData(final Frame frame, final OutputStream out) throws IOException {
        assert frame instanceof DataFrame : "Non-DATA frame passed to writeData";
        out.write(((DataFrame) frame).getBytes());
    }

    /**
     * Writes a CONTINUATION frame to the output stream.
     *
     * @param frame the CONTINUATION frame
     * @param out the output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeContinuation(final Frame frame, final OutputStream out) throws IOException {
        assert frame instanceof ContinuationFrame
                : "Non-CONTINUATION frame passed to writeContinuation";
        out.write(((ContinuationFrame) frame).getBytes());
    }

    /**
     * Writes a GOAWAY frame to the output stream.
     *
     * @param frame the GOAWAY frame
     * @param out tho output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeGoAway(final Frame frame, final OutputStream out) throws IOException {
        assert frame instanceof GoAwayFrame : "Non-GOAWAY frame passed to writeGoAway";
        final var goAway = (GoAwayFrame) frame;

        this.writeUnsignedInt(goAway.getLastStreamId(), out);
        this.writeUnsignedInt(goAway.getErrorCode().getCode(), out);
        out.write(goAway.getDebugData());
    }

    /**
     * Writes an unsigned 32-bit integer to the output stream.
     *
     * @param value the integer to write
     * @param out the stream to write the number to
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeUnsignedInt(final long value, final OutputStream out) throws IOException {
        out.write((int) (value >>> 24) & 0xFF);
        out.write((int) (value >>> 16) & 0xFF);
        out.write((int) (value >>> 8) & 0xFF);
        out.write((int) value & 0xFF);
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
