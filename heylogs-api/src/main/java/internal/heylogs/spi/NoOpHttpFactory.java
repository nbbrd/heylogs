package internal.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.HttpFactory;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;

import java.io.IOException;

public enum NoOpHttpFactory implements HttpFactory {

    NO_OP;

    @Override
    public @NonNull HttpClient getClient() {
        return NO_OP_HTTP_CLIENT;
    }

    private static final HttpClient NO_OP_HTTP_CLIENT = new HttpClient() {

        @Override
        public @NonNull String getDescription() {
            return "no_op";
        }

        @Override
        public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
            throw new IOException("NoOp");
        }
    };
}
