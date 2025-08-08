package nbbrd.heylogs.ext.forgejo;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;
import tests.heylogs.api.Sample;
import tests.heylogs.spi.MockedForgeLink;

import java.util.Objects;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.ext.forgejo.ForgejoRules.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.RuleAssert.assertRuleCompliance;

public class ForgejoRulesTest {

    @Test
    public void testCompliance() {
        assertRuleCompliance(new ForgejoRules());
    }

    @Test
    public void testRules() {
        Node sample = Sample.using("/Main.md");
        for (Rule rule : new ForgejoRules().getProviders().collect(toList())) {
            assertThat(Nodes.of(Node.class).descendants(sample).map(node -> rule.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                    .isEmpty();
        }

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidForgejoIssueRef.md")).map(node -> FORGEJO_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidForgejoPullRequestRef.md")).map(node -> FORGEJO_PULL_REQUEST_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidForgejoMentionRef.md")).map(node -> FORGEJO_MENTION_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);

        assertThat(Nodes.of(Node.class).descendants(using("/InvalidForgejoCommitRef.md")).map(node -> FORGEJO_COMMIT_REF.getRuleIssueOrNull(node, Config.DEFAULT)).filter(Objects::nonNull))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubIssueRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> FORGEJO_ISSUE_REF.getRuleIssueOrNull(node1, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoIssueRef.md")))
                .map(node -> FORGEJO_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref #5173, found #517").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoIssueRefPrefix.md")))
                .map(node -> FORGEJO_ISSUE_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting issue ref #5173, found 5173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubPullRequestRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> FORGEJO_PULL_REQUEST_REF.getRuleIssueOrNull(node1, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoPullRequestRef.md")))
                .map(node1 -> FORGEJO_PULL_REQUEST_REF.getRuleIssueOrNull(node1, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting pull request ref #5170, found #517").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoPullRequestRefPrefix.md")))
                .map(node -> FORGEJO_PULL_REQUEST_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting pull request ref #5170, found 5170").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubMentionRef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node1 -> FORGEJO_MENTION_REF.getRuleIssueOrNull(node1, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoMentionRef.md")))
                .map(node -> FORGEJO_MENTION_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting mention ref @charphi, found @user").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubCommitSHARef() {
        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> FORGEJO_COMMIT_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoCommitRef.md")))
                .map(node -> FORGEJO_COMMIT_REF.getRuleIssueOrNull(node, Config.DEFAULT))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting commit ref b5d40a0, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsForgejoHost() {
        ForgeLink official = MockedForgeLink.parse(urlOf("https://codeberg.org"));
        assertThat(isForgejoHost(official, null)).isTrue();
        assertThat(isForgejoHost(official, "stuff")).isTrue();
        assertThat(isForgejoHost(official, "forgejo")).isTrue();

        ForgeLink invalid = MockedForgeLink.parse(urlOf("https://codebergcodeberg.org"));
        assertThat(isForgejoHost(invalid, null)).isFalse();
        assertThat(isForgejoHost(invalid, "stuff")).isFalse();
        assertThat(isForgejoHost(invalid, "forgejo")).isTrue();

        ForgeLink valid = MockedForgeLink.parse(urlOf("https://codeberg.example.com"));
        assertThat(isForgejoHost(valid, null)).isTrue();
        assertThat(isForgejoHost(valid, "stuff")).isTrue();
        assertThat(isForgejoHost(valid, "forgejo")).isTrue();

        ForgeLink local = MockedForgeLink.parse(urlOf("https://localhost:8080"));
        assertThat(isForgejoHost(local, null)).isFalse();
        assertThat(isForgejoHost(local, "stuff")).isFalse();
        assertThat(isForgejoHost(local, "forgejo")).isTrue();
    }
}
