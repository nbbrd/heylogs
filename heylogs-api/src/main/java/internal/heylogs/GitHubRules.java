package internal.heylogs;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static internal.heylogs.RuleSupport.nameToId;
import static java.lang.Integer.parseInt;

public enum GitHubRules implements Rule {

    GITHUB_ISSUE_REF {
        @Override
        public Failure validate(@NonNull Node node) {
            return node instanceof Link ? validateGitHubIssueRef((Link) node) : NO_PROBLEM;
        }
    };

    @Override
    public @NonNull String getId() {
        return nameToId(this);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @VisibleForTesting
    static Failure validateGitHubIssueRef(Link link) {
        IssueLink expected = Parser.of(IssueLink::parse).parse(link.getUrl());
        IssueShortLink found = Parser.of(IssueShortLink::parse).parse(link.getText());
        return expected != null && found != null && !found.isCompatibleWith(expected)
                ? Failure
                .builder()
                .rule(GITHUB_ISSUE_REF)
                .message("Expecting GitHub issue ref " + expected.getIssueNumber() + ", found " + found.getIssueNumber())
                .location(link)
                .build()
                : NO_PROBLEM;
    }

    @MightBeGenerated
    @ServiceProvider
    public static final class Batch implements RuleBatch {

        @Override
        public @NonNull Stream<Rule> getProviders() {
            return Stream.of(GitHubRules.values());
        }
    }

    @VisibleForTesting
    @RepresentableAsString
    @lombok.Value
    static class IssueLink {

        public static @NonNull IssueLink parse(@NonNull CharSequence text) {
            Matcher m = PATTERN.matcher(text);
            if (!m.matches()) throw new IllegalArgumentException(text.toString());
            return new IssueLink(
                    m.group("protocol"),
                    m.group("owner"),
                    m.group("repo"),
                    m.group("type"),
                    parseInt(m.group("issueNumber"))
            );
        }

        @NonNull String protocol;
        @NonNull String owner;
        @NonNull String repo;
        @NonNull String type;
        int issueNumber;

        @Override
        public String toString() {
            return protocol + "://github.com/" + owner + "/" + repo + "/" + type + "/" + issueNumber;
        }

        // https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#issues-and-pull-requests
        private static final Pattern PATTERN = Pattern.compile("(?<protocol>https?)://github\\.com/(?<owner>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})/(?<repo>[a-z\\d._-]{1,100})/(?<type>(issues|pull))/(?<issueNumber>\\d+)(?<issueComment>#issuecomment-(?<issueCommentNumber>\\d+))?");
    }

    @VisibleForTesting
    @RepresentableAsString
    @lombok.Value
    static class IssueShortLink {

        public static @NonNull IssueShortLink parse(@NonNull CharSequence text) {
            Matcher m = PATTERN.matcher(text);
            if (!m.matches()) throw new IllegalArgumentException(text.toString());
            return new IssueShortLink(
                    m.group("owner"),
                    m.group("repo"),
                    parseInt(m.group("issueNumber"))
            );
        }

        @Nullable String owner;
        @Nullable String repo;
        int issueNumber;

        @Override
        public String toString() {
            return hasOwnerRepo()
                    ? owner + "/" + repo + "#" + issueNumber
                    : "#" + issueNumber;
        }

        public boolean isCompatibleWith(@NonNull IssueLink link) {
            return (!hasOwnerRepo() || (link.getOwner().equals(owner) && link.getRepo().equals(repo)))
                    && link.getIssueNumber() == issueNumber;
        }

        private boolean hasOwnerRepo() {
            return owner != null && repo != null;
        }

        // https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/autolinked-references-and-urls#issues-and-pull-requests
        private static final Pattern PATTERN = Pattern.compile("((?<owner>[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38})/(?<repo>[a-z\\d._-]{1,100}))?#(?<issueNumber>\\d+)");
    }
}
