package nbbrd.heylogs.ext.github;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;
import tests.heylogs.api.Sample;

import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.ext.github.GitHubRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class GitHubRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new GitHubRules());
    }

    @Test
    public void testRules() {
        Node sample = Sample.using("/Main.md");
        for (Rule rule : new GitHubRules().getProviders().collect(toList())) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubIssueRef.md")).map(node -> GITHUB_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubPullRequestRef.md")).map(node -> GITHUB_PULL_REQUEST_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubMentionRef.md")).map(node -> GITHUB_MENTION_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubCommitSHARef.md")).map(node -> GITHUB_COMMIT_SHA_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubIssueRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> GITHUB_ISSUE_REF.getRuleIssueOrNull(node1, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubIssueRef.md")))
                .map(node -> GITHUB_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubIssueRefPrefix.md")))
                .map(node -> GITHUB_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref #172, found 172").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubPullRequestRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> GITHUB_PULL_REQUEST_REF.getRuleIssueOrNull(node1, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubPullRequestRef.md")))
                .map(node1 -> GITHUB_PULL_REQUEST_REF.getRuleIssueOrNull(node1, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting pull request ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubPullRequestRefPrefix.md")))
                .map(node -> GITHUB_PULL_REQUEST_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting pull request ref #172, found 172").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubMentionRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> GITHUB_MENTION_REF.getRuleIssueOrNull(node1, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubMentionRef.md")))
                .map(node -> GITHUB_MENTION_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting mention ref @charphi, found @user").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubCommitSHARef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITHUB_COMMIT_SHA_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubCommitSHARef.md")))
                .map(node -> GITHUB_COMMIT_SHA_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting commit SHA ref 862157d, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsGitHubHost() {
        GitHubIssueLink official = GitHubIssueLink.parse("https://github.com/nbbrd/heylogs/issues/173");
        assertThat(isGitHubHost(official, null)).isTrue();
        assertThat(isGitHubHost(official, "stuff")).isTrue();
        assertThat(isGitHubHost(official, "github")).isTrue();

        GitHubIssueLink invalid = GitHubIssueLink.parse("https://githubgithub.com/nbbrd/heylogs/issues/173");
        assertThat(isGitHubHost(invalid, null)).isFalse();
        assertThat(isGitHubHost(invalid, "stuff")).isFalse();
        assertThat(isGitHubHost(invalid, "github")).isTrue();

        GitHubIssueLink valid = GitHubIssueLink.parse("https://github.example.com/nbbrd/heylogs/issues/173");
        assertThat(isGitHubHost(valid, null)).isTrue();
        assertThat(isGitHubHost(valid, "stuff")).isTrue();
        assertThat(isGitHubHost(valid, "github")).isTrue();

        GitHubIssueLink local = GitHubIssueLink.parse("https://localhost:8080/nbbrd/heylogs/issues/173");
        assertThat(isGitHubHost(local, null)).isFalse();
        assertThat(isGitHubHost(local, "stuff")).isFalse();
        assertThat(isGitHubHost(local, "github")).isTrue();
    }
}
