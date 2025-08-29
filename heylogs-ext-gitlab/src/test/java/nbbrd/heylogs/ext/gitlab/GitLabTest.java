package nbbrd.heylogs.ext.gitlab;

import com.vladsch.flexmark.ast.Link;
import internal.heylogs.base.ExtendedRules;
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

class GitLabTest {

    @Test
    void testCompliance() {
        assertForgeCompliance(new GitLab());
    }

    @Test
    void testIsCompareLink() {
        Forge x = new GitLab();
        assertThat(x.isCompareLink(urlOf("https://nbb.be"))).isFalse();
        assertThat(x.isCompareLink(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...HEAD"))).isTrue();
        assertThat(x.isCompareLink(urlOf("http://localhost:8080/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...HEAD"))).isFalse();
    }

    @Test
    void testGetProjectURL() {
        Forge x = new GitLab();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getCompareLink(urlOf("https://nbb.be")).getProjectURL());

        assertThat(x.getCompareLink(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/compare/v1.0.0...HEAD")).getProjectURL())
                .isEqualTo(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab"));
    }

    @Test
    public void testForgeRefAsRule() {
        RuleContext context = RuleContext.builder().forge(new GitLab()).build();

        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabCommitRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting COMMIT ref 656ad7d, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabIssueRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting ISSUE ref 1, found 0").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabMergeRequestRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting REQUEST ref 1, found 0").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidGitLabMentionRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting MENTION ref @charphi, found @nbbrd").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsKnownHost() {
        Forge x = new GitLab();
        assertThat(x.isKnownHost(urlOf("https://gitlab.com"))).isTrue();
        assertThat(x.isKnownHost(urlOf("https://gitlabgitlab.com"))).isFalse();
        assertThat(x.isKnownHost(urlOf("https://gitlab.example.com"))).isTrue();
        assertThat(x.isKnownHost(urlOf("https://localhost:8080"))).isFalse();
    }
}