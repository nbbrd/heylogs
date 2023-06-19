package internal.heylogs;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.service.ServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
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
        try {
            if (new URL(link.getUrl().toString()).getProtocol().equals("http")) {
                return Failure
                        .builder()
                        .rule(HTTPS)
                        .message("Expecting HTTPS protocol")
                        .location(link)
                        .build();
            }
        } catch (MalformedURLException e) {
        }
        return NO_PROBLEM;
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
        try {
            URL url = new URL(link.getUrl().toString());
            if (url.getHost().equals("github.com")) {
                int index = url.getPath().indexOf("/issues/");
                if (index != -1) {
                    return Integer.parseInt(url.getPath().substring(index + 8));
                }
            }
        } catch (MalformedURLException | NumberFormatException ex) {
        }
        return NO_ISSUE_REF;
    }

    private static int getGitHubIssueRefFromText(Link link) {
        try {
            String text = link.getText().toString();
            if (text.startsWith("#")) {
                return Integer.parseInt(text.substring(1));
            }
        } catch (NumberFormatException ex) {
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
