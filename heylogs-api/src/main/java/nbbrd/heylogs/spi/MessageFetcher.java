package nbbrd.heylogs.spi;

import lombok.NonNull;
import nbbrd.io.http.HttpClient;

import java.io.IOException;

@FunctionalInterface
public interface MessageFetcher {

    /**
     * Return the title of a forge issue
     * @param client the HTTP client to use for fetching the message
     * @param link the issue link
     * @return the title
     * @throws IOException if something goes wrong
     */
    @NonNull
    String fetchMessage(@NonNull HttpClient client, @NonNull ForgeLink link) throws IOException;
}
