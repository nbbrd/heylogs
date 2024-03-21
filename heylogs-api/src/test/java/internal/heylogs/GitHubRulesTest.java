package internal.heylogs;

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
import static internal.heylogs.GitHubRules.GITHUB_ISSUE_REF;
import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
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
    public void testIssueLink() {
        assertThatIllegalArgumentException()
                .isThrownBy(()-> GitHubRules.IssueLink.parse("https://github.com/nbbrd/heylogs/issues/"));

        assertThatIllegalArgumentException()
                .isThrownBy(()-> GitHubRules.IssueLink.parse("https://github.com/nbbrd/heylogs/173"));

        assertThat(GitHubRules.IssueLink.parse("https://github.com/nbbrd/heylogs/issues/173"))
                .returns("https", GitHubRules.IssueLink::getProtocol)
                .returns("nbbrd", GitHubRules.IssueLink::getOwner)
                .returns("heylogs", GitHubRules.IssueLink::getRepo)
                .returns("issues", GitHubRules.IssueLink::getType)
                .returns(173, GitHubRules.IssueLink::getIssueNumber)
                .hasToString("https://github.com/nbbrd/heylogs/issues/173");

        assertThat(GitHubRules.IssueLink.parse("https://github.com/nbbrd/heylogs/pull/217"))
                .returns("https", GitHubRules.IssueLink::getProtocol)
                .returns("nbbrd", GitHubRules.IssueLink::getOwner)
                .returns("heylogs", GitHubRules.IssueLink::getRepo)
                .returns("pull", GitHubRules.IssueLink::getType)
                .returns(217, GitHubRules.IssueLink::getIssueNumber)
                .hasToString("https://github.com/nbbrd/heylogs/pull/217");
    }

    @Test
    public void testIssueShortLink() {
        assertThatIllegalArgumentException()
                .isThrownBy(()-> GitHubRules.IssueShortLink.parse("#"));

        assertThatIllegalArgumentException()
                .isThrownBy(()-> GitHubRules.IssueShortLink.parse("heylogs#173"));

        assertThat(GitHubRules.IssueShortLink.parse("#173"))
                .returns(null, GitHubRules.IssueShortLink::getOwner)
                .returns(null, GitHubRules.IssueShortLink::getRepo)
                .returns(173, GitHubRules.IssueShortLink::getIssueNumber)
                .hasToString("#173");

        assertThat(GitHubRules.IssueShortLink.parse("nbbrd/heylogs#173"))
                .returns("nbbrd", GitHubRules.IssueShortLink::getOwner)
                .returns("heylogs", GitHubRules.IssueShortLink::getRepo)
                .returns(173, GitHubRules.IssueShortLink::getIssueNumber)
                .hasToString("nbbrd/heylogs#173");
    }
}
