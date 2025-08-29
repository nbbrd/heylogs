package nbbrd.heylogs.ext.gitlab;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import internal.heylogs.spi.URLExtractor;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleContext;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.ext.gitlab.GitLabRefRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class GitLabRefRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new GitLabRefRules());
    }

    @Test
    public void testRules() {
        Node sample = using("/Main.md");
        for (Rule rule : new GitLabRefRules().getProviders().collect(toList())) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitLabCommitRef.md")).map(node -> GITLAB_COMMIT_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitLabIssueRef.md")).map(node -> GITLAB_ISSUE_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitLabMergeRequestRef.md")).map(node -> GITLAB_MERGE_REQUEST_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitLabMentionRef.md")).map(node -> GITLAB_MENTION_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);
    }

    @Test
    public void testValidateGitLabCommitRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITLAB_COMMIT_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabCommitRef.md")))
                .map(node -> GITLAB_COMMIT_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting COMMIT ref 656ad7d, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitLabIssueRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITLAB_ISSUE_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabIssueRef.md")))
                .map(node -> GITLAB_ISSUE_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting ISSUE ref 1, found 0").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitLabMergeRequestRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITLAB_MERGE_REQUEST_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabMergeRequestRef.md")))
                .map(node -> GITLAB_MERGE_REQUEST_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting REQUEST ref 1, found 0").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitLabMentionRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITLAB_MENTION_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabMentionRef.md")))
                .map(node -> GITLAB_MENTION_REF.getRuleIssueOrNull(node, RuleContext.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting MENTION ref @charphi, found @nbbrd").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsGitLabHost() {
        assertThat(GitLab.isGitLabHost(URLExtractor.urlOf("https://gitlab.com"))).isTrue();
        assertThat(GitLab.isGitLabHost(URLExtractor.urlOf("https://gitlabgitlab.com"))).isFalse();
        assertThat(GitLab.isGitLabHost(URLExtractor.urlOf("https://gitlab.example.com"))).isTrue();
        assertThat(GitLab.isGitLabHost(URLExtractor.urlOf("https://localhost:8080"))).isFalse();
    }
}
