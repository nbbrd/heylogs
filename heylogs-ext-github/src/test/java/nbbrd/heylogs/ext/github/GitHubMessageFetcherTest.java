package nbbrd.heylogs.ext.github;

import lombok.NonNull;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.PersistentResponse;
import nbbrd.io.net.MediaType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.*;

class GitHubMessageFetcherTest {

    @Test
    void testFetchIssueMessage() throws IOException {
        GitHubMessageFetcher fetcher = GitHubMessageFetcher.ISSUE;

        ForgeLink link = GitHubIssueLink.parse(urlOf("https://github.com/nbbrd/heylogs/issues/173"));

        HttpClient client = request -> {
            assertThat(request.getQuery()).hasToString("https://api.github.com/repos/nbbrd/heylogs/issues/173");
            return PersistentResponse.of(MediaType.ANY_TYPE, "{\"id\":1,\"title\":\"Add check on GitHub Pull Request links\",\"state\":\"closed\"}");
        };

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Add check on GitHub Pull Request links");
    }

    @Test
    void testFetchRequestMessage() throws IOException {
        GitHubMessageFetcher fetcher = GitHubMessageFetcher.REQUEST;

        ForgeLink link = GitHubRequestLink.parse(urlOf("https://github.com/nbbrd/heylogs/pull/172"));

        HttpClient client = request -> {
            assertThat(request.getQuery()).hasToString("https://api.github.com/repos/nbbrd/heylogs/pulls/172");
            return PersistentResponse.of(MediaType.ANY_TYPE, "{\"id\":1,\"title\":\"Fix issue with changelog parsing\",\"state\":\"closed\"}");
        };

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Fix issue with changelog parsing");
    }

    @Test
    void testFetchMessageWithEscapedTitle() throws IOException {
        GitHubMessageFetcher fetcher = GitHubMessageFetcher.ISSUE;

        ForgeLink link = GitHubIssueLink.parse(urlOf("https://github.com/nbbrd/heylogs/issues/173"));

        HttpClient client = url ->
                PersistentResponse.of(MediaType.ANY_TYPE, "{\"title\":\"Fix \\\"quotes\\\" in title\"}");

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Fix \"quotes\" in title");
    }

    @Test
    void testFetchMessagePropagatesHttpError() {
        GitHubMessageFetcher fetcher = GitHubMessageFetcher.ISSUE;

        ForgeLink link = GitHubIssueLink.parse(urlOf("https://github.com/nbbrd/heylogs/issues/173"));

        HttpClient client = url -> {
            throw new IOException("HTTP 404");
        };

        assertThatIOException()
                .isThrownBy(() -> fetcher.fetchMessage(client, link))
                .withMessage("HTTP 404");
    }

    @Test
    void testExtractTitle() throws IOException {
        assertThat(GitHubMessageFetcher.extractTitle(
                "{\"id\":1,\"title\":\"Hello World\",\"state\":\"open\"}"))
                .isEqualTo("Hello World");
    }

    @Test
    void testExtractTitleWithSpecialChars() throws IOException {
        assertThat(GitHubMessageFetcher.extractTitle("{\"title\":\"Fix \\\\path/separator\"}"))
                .isEqualTo("Fix \\path/separator");
    }

    @Test
    void testExtractTitleMissing() {
        assertThatIOException()
                .isThrownBy(() -> GitHubMessageFetcher.extractTitle("{\"id\":1,\"state\":\"open\"}"))
                .withMessageContaining("title");
    }

    @Test
    void testBuildApiUrlForIssue() {
        ForgeLink link = GitHubIssueLink.parse(urlOf("https://github.com/nbbrd/heylogs/issues/173"));

        assertThat(GitHubMessageFetcher.ISSUE.buildApiUrl(link, urlOf("https://api.github.com")))
                .hasToString("https://api.github.com/repos/nbbrd/heylogs/issues/173");
    }

    @Test
    void testBuildApiUrlForRequest() {
        ForgeLink link = GitHubRequestLink.parse(urlOf("https://github.com/nbbrd/heylogs/pull/172"));

        assertThat(GitHubMessageFetcher.REQUEST.buildApiUrl(link, urlOf("https://api.github.com")))
                .hasToString("https://api.github.com/repos/nbbrd/heylogs/pulls/172");
    }

    @Test
    void testBuildApiUrlUnsupportedLink() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> GitHubMessageFetcher.ISSUE.buildApiUrl(
                        new UnsupportedForgeLink(), urlOf("https://api.github.com")))
                .withMessageContaining("Unsupported link type");
    }

    private static final class UnsupportedForgeLink implements ForgeLink {

        @Override
        public @NonNull URL getBase() {
            return toURL();
        }

        @Override
        public @NonNull java.net.URL toURL() {
            return urlOf("https://github.com");
        }

        @Override
        public nbbrd.heylogs.spi.ForgeRef toRef(nbbrd.heylogs.spi.ForgeRef baseRef) {
            return null;
        }
    }
}



