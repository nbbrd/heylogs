package nbbrd.heylogs.ext.github;

import com.vladsch.flexmark.ast.Link;
import internal.heylogs.base.ExtendedRules;
import internal.heylogs.spi.URLExtractor;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.RuleContext;
import nbbrd.heylogs.spi.RuleIssue;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.data.Index.atIndex;
import static tests.heylogs.api.Sample.using;
import static tests.heylogs.spi.ForgeAssert.assertForgeCompliance;

class GitHubTest {

    @Test
    void testCompliance() {
        assertForgeCompliance(new GitHub());
    }

    @Test
    void testIsCompareLink() {
        Forge x = new GitHub();
        assertThat(x.isCompareLink(urlOf("https://nbb.be"))).isFalse();
        assertThat(x.isCompareLink(urlOf("https://github.com/nbbrd/heylogs/compare/v0.7.2...HEAD"))).isTrue();
        assertThat(x.isCompareLink(urlOf("https://localhost:8080/nbbrd/heylogs/compare/v0.7.2...HEAD"))).isFalse();
    }

    @Test
    void testGetProjectURL() {
        Forge x = new GitHub();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getCompareLink(urlOf("https://nbb.be")).getProjectURL());

        assertThat(x.getCompareLink(URLExtractor.urlOf("https://github.com/nbbrd/heylogs/compare/v0.7.2...HEAD")).getProjectURL())
                .isEqualTo(urlOf("https://github.com/nbbrd/heylogs"));
    }

    @Test
    public void testForgeRefAsRule() {
        RuleContext context = RuleContext.builder().forge(new GitHub()).build();

        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubIssueRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting github ISSUE ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubIssueRefPrefix.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting github ISSUE ref #172, found 172").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubPullRequestRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting github REQUEST ref #172, found #173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubPullRequestRefPrefix.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting github REQUEST ref #172, found 172").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubMentionRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting github MENTION ref @charphi, found @user").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitHubCommitRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting github COMMIT ref 862157d, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsKnownHost() {
        Forge x = new GitHub();
        assertThat(x.isKnownHost(URLExtractor.urlOf("https://github.com"))).isTrue();
        assertThat(x.isKnownHost(URLExtractor.urlOf("https://githubgithub.com"))).isFalse();
        assertThat(x.isKnownHost(URLExtractor.urlOf("https://github.example.com"))).isTrue();
        assertThat(x.isKnownHost(URLExtractor.urlOf("https://localhost:8080"))).isFalse();
    }
}