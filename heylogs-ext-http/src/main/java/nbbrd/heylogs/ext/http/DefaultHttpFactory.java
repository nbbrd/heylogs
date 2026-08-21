package nbbrd.heylogs.ext.http;

import lombok.NonNull;
import nbbrd.heylogs.spi.HttpFactory;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.*;
import nbbrd.io.http.urlconnection.UrlConnectionHttpClient;
import nbbrd.service.ServiceProvider;
import nl.altindag.ssl.SSLFactory;

@ServiceProvider
public final class DefaultHttpFactory implements HttpFactory {

    @lombok.Getter(lazy = true)
    private final SSLFactory sslFactory = initSSLFactory();

    private static SSLFactory initSSLFactory() {
        return SSLFactory
                .builder()
                .withDefaultTrustMaterial()
                .withSystemTrustMaterial()
                .build();
    }

    @Override
    public @NonNull HttpClient getClient() {
        SSLFactory sslFactory = getSslFactory();
        return new ThrowingStatusDecorator(
                new RetryDecorator(
                        new RedirectDecorator(
                                UrlConnectionHttpClient
                                        .builder()
                                        .userAgent("heylogs")
                                        .sslSocketFactory(sslFactory.getSslSocketFactory())
                                        .hostnameVerifier(sslFactory.getHostnameVerifier())
                                        .build()
                                , 5, RedirectListener.noOp()),
                        3, RetryListener.noOp()),
                ThrowingStatusDecorator.DEFAULT_SHOULD_THROW);
    }
}
