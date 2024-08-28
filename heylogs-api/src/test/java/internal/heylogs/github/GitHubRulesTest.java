package internal.heylogs.github;

import tests.heylogs.api.Sample;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleLoader;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static tests.heylogs.api.Sample.using;
import static internal.heylogs.github.GitHubRules.*;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class GitHubRulesTest {

    @Test
    public void testIdPattern() {
        assertThat(new GitHubRules().getProviders())
                .extracting(Rule::getRuleId)
                .allMatch(RuleLoader.ID_PATTERN.asPredicate());
    }

    @Test
    public void testRules() {
        Node sample = Sample.using("/Main.md");
        for (Rule rule : new GitHubRules().getProviders().collect(toList())) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(rule::getRuleIssueOrNull).filter(Objects::nonNull))
                    .isEmpty();
        }

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubIssueRef.md")).map(GITHUB_ISSUE_REF::getRuleIssueOrNull).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubPullRequestRef.md")).map(GITHUB_PULL_REQUEST_REF::getRuleIssueOrNull).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubMentionRef.md")).map(GITHUB_MENTION_REF::getRuleIssueOrNull).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitHubCommitSHARef.md")).map(GITHUB_COMMIT_SHA_REF::getRuleIssueOrNull).filter(Objects::nonNull))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubIssueRef() {
        assertThat(of(Link.class).descendants(using("/Main.md")))
                .map(GITHUB_ISSUE_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Link.class).descendants(using("/InvalidGitHubIssueRef.md")))
                .map(GITHUB_ISSUE_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting GitHub issue ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubPullRequestRef() {
        assertThat(of(Link.class).descendants(using("/Main.md")))
                .map(GITHUB_PULL_REQUEST_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Link.class).descendants(using("/InvalidGitHubPullRequestRef.md")))
                .map(GITHUB_PULL_REQUEST_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting GitHub pull request ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubMentionRef() {
        assertThat(of(Link.class).descendants(using("/Main.md")))
                .map(GITHUB_MENTION_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Link.class).descendants(using("/InvalidGitHubMentionRef.md")))
                .map(GITHUB_MENTION_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting GitHub mention ref @charphi, found @user").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubCommitSHARef() {
        assertThat(of(Link.class).descendants(using("/Main.md")))
                .map(GITHUB_COMMIT_SHA_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Link.class).descendants(using("/InvalidGitHubCommitSHARef.md")))
                .map(GITHUB_COMMIT_SHA_REF::getRuleIssueOrNull)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting GitHub commit SHA ref 862157d, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }
}
