package net.wukl.cacofony.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * A helper factory to create SSL contexts for the secure parts of the server to use.
 */
public class SslContextFactory {
    private static final Logger logger = LoggerFactory.getLogger(SslContextFactory.class);

    /**
     * Creates an SSL context by reading a key store from an input stream.
     *
     * Due to limitations in Java, it is not possible to use individually encrypted keys in the
     * keystore.
     *
     * @param keyStream the input stream to read it from
     * @param keyStorePassword the password for the keystore
     *
     * @return the SSL context with the keystore
     *
     * @throws IOException if an error occurs while reading from the stream
     * @throws RuntimeException if any other part of the process goes wrong
     */
    public SSLContext fromStream(
            final InputStream keyStream, final String keyStorePassword
    ) throws IOException {
        try {
            final var keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(keyStream, keyStorePassword.toCharArray());

            var keyManagerFactory = KeyManagerFactory.getInstance("PKIX", "SunJSSE");
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            final var managers = keyManagerFactory.getKeyManagers();

            final var context = SSLContext.getInstance("TLS");
            context.init(managers, null, null);

            return context;
        } catch (
                final NoSuchAlgorithmException | KeyStoreException | CertificateException
                        | UnrecoverableKeyException | KeyManagementException
                        | NoSuchProviderException ex
        ) {
            throw new RuntimeException(ex);
        }
    }
}
