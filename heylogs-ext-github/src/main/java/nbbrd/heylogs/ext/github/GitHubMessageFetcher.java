package nbbrd.heylogs.ext.github;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.NonNull;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.MessageFetcher;
import nbbrd.io.http.*;
import nbbrd.io.net.MediaType;

import java.io.IOException;
import java.net.URL;

import static internal.heylogs.spi.URLExtractor.urlOf;

// https://docs.github.com/en/rest/issues/issues#get-an-issue
// https://docs.github.com/en/rest/pulls/pulls#get-a-pull-request
final class GitHubMessageFetcher implements MessageFetcher {

    private static final URL GITHUB_API_BASE = urlOf("https://api.github.com");

    @Override
    public @NonNull String fetchMessage(@NonNull HttpClient client, @NonNull ForgeLink link) throws IOException {
        HttpRequest request = HttpRequest
                .builder()
                .query(buildApiUrl(link, GITHUB_API_BASE))
                .method(HttpMethod.GET)
                .mediaType(MediaType.parse("application/vnd.github+json"))
                .build();
        try (HttpResponse response = client.send(request)) {
            return extractTitle(response.getBodyAsString());
        } catch (HttpResponseException ex) {
            throw new IOException("GitHub API returned HTTP " + ex.getResponseCode() + " for " + request.getQuery());
        }
    }

    static @NonNull URL buildApiUrl(@NonNull ForgeLink link, @NonNull URL apiBase) {
        if (link instanceof GitHubIssueLink) {
            GitHubIssueLink issueLink = (GitHubIssueLink) link;
            return urlOf(URLQueryBuilder.of(apiBase)
                    .path("repos")
                    .path(issueLink.getOwner())
                    .path(issueLink.getRepo())
                    .path("issues")
                    .path(String.valueOf(issueLink.getIssueNumber()))
                    .toString());
        } else if (link instanceof GitHubRequestLink) {
            GitHubRequestLink requestLink = (GitHubRequestLink) link;
            return urlOf(URLQueryBuilder.of(apiBase)
                    .path("repos")
                    .path(requestLink.getOwner())
                    .path(requestLink.getRepo())
                    .path("pulls")
                    .path(String.valueOf(requestLink.getRequestNumber()))
                    .toString());
        }
        throw new IllegalArgumentException("Unsupported link type: " + link.getClass().getName());
    }

    static @NonNull String extractTitle(@NonNull String json) throws IOException {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (!obj.has("title") || obj.get("title").isJsonNull()) {
                throw new IOException("Could not find 'title' field in GitHub API response");
            }
            return obj.get("title").getAsString();
        } catch (JsonSyntaxException ex) {
            throw new IOException("Invalid JSON response: " + ex.getMessage(), ex);
        }
    }
}

