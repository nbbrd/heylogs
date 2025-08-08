package nbbrd.heylogs.ext.gitlab;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;
import tests.heylogs.spi.MockedForgeLink;

import java.util.Objects;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.ext.gitlab.GitLabRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class GitLabRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new GitLabRules());
    }

    @Test
    public void testRules() {
        Node sample = using("/Main.md");
        for (Rule rule : new GitLabRules().getProviders().collect(toList())) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitLabCommitRef.md")).map(node -> GITLAB_COMMIT_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitLabIssueRef.md")).map(node -> GITLAB_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitLabMergeRequestRef.md")).map(node -> GITLAB_MERGE_REQUEST_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidGitLabMentionRef.md")).map(node -> GITLAB_MENTION_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);
    }

    @Test
    public void testValidateGitLabCommitRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITLAB_COMMIT_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabCommitRef.md")))
                .map(node -> GITLAB_COMMIT_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting commit ref 656ad7d, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitLabIssueRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITLAB_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabIssueRef.md")))
                .map(node -> GITLAB_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref 1, found 0").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitLabMergeRequestRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITLAB_MERGE_REQUEST_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabMergeRequestRef.md")))
                .map(node -> GITLAB_MERGE_REQUEST_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting merge request ref 1, found 0").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitLabMentionRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> GITLAB_MENTION_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabMentionRef.md")))
                .map(node -> GITLAB_MENTION_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting mention ref @charphi, found @nbbrd").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsGitLabHost() {
        ForgeLink official = MockedForgeLink.parse(urlOf("https://gitlab.com"));
        assertThat(isGitLabHost(official, null)).isTrue();
        assertThat(isGitLabHost(official, "stuff")).isTrue();
        assertThat(isGitLabHost(official, "gitlab")).isTrue();

        ForgeLink invalid = MockedForgeLink.parse(urlOf("https://gitlabgitlab.com"));
        assertThat(isGitLabHost(invalid, null)).isFalse();
        assertThat(isGitLabHost(invalid, "stuff")).isFalse();
        assertThat(isGitLabHost(invalid, "gitlab")).isTrue();

        ForgeLink valid = MockedForgeLink.parse(urlOf("https://gitlab.example.com"));
        assertThat(isGitLabHost(valid, null)).isTrue();
        assertThat(isGitLabHost(valid, "stuff")).isTrue();
        assertThat(isGitLabHost(valid, "gitlab")).isTrue();

        ForgeLink local = MockedForgeLink.parse(urlOf("https://localhost:8080"));
        assertThat(isGitLabHost(local, null)).isFalse();
        assertThat(isGitLabHost(local, "stuff")).isFalse();
        assertThat(isGitLabHost(local, "gitlab")).isTrue();
    }
}
