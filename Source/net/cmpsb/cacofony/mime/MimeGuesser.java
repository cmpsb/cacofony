package net.cmpsb.cacofony.mime;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
     * <p>
     * If you're inspecting files received from a third party, <em>do not use this function.</em>
     * Use {@link #guessRemote(Path)} instead.
     *
     * @param file the file to guess the type for
     *
     * @return the file's MIME type
     */
    public MimeType guessLocal(final Path file) {
        final String name = file.toString();

        final MimeType dbType = this.mimeDb.getForName(name);
        if (dbType != null) {
            return dbType;
        }

        final MimeType magicType = this.guessRemote(file);

        if (magicType != null) {
            return magicType;
        }

        return MimeType.octetStream();
    }

    /**
     * Guesses the MIME type for a local resource by looking at the file extension and, if it
     * doesn't match, the file header.
     *
     * @param jar  the jar the resource should be in
     * @param path the path to the resource
     *
     * @return the file's MIME type
     */
    public MimeType guessLocal(final Class<?> jar, final String path) {
        final MimeType dbType = this.mimeDb.getForName(path);
        if (dbType != null) {
            return dbType;
        }

        try (InputStream in = jar.getResourceAsStream(path)) {
            final MimeType magicType = this.guessRemote(in);
            if (magicType != null) {
                return magicType;
            }
        } catch (final IOException ex) {
            logger.warn("Unable to inspect the resource {}#{}: ", jar.getCanonicalName(), path, ex);
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
    public MimeType guessRemote(final Path file) {
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

    /**
     * Uses simplemagic to guess the stream's content type.
     * <p>
     * Note: this may read at random points and "damage" the stream pointer.
     *
     * @param in the stream to examine
     *
     * @return the MIME type or {@code null}
     */
    public MimeType guessRemote(final InputStream in) {
        try {
            final ContentInfo info = this.mimeInfo.findMatch(in);

            if (info == null) {
                return null;
            }

            return this.parser.parse(info.getMimeType());
        } catch (final IOException ex) {
            logger.warn("I/O exception while inspecting the resource header: ", ex);
            return null;
        }
    }
}
