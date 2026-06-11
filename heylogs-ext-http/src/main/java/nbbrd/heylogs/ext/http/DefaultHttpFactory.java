package nbbrd.heylogs.ext.http;

import lombok.NonNull;
import nbbrd.heylogs.spi.HttpFactory;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.UrlConnectionHttpClient;
import nbbrd.io.http.ext.LazyHttpClient;
import nbbrd.service.ServiceProvider;
import nl.altindag.ssl.SSLFactory;

@ServiceProvider
public final class DefaultHttpFactory implements HttpFactory {

    @Override
    public @NonNull HttpClient getClient() {
        return new LazyHttpClient(() -> {
            SSLFactory sslFactory = SSLFactory
                    .builder()
                    .withDefaultTrustMaterial()
                    .withSystemTrustMaterial()
                    .build();
            return UrlConnectionHttpClient
                    .builder()
                    .userAgent("heylogs")
                    .sslSocketFactory(sslFactory.getSslSocketFactory())
                    .hostnameVerifier(sslFactory.getHostnameVerifier())
                    .build();
        });
    }
}
