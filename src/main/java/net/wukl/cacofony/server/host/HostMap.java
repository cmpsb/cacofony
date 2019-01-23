package net.wukl.cacofony.server.host;

import java.util.HashMap;
import java.util.Map;

/**
 * A map of host-names and their host instances.
 */
public class HostMap {
    /**
     * The default host to return if no matching host could be found.
     */
    private Host defaultHost = null;

    /**
     * The map of hosts.
     */
    private final Map<String, Host> hosts = new HashMap<>();

    /**
     * Sets the default host.
     *
     * @param host the new default host
     */
    public void setDefault(final Host host) {
        this.defaultHost = host;
    }

    /**
     * Adds a new host to the collection.
     *
     * @param host the new host
     */
    public void add(final Host host) {
        this.hosts.put(host.getName(), host);
    }

    /**
     * Looks up a host in the map.
     *
     * If the host is not found and no default host was set, {@code null} is returned.
     *
     * @param name the name of the host to search for
     *
     * @return the host or {@code null} if no host was found and no default was set
     */
    public Host get(final String name) {
        return this.hosts.getOrDefault(name, this.defaultHost);
    }
}
