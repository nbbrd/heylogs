package nbbrd.heylogs.ext.forgejo;

import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.ext.PersistentResponse;
import nbbrd.io.net.MediaType;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.*;

class ForgejoMessageFetcherTest {

    @Test
    void testFetchIssueMessage() throws IOException {
        ForgejoMessageFetcher fetcher = new ForgejoMessageFetcher();

        ForgeLink link = ForgejoIssueLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/issues/173"));

        HttpClient client = request -> {
            assertThat(request.getQuery()).hasToString("https://codeberg.org/api/v1/repos/nbbrd/heylogs/issues/173");
            return PersistentResponse.of(MediaType.ANY_TYPE, "{\"id\":1,\"title\":\"Add check on Forgejo issue links\",\"state\":\"closed\"}");
        };

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Add check on Forgejo issue links");
    }

    @Test
    void testFetchRequestMessage() throws IOException {
        ForgejoMessageFetcher fetcher = new ForgejoMessageFetcher();

        ForgeLink link = ForgejoRequestLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/pulls/172"));

        HttpClient client = request -> {
            assertThat(request.getQuery()).hasToString("https://codeberg.org/api/v1/repos/nbbrd/heylogs/pulls/172");
            return PersistentResponse.of(MediaType.ANY_TYPE, "{\"id\":1,\"title\":\"Fix issue with changelog parsing\",\"state\":\"open\"}");
        };

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Fix issue with changelog parsing");
    }

    @Test
    void testFetchMessageWithEscapedTitle() throws IOException {
        ForgejoMessageFetcher fetcher = new ForgejoMessageFetcher();

        ForgeLink link = ForgejoIssueLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/issues/173"));

        HttpClient client = url ->
                PersistentResponse.of(MediaType.ANY_TYPE, "{\"title\":\"Fix \\\"quotes\\\" in title\"}");

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Fix \"quotes\" in title");
    }

    @Test
    void testFetchMessagePropagatesHttpError() {
        ForgejoMessageFetcher fetcher = new ForgejoMessageFetcher();

        ForgeLink link = ForgejoIssueLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/issues/173"));

        HttpClient client = url -> {
            throw new IOException("HTTP 404");
        };

        assertThatIOException()
                .isThrownBy(() -> fetcher.fetchMessage(client, link))
                .withMessage("HTTP 404");
    }

    @Test
    void testExtractTitle() throws IOException {
        assertThat(ForgejoMessageFetcher.extractTitle(
                "{\"id\":1,\"title\":\"Hello World\",\"state\":\"open\"}"))
                .isEqualTo("Hello World");
    }

    @Test
    void testBuildApiUrlForIssue() {
        ForgeLink link = ForgejoIssueLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/issues/42"));
        assertThat(ForgejoMessageFetcher.buildApiUrl(link))
                .hasToString("https://codeberg.org/api/v1/repos/nbbrd/heylogs/issues/42");
    }

    @Test
    void testBuildApiUrlForRequest() {
        ForgeLink link = ForgejoRequestLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/pulls/10"));
        assertThat(ForgejoMessageFetcher.buildApiUrl(link))
                .hasToString("https://codeberg.org/api/v1/repos/nbbrd/heylogs/pulls/10");
    }
}

