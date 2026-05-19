package nbbrd.heylogs.ext.forgejo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.MessageFetcher;
import nbbrd.io.http.*;
import nbbrd.io.net.MediaType;

import java.io.IOException;
import java.net.URL;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;

// https://forgejo.org/docs/latest/developer/api-usage/
// https://codeberg.org/api/swagger
enum ForgejoMessageFetcher implements MessageFetcher {

    ISSUE {
        @Override
        @NonNull
        URL buildApiUrl(@NonNull ForgeLink link) {
            if (link instanceof ForgejoIssueLink) {
                ForgejoIssueLink issueLink = (ForgejoIssueLink) link;
                return urlOf(issueLink.getBase() + FORGEJO_API_PATH
                        + "/repos/" + issueLink.getOwner()
                        + "/" + issueLink.getRepo()
                        + "/issues/" + issueLink.getIssueNumber());
            }
            throw new IllegalArgumentException("Unsupported link type: " + link.getClass().getName());
        }
    },
    REQUEST {
        @Override
        @NonNull
        URL buildApiUrl(@NonNull ForgeLink link) {
            if (link instanceof ForgejoRequestLink) {
                ForgejoRequestLink requestLink = (ForgejoRequestLink) link;
                return urlOf(requestLink.getBase() + FORGEJO_API_PATH
                        + "/repos/" + requestLink.getOwner()
                        + "/" + requestLink.getRepo()
                        + "/pulls/" + requestLink.getIssueNumber());
            }
            throw new IllegalArgumentException("Unsupported link type: " + link.getClass().getName());
        }
    };

    private static final String FORGEJO_API_PATH = "/api/v1";
    public static final MediaType JSON = MediaType.parse("application/json");

    @Override
    public @NonNull String fetchMessage(@NonNull HttpClient client, @NonNull ForgeLink link) throws IOException {
        URL apiUrl = buildApiUrl(link);
        HttpRequest request = HttpRequest
                .builder()
                .query(apiUrl)
                .method(HttpMethod.GET)
                .mediaType(JSON)
                .build();
        try (HttpResponse response = client.send(request)) {
            return extractTitle(response.getBodyAsString());
        } catch (HttpResponseException ex) {
            throw new IOException("Forgejo API returned HTTP " + ex.getResponseCode() + " for " + request.getQuery());
        }
    }

    @VisibleForTesting
    abstract @NonNull URL buildApiUrl(@NonNull ForgeLink link);

    @VisibleForTesting
    static @NonNull String extractTitle(@NonNull String json) throws IOException {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (!obj.has("title") || obj.get("title").isJsonNull()) {
                throw new IOException("Could not find 'title' field in Forgejo API response");
            }
            return obj.get("title").getAsString();
        } catch (JsonSyntaxException ex) {
            throw new IOException("Invalid JSON response: " + ex.getMessage(), ex);
        }
    }
}
