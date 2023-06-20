package internal.heylogs;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Locale;
import java.util.stream.Stream;

public enum ExtendedRules implements Rule {

    HTTPS {
        @Override
        public Failure validate(@NotNull Node node) {
            return node instanceof LinkNodeBase ? validateHttps((LinkNodeBase) node) : NO_PROBLEM;
        }
    },
    GITHUB_ISSUE_REF {
        @Override
        public Failure validate(@NotNull Node node) {
            return node instanceof Link ? validateGitHubIssueRef((Link) node) : NO_PROBLEM;
        }
    };

    @Override
    public @NotNull String getId() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @VisibleForTesting
    static Failure validateHttps(LinkNodeBase link) {
        return Parser
                .onURL()
                .parseValue(link.getUrl())
                .filter(url -> !url.getProtocol().equals("https"))
                .map(ignore -> Failure
                        .builder()
                        .rule(HTTPS)
                        .message("Expecting HTTPS protocol")
                        .location(link)
                        .build())
                .orElse(NO_PROBLEM);
    }

    @VisibleForTesting
    static Failure validateGitHubIssueRef(Link link) {
        int expected = getGitHubIssueRefFromURL(link);
        int found = getGitHubIssueRefFromText(link);
        return expected != NO_ISSUE_REF && found != NO_ISSUE_REF && expected != found
                ? Failure
                .builder()
                .rule(GITHUB_ISSUE_REF)
                .message("Expecting GitHub issue ref " + expected + ", found " + found)
                .location(link)
                .build()
                : NO_PROBLEM;
    }

    private static int getGitHubIssueRefFromURL(Link link) {
        URL url = Parser.onURL().parse(link.getUrl());
        if (url != null && url.getHost().equals("github.com")) {
            int index = url.getPath().indexOf("/issues/");
            if (index != -1) {
                return Parser
                        .onInteger()
                        .parseValue(url.getPath().substring(index + 8))
                        .orElse(NO_ISSUE_REF);
            }
        }
        return NO_ISSUE_REF;
    }

    private static int getGitHubIssueRefFromText(Link link) {
        String text = link.getText().toString();
        if (text.startsWith("#")) {
            return Parser
                    .onInteger()
                    .parseValue(text.substring(1))
                    .orElse(NO_ISSUE_REF);
        }
        return NO_ISSUE_REF;
    }

    private static final int NO_ISSUE_REF = -1;

    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public Stream<Rule> getProviders() {
            return Stream.of(ExtendedRules.values());
        }
    }
}
