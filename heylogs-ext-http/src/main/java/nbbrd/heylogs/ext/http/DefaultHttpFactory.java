package nbbrd.heylogs.ext.http;

import lombok.NonNull;
import nbbrd.heylogs.spi.HttpFactory;
import nbbrd.io.http.DefaultHttpClient;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpContext;
import nbbrd.service.ServiceProvider;
import nl.altindag.ssl.SSLFactory;

@ServiceProvider
public final class DefaultHttpFactory implements HttpFactory {

    @Override
    public @NonNull HttpClient getClient() {
        return new DefaultHttpClient(getContext());
    }

    private static HttpContext getContext() {
        SSLFactory sslFactory = SSLFactory
                .builder()
                .withDefaultTrustMaterial()
                .withSystemTrustMaterial()
                .build();
        return HttpContext
                .builder()
                .userAgent("heylogs")
                .sslSocketFactory(sslFactory::getSslSocketFactory)
                .hostnameVerifier(sslFactory::getHostnameVerifier)
                .build();
    }
}
