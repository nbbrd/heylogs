package nbbrd.heylogs.ext.github;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleContext;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;
import tests.heylogs.api.Sample;
import tests.heylogs.spi.MockedForgeLink;

import java.util.Objects;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.ext.github.GitHubRefRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class GitHubRefRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new GitHubRefRules());
    }

    @Test
    public void testRules() {
        Node sample = Sample.using("/Main.md");
        for (Rule rule : new GitHubRefRules().getProviders().collect(toList())) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubIssueRef.md")).map(node -> GITHUB_ISSUE_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubPullRequestRef.md")).map(node -> GITHUB_PULL_REQUEST_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubMentionRef.md")).map(node -> GITHUB_MENTION_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubCommitRef.md")).map(node -> GITHUB_COMMIT_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubIssueRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> GITHUB_ISSUE_REF.getRuleIssueOrNull(node1, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubIssueRef.md")))
                .map(node -> GITHUB_ISSUE_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubIssueRefPrefix.md")))
                .map(node -> GITHUB_ISSUE_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref #172, found 172").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubPullRequestRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> GITHUB_PULL_REQUEST_REF.getRuleIssueOrNull(node1, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubPullRequestRef.md")))
                .map(node1 -> GITHUB_PULL_REQUEST_REF.getRuleIssueOrNull(node1, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting pull request ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubPullRequestRefPrefix.md")))
                .map(node -> GITHUB_PULL_REQUEST_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting pull request ref #172, found 172").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubMentionRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> GITHUB_MENTION_REF.getRuleIssueOrNull(node1, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubMentionRef.md")))
                .map(node -> GITHUB_MENTION_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting mention ref @charphi, found @user").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubCommitSHARef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITHUB_COMMIT_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubCommitRef.md")))
                .map(node -> GITHUB_COMMIT_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting commit ref 862157d, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsGitHubHost() {
        ForgeLink official = MockedForgeLink.parse(urlOf("https://github.com"));
        assertThat(isGitHubHost(official, null)).isTrue();
        assertThat(isGitHubHost(official, "stuff")).isTrue();
        assertThat(isGitHubHost(official, "github")).isTrue();

        ForgeLink invalid = MockedForgeLink.parse(urlOf("https://githubgithub.com"));
        assertThat(isGitHubHost(invalid, null)).isFalse();
        assertThat(isGitHubHost(invalid, "stuff")).isFalse();
        assertThat(isGitHubHost(invalid, "github")).isTrue();

        ForgeLink valid = MockedForgeLink.parse(urlOf("https://github.example.com"));
        assertThat(isGitHubHost(valid, null)).isTrue();
        assertThat(isGitHubHost(valid, "stuff")).isTrue();
        assertThat(isGitHubHost(valid, "github")).isTrue();

        ForgeLink local = MockedForgeLink.parse(urlOf("https://localhost:8080"));
        assertThat(isGitHubHost(local, null)).isFalse();
        assertThat(isGitHubHost(local, "stuff")).isFalse();
        assertThat(isGitHubHost(local, "github")).isTrue();
    }
}
