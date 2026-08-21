package tests.heylogs.spi;

import lombok.NonNull;
import nbbrd.io.http.HttpHeaders;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.net.MediaType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@lombok.Value(staticConstructor = "of")
public class PersistentResponse implements HttpResponse {

    @NonNull
    MediaType contentType;

    @NonNull
    HttpHeaders headers;

    @NonNull
    String body;

    @Override
    public long getContentLength() {
        return body.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public int getStatusCode() {
        return 200;
    }

    @Override
    public @NonNull InputStream getBody() {
        return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() {
    }
}
