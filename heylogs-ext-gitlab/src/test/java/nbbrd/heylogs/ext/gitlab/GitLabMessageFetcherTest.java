package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.PersistentResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.io.net.MediaType.ANY_TYPE;
import static org.assertj.core.api.Assertions.*;

class GitLabMessageFetcherTest {

    @Test
    void testFetchIssueMessage() throws IOException {
        GitLabMessageFetcher x = GitLabMessageFetcher.ISSUE;

        ForgeLink link = GitLabIssueLink.parse(urlOf("https://gitlab.com/nbbrd/heylogs/-/issues/173"));

        HttpClient client = request -> {
            assertThat(request.getQuery()).hasToString("https://gitlab.com/api/v4/projects/nbbrd%2Fheylogs/issues/173");
            return PersistentResponse.of(ANY_TYPE, "{\"id\":1,\"title\":\"Add check on GitLab issue links\",\"state\":\"closed\"}");
        };

        assertThat(x.fetchMessage(client, link))
                .isEqualTo("Add check on GitLab issue links");
    }

    @Test
    void testFetchRequestMessage() throws IOException {
        GitLabMessageFetcher fetcher = GitLabMessageFetcher.REQUEST;

        ForgeLink link = GitLabRequestLink.parse(urlOf("https://gitlab.com/nbbrd/heylogs/-/merge_requests/172"));

        HttpClient client = request -> {
            assertThat(request.getQuery()).hasToString("https://gitlab.com/api/v4/projects/nbbrd%2Fheylogs/merge_requests/172");
            return PersistentResponse.of(ANY_TYPE, "{\"id\":1,\"title\":\"Fix issue with changelog parsing\",\"state\":\"merged\"}");
        };

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Fix issue with changelog parsing");
    }

    @Test
    void testFetchMessageWithEscapedTitle() throws IOException {
        GitLabMessageFetcher fetcher = GitLabMessageFetcher.ISSUE;

        ForgeLink link = GitLabIssueLink.parse(urlOf("https://gitlab.com/nbbrd/heylogs/-/issues/173"));

        HttpClient client = url ->
                PersistentResponse.of(ANY_TYPE, "{\"title\":\"Fix \\\"quotes\\\" in title\"}");

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Fix \"quotes\" in title");
    }

    @Test
    void testFetchMessagePropagatesHttpError() {
        GitLabMessageFetcher fetcher = GitLabMessageFetcher.ISSUE;

        ForgeLink link = GitLabIssueLink.parse(urlOf("https://gitlab.com/nbbrd/heylogs/-/issues/173"));

        HttpClient client = url -> {
            throw new IOException("HTTP 404");
        };

        assertThatIOException()
                .isThrownBy(() -> fetcher.fetchMessage(client, link))
                .withMessage("HTTP 404");
    }

    @Test
    void testExtractTitle() throws IOException {
        assertThat(GitLabMessageFetcher.extractTitle(
                "{\"id\":1,\"title\":\"Hello World\",\"state\":\"opened\"}"))
                .isEqualTo("Hello World");
    }

    @Test
    void testExtractTitleWithSpecialChars() throws IOException {
        assertThat(GitLabMessageFetcher.extractTitle("{\"title\":\"Fix \\\\path/separator\"}"))
                .isEqualTo("Fix \\path/separator");
    }

    @Test
    void testExtractTitleMissing() {
        assertThatIOException()
                .isThrownBy(() -> GitLabMessageFetcher.extractTitle("{\"id\":1,\"state\":\"opened\"}"))
                .withMessageContaining("title");
    }

    @Test
    void testBuildApiUrlForIssue() {
        ForgeLink link = GitLabIssueLink.parse(urlOf("https://gitlab.com/nbbrd/heylogs/-/issues/173"));

        assertThat(GitLabMessageFetcher.ISSUE.buildApiUrl(link))
                .hasToString("https://gitlab.com/api/v4/projects/nbbrd%2Fheylogs/issues/173");
    }

    @Test
    void testBuildApiUrlForRequest() {
        ForgeLink link = GitLabRequestLink.parse(urlOf("https://gitlab.com/nbbrd/heylogs/-/merge_requests/172"));

        assertThat(GitLabMessageFetcher.REQUEST.buildApiUrl(link))
                .hasToString("https://gitlab.com/api/v4/projects/nbbrd%2Fheylogs/merge_requests/172");
    }

    @Test
    void testBuildApiUrlForNestedNamespace() {
        ForgeLink link = GitLabIssueLink.parse(urlOf("https://gitlab.com/group/subgroup/myproject/-/issues/42"));

        assertThat(GitLabMessageFetcher.ISSUE.buildApiUrl(link))
                .hasToString("https://gitlab.com/api/v4/projects/group%2Fsubgroup%2Fmyproject/issues/42");
    }

    @Test
    void testBuildApiUrlUnsupportedLink() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> GitLabMessageFetcher.ISSUE.buildApiUrl(new UnsupportedForgeLink()))
                .withMessageContaining("Unsupported link type");
    }

    private static final class UnsupportedForgeLink implements ForgeLink {

        @Override
        public @NonNull URL getBase() {
            return toURL();
        }

        @Override
        public @NonNull java.net.URL toURL() {
            return urlOf("https://gitlab.com");
        }

        @Override
        public nbbrd.heylogs.spi.ForgeRef toRef(nbbrd.heylogs.spi.ForgeRef baseRef) {
            return null;
        }
    }
}
