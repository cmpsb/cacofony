package net.wukl.cacofony.server.protocol;

import net.wukl.cacofony.http.request.RequestParser;
import net.wukl.cacofony.server.Connection;
import net.wukl.cacofony.server.host.HostMap;

/**
 * A factory for HTTP protocol instances used to serve HTTP connections.
 */
public class HttpProtocolFactory implements ProtocolFactory<HttpProtocol> {
    /**
     * The hosts in the server.
     */
    private final HostMap hosts;

    /**
     * The HTTP request parser.
     */
    private final RequestParser requestParser;

    /**
     * Creates a new HTTP protocol factory.
     *
     * @param hosts the hosts registered in the server
     * @param requestParser the HTTP request parser
     */
    public HttpProtocolFactory(final HostMap hosts, final RequestParser requestParser) {
        this.hosts = hosts;
        this.requestParser = requestParser;
    }

    /**
     * Builds an instance of the protocol.
     *
     * @return the protocol instance
     */
    @Override
    public HttpProtocol build(final Connection conn) {
        return new HttpProtocol(conn, this.hosts, this.requestParser);
    }
}
