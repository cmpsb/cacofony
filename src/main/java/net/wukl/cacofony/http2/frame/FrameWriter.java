package net.wukl.cacofony.http2.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * Writes HTTP/2 frames onto a Java output stream.
 */
public class FrameWriter {
    /**
     * Writes the frame out onto the output stream.
     *
     * @param frame the frame to write
     * @param out the outputstream to write the frame to
     *
     * @throws IOException if an I/O error occurs
     */
    public void write(final Frame frame, final OutputStream out) throws IOException {
        final var length = frame.getPayloadLength();
        final var id = frame.getStreamId();

        final var buf = new byte[] {
                (byte) ((length >>> 16) & 0xFF),
                (byte) ((length >>>  8) & 0xFF),
                (byte) ((length >>>  0) & 0xFF),
                frame.getType().getValue(),
                this.flagsToByte(frame.getType(), frame.getFlags()),
                (byte) ((id >>> 24) & 0xFF),
                (byte) ((id >>> 16) & 0xFF),
                (byte) ((id >>>  8) & 0xFF),
                (byte) ((id >>>  0) & 0xFF)
        };

        out.write(buf);
        frame.writePayload(out);
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
}
