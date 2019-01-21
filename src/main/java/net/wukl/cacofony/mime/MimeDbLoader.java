package net.wukl.cacofony.mime;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * @author Luc Everse
 */
public class MimeDbLoader {
    /**
     * The MIME parser to use.
     */
    private final MimeParser parser;

    /**
     * Creates a loader for Apache-style MIME type databases.
     *
     * @param parser the MIME parser to use
     */
    public MimeDbLoader(final MimeParser parser) {
        this.parser = parser;
    }

    /**
     * Loads a database from an input stream.
     *
     * @param in          the input stream to read the database from
     * @param registrator the function to call to store a type
     */
    public void load(final InputStream in, final BiConsumer<String, MimeType> registrator) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        final Pattern splitter = Pattern.compile("\\s+");

        reader.lines()
                .parallel()
                .filter(l -> !l.startsWith("#"))
                .map(splitter::split)
                .filter(l -> l.length > 1)
                .forEach(l -> {
                    final MimeType type = this.parser.parse(l[0]);
                    for (int i = 1; i < l.length; ++i) {
                        registrator.accept(l[i], type);
                    }
                });
    }
}
