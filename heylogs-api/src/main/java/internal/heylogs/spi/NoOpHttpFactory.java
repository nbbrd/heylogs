package internal.heylogs.spi;

import lombok.NonNull;
import nbbrd.heylogs.spi.HttpFactory;
import nbbrd.io.http.HttpClient;

import java.io.IOException;

public enum NoOpHttpFactory implements HttpFactory {

    NO_OP;

    @Override
    public @NonNull HttpClient getClient() {
        return request -> {
            throw new IOException("NoOp");
        };
    }
}
