package internal.heylogs.github;

import _test.Sample;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.Rule;
import nbbrd.service.ServiceId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.regex.Pattern;

import static _test.Sample.using;
import static internal.heylogs.github.GitHubRules.GITHUB_ISSUE_REF;
import static internal.heylogs.github.GitHubRules.GITHUB_PULL_REQUEST_REF;
import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class GitHubRulesTest {

    @Test
    public void testIdPattern() {
        assertThat(GitHubRules.values())
                .extracting(Rule::getId)
                .allMatch(Pattern.compile(ServiceId.KEBAB_CASE).asPredicate());
    }

    @Test
    public void test() {
        Node sample = Sample.using("/Main.md");
        for (GitHubRules rule : GitHubRules.values()) {
            Assertions.assertThat(Nodes.of(Node.class).descendants(sample).map(rule::validate).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateGitHubIssueRef() {
        assertThat(of(Link.class).descendants(using("/Main.md")))
                .map(GitHubRules::validateGitHubIssueRef)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Link.class).descendants(using("/InvalidGitHubIssueRef.md")))
                .map(GitHubRules::validateGitHubIssueRef)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(Failure.builder().rule(GITHUB_ISSUE_REF).message("Expecting GitHub issue ref 172, found 173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateGitHubPullRequestRef() {
        assertThat(of(Link.class).descendants(using("/Main.md")))
                .map(GitHubRules::validateGitHubPullRequestRef)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Link.class).descendants(using("/InvalidGitHubPullRequestRef.md")))
                .map(GitHubRules::validateGitHubPullRequestRef)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(Failure.builder().rule(GITHUB_PULL_REQUEST_REF).message("Expecting GitHub pull request ref 172, found 173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }
}
