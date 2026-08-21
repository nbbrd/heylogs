package nbbrd.heylogs.ext.forgejo;

import lombok.NonNull;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.io.http.HttpClient;
import nbbrd.io.http.HttpHeaders;
import nbbrd.io.http.HttpRequest;
import nbbrd.io.http.HttpResponse;
import nbbrd.io.net.MediaType;
import org.junit.jupiter.api.Test;
import tests.heylogs.spi.PersistentResponse;

import java.io.IOException;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

class ForgejoMessageFetcherTest {

    @Test
    void testFetchIssueMessage() throws IOException {
        ForgejoMessageFetcher fetcher = ForgejoMessageFetcher.ISSUE;

        ForgeLink link = ForgejoIssueLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/issues/173"));

        HttpClient client = new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return "";
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
                assertThat(request.getQuery()).hasToString("https://codeberg.org/api/v1/repos/nbbrd/heylogs/issues/173");
                return PersistentResponse.of(MediaType.ANY_TYPE, HttpHeaders.EMPTY, "{\"id\":1,\"title\":\"Add check on Forgejo issue links\",\"state\":\"closed\"}");
            }
        };

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Add check on Forgejo issue links");
    }

    @Test
    void testFetchRequestMessage() throws IOException {
        ForgejoMessageFetcher fetcher = ForgejoMessageFetcher.REQUEST;

        ForgeLink link = ForgejoRequestLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/pulls/172"));

        HttpClient client = new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return "";
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
                assertThat(request.getQuery()).hasToString("https://codeberg.org/api/v1/repos/nbbrd/heylogs/pulls/172");
                return PersistentResponse.of(MediaType.ANY_TYPE, HttpHeaders.EMPTY, "{\"id\":1,\"title\":\"Fix issue with changelog parsing\",\"state\":\"open\"}");
            }
        };

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Fix issue with changelog parsing");
    }

    @Test
    void testFetchMessageWithEscapedTitle() throws IOException {
        ForgejoMessageFetcher fetcher = ForgejoMessageFetcher.ISSUE;

        ForgeLink link = ForgejoIssueLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/issues/173"));

        HttpClient client = new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return "";
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
                return PersistentResponse.of(MediaType.ANY_TYPE, HttpHeaders.EMPTY, "{\"title\":\"Fix \\\"quotes\\\" in title\"}");
            }
        };

        assertThat(fetcher.fetchMessage(client, link))
                .isEqualTo("Fix \"quotes\" in title");
    }

    @Test
    void testFetchMessagePropagatesHttpError() {
        ForgejoMessageFetcher fetcher = ForgejoMessageFetcher.ISSUE;

        ForgeLink link = ForgejoIssueLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/issues/173"));

        HttpClient client = new HttpClient() {
            @Override
            public @NonNull String getDescription() {
                return "";
            }

            @Override
            public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
                throw new IOException("HTTP 404");
            }
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
        assertThat(ForgejoMessageFetcher.ISSUE.buildApiUrl(link))
                .hasToString("https://codeberg.org/api/v1/repos/nbbrd/heylogs/issues/42");
    }

    @Test
    void testBuildApiUrlForRequest() {
        ForgeLink link = ForgejoRequestLink.parse(urlOf("https://codeberg.org/nbbrd/heylogs/pulls/10"));
        assertThat(ForgejoMessageFetcher.REQUEST.buildApiUrl(link))
                .hasToString("https://codeberg.org/api/v1/repos/nbbrd/heylogs/pulls/10");
    }
}

