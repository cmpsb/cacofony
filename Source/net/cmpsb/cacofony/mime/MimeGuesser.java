package net.cmpsb.cacofony.mime;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A class that can guess the MIME type of the file it's given.
 *
 * @author Luc Everse
 */
public class MimeGuesser {
    private static final Logger logger = LoggerFactory.getLogger(MimeGuesser.class);

    /**
     * The MIME parser to use.
     */
    private final MimeParser parser;

    /**
     * The MIME DB to use.
     */
    private final MimeDb mimeDb;

    /**
     * The MIME type resolver to use.
     */
    private final ContentInfoUtil mimeInfo;

    /**
     * Creates a new MIME guesser.
     *
     * @param parser   the MIME parser to use
     * @param mimeDb   the extension-to-MIME-type database
     * @param mimeInfo the simplemagic instance to use
     */
    public MimeGuesser(final MimeParser parser,
                       final MimeDb mimeDb,
                       final ContentInfoUtil mimeInfo) {
        this.parser = parser;
        this.mimeDb = mimeDb;
        this.mimeInfo = mimeInfo;
    }

    /**
     * Guesses the MIME type for a file by looking at the file extension and, if it doesn't match,
     * the file header.
     *
     * @param file the file to guess the type for
     *
     * @return the file's MIME type
     */
    public MimeType guess(final Path file) {
        final String name = file.toString();

        final MimeType dbType = this.mimeDb.getForName(name);
        if (dbType != null) {
            return dbType;
        }

        final MimeType magicType = this.inspectHeader(file);

        if (magicType != null) {
            return magicType;
        }

        return MimeType.octetStream();
    }

    /**
     * Uses simplemagic to guess the file's content type.
     *
     * @param file the file to inspect
     *
     * @return the MIME type or {@code null}
     */
    public MimeType inspectHeader(final Path file) {
        try {
            final ContentInfo info = this.mimeInfo.findMatch(file.toFile());

            if (info == null) {
                return null;
            }

            return this.parser.parse(info.getMimeType());

        } catch (final IOException ex) {
            logger.warn("I/O exception while inspecting the file header: ", ex);
            return null;
        }
    }
}
