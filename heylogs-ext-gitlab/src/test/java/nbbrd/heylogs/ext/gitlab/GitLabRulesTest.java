package nbbrd.heylogs.ext.gitlab;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Config;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.ext.gitlab.GitLabRules.GITLAB_COMMIT_REF;
import static nbbrd.heylogs.ext.gitlab.GitLabRules.isGitLabHost;
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
    }

    @Test
    public void testValidateGitHubCommitRef() {
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
    public void testIsGitLabHost() {
        ForgeLink official = GitLabCommitLink.parse("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/commit/656ad7df2a11dcdbaf206a3b59d327fc67f226ac");
        assertThat(isGitLabHost(official, null)).isTrue();
        assertThat(isGitLabHost(official, "stuff")).isTrue();
        assertThat(isGitLabHost(official, "gitlab")).isTrue();

        ForgeLink invalid = GitLabCommitLink.parse("https://gitlabgitlab.com/nbbrd/heylogs-ext-gitlab/-/commit/656ad7df2a11dcdbaf206a3b59d327fc67f226ac");
        assertThat(isGitLabHost(invalid, null)).isFalse();
        assertThat(isGitLabHost(invalid, "stuff")).isFalse();
        assertThat(isGitLabHost(invalid, "gitlab")).isTrue();

        ForgeLink valid = GitLabCommitLink.parse("https://gitlab.example.com/nbbrd/heylogs-ext-gitlab/-/commit/656ad7df2a11dcdbaf206a3b59d327fc67f226ac");
        assertThat(isGitLabHost(valid, null)).isTrue();
        assertThat(isGitLabHost(valid, "stuff")).isTrue();
        assertThat(isGitLabHost(valid, "gitlab")).isTrue();

        ForgeLink local = GitLabCommitLink.parse("https://localhost:8080/nbbrd/heylogs-ext-gitlab/-/commit/656ad7df2a11dcdbaf206a3b59d327fc67f226ac");
        assertThat(isGitLabHost(local, null)).isFalse();
        assertThat(isGitLabHost(local, "stuff")).isFalse();
        assertThat(isGitLabHost(local, "gitlab")).isTrue();
    }
}
