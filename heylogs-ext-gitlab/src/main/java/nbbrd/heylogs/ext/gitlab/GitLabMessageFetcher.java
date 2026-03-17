package nbbrd.heylogs.ext.gitlab;

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
import java.util.List;

import static internal.heylogs.spi.URLExtractor.urlOf;

// https://docs.gitlab.com/ee/api/issues.html#single-issue
// https://docs.gitlab.com/ee/api/merge_requests.html#get-single-mr
final class GitLabMessageFetcher implements MessageFetcher {

    private static final String GITLAB_API_PATH = "/api/v4";

    @Override
    public @NonNull String fetchMessage(@NonNull HttpClient client, @NonNull ForgeLink link) throws IOException {
        URL apiUrl = buildApiUrl(link);
        HttpRequest request = HttpRequest
                .builder()
                .query(apiUrl)
                .method(HttpMethod.GET)
                .mediaType(MediaType.parse("application/json"))
                .build();
        try (HttpResponse response = client.send(request)) {
            return extractTitle(response.getBodyAsString());
        } catch (HttpResponseException ex) {
            throw new IOException("GitLab API returned HTTP " + ex.getResponseCode() + " for " + request.getQuery());
        }
    }

    static @NonNull URL buildApiUrl(@NonNull ForgeLink link) {
        if (link instanceof GitLabIssueLink) {
            GitLabIssueLink issueLink = (GitLabIssueLink) link;
            String encodedProject = encodeProjectPath(issueLink.getNamespace(), issueLink.getProject());
            return urlOf(issueLink.getBase() + GITLAB_API_PATH + "/projects/" + encodedProject + "/issues/" + issueLink.getNumber());
        } else if (link instanceof GitLabRequestLink) {
            GitLabRequestLink requestLink = (GitLabRequestLink) link;
            String encodedProject = encodeProjectPath(requestLink.getNamespace(), requestLink.getProject());
            return urlOf(requestLink.getBase() + GITLAB_API_PATH + "/projects/" + encodedProject + "/merge_requests/" + requestLink.getNumber());
        }
        throw new IllegalArgumentException("Unsupported link type: " + link.getClass().getName());
    }

    static @NonNull String extractTitle(@NonNull String json) throws IOException {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (!obj.has("title") || obj.get("title").isJsonNull()) {
                throw new IOException("Could not find 'title' field in GitLab API response");
            }
            return obj.get("title").getAsString();
        } catch (JsonSyntaxException ex) {
            throw new IOException("Invalid JSON response: " + ex.getMessage(), ex);
        }
    }

    private static String encodeProjectPath(@NonNull List<String> namespace, @NonNull String project) {
        StringBuilder sb = new StringBuilder();
        for (String part : namespace) {
            sb.append(part).append("%2F");
        }
        sb.append(project);
        return sb.toString();
    }
}

