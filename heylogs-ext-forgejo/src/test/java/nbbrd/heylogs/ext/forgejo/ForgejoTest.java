package nbbrd.heylogs.ext.forgejo;

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

class ForgejoTest {

    @Test
    void testCompliance() {
        assertForgeCompliance(new Forgejo());
    }

    @Test
    void testIsCompareLink() {
        Forge x = new Forgejo();
        assertThat(x.isCompareLink(urlOf("https://nbb.be"))).isFalse();
        assertThat(x.isCompareLink(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1"))).isTrue();
        assertThat(x.isCompareLink(urlOf("https://localhost:8080/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1"))).isFalse();
    }

    @Test
    void testGetProjectURL() {
        Forge x = new Forgejo();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.getCompareLink(urlOf("https://nbb.be")).getProjectURL());

        assertThat(x.getCompareLink(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/compare/0.86.0...0.86.1")).getProjectURL())
                .isEqualTo(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge"));
    }

    @Test
    public void testForgeRefAsRule() {
        RuleContext context = RuleContext.builder().forge(new Forgejo()).build();

        assertThat(Nodes.of(Link.class).descendants(using("/Main.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoIssueRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting ISSUE ref #5173, found #517").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoIssueRefPrefix.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting ISSUE ref #5173, found 5173").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoPullRequestRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting REQUEST ref #5170, found #517").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoPullRequestRefPrefix.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting REQUEST ref #5170, found 5170").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoMentionRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting MENTION ref @charphi, found @user").line(2).column(1).build(), atIndex(0))
                .hasSize(1);

        assertThat(Nodes.of(Link.class).descendants(using("/InvalidForgejoCommitRef.md")))
                .map(node -> ExtendedRules.validateForgeRef(node, context))
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Expecting COMMIT ref b5d40a0, found 0000000").line(2).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testIsKnownHost() {
        Forge x = new Forgejo();
        assertThat(x.isKnownHost(urlOf("https://codeberg.org"))).isTrue();
        assertThat(x.isKnownHost(urlOf("https://codebergcodeberg.org"))).isFalse();
        assertThat(x.isKnownHost(urlOf("https://codeberg.example.com"))).isTrue();
        assertThat(x.isKnownHost(urlOf("https://localhost:8080"))).isFalse();
    }
}