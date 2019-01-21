package net.wukl.cacofony.server.host;

/**
 * A container class specifying a static file or resource route.
 *
 * @author Luc Everse
 */
public class StaticRouteEntry {
    /**
     * The URL prefix.
     */
    private final String prefix;

    /**
     * The directory containing the files, either on disk on in a jar.
     */
    private final String path;

    /**
     * The class used to refer to the files, {@code null} if the files are on disk.
     */
    private final Class<?> jar;

    /**
     * Creates a new static route entry.
     *
     * @param prefix the URL prefix
     * @param path   the path
     * @param jar    the class
     */
    public StaticRouteEntry(final String prefix, final String path, final Class<?> jar) {
        this.prefix = prefix;
        this.path = path;
        this.jar = jar;
    }

    /**
     * Creates a new static route entry.
     *
     * @param prefix the URL prefix
     * @param path   the path
     */
    public StaticRouteEntry(final String prefix, final String path) {
        this(prefix, path, null);
    }

    /**
     * Returns the URL prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Returns the path, either in the jar or on disk.
     *
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Returns the jar the files are in or {@code null} if they're on disk.
     *
     * @return the jar
     */
    public Class<?> getJar() {
        return this.jar;
    }
}
