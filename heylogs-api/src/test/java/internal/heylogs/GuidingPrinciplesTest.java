package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleLoader;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static _test.Sample.using;
import static internal.heylogs.GuidingPrinciples.validateForHumans;
import static internal.heylogs.GuidingPrinciples.validateLatestVersionFirst;
import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class
GuidingPrinciplesTest {

    @Test
    public void testIdPattern() {
        assertThat(GuidingPrinciples.values())
                .extracting(Rule::getRuleId)
                .allMatch(RuleLoader.ID_PATTERN.asPredicate());
    }

    @Test
    public void testSample() {
        Node sample = using("/Main.md");
        for (GuidingPrinciples rule : GuidingPrinciples.values()) {
            assertThat(of(Node.class).descendants(sample).map(rule::getRuleIssueOrNull).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateForHumans() {
        assertThat(validateForHumans(using("/Main.md")))
                .isNull();

        assertThat(validateForHumans(using("/Empty.md")))
                .isEqualTo(RuleIssue.builder().message("Missing Changelog heading").line(1).column(1).build());

        assertThat(validateForHumans(using("/NoChangelog.md")))
                .isEqualTo(RuleIssue.builder().message("Invalid text").line(1).column(1).build());

        assertThat(validateForHumans(using("/TooManyChangelog.md")))
                .isEqualTo(RuleIssue.builder().message("Too many Changelog headings").line(1).column(1).build());
    }

    @Test
    public void testValidateEntryForEveryVersions() {
        assertThat(of(Heading.class).descendants(using("/Main.md")))
                .map(GuidingPrinciples::validateAllH2ContainAVersion)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("/InvalidVersion.md")))
                .map(GuidingPrinciples::validateAllH2ContainAVersion)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Invalid date format").line(4).column(1).build(), atIndex(0))
                .contains(RuleIssue.builder().message("Missing date part").line(5).column(1).build(), atIndex(1))
                .contains(RuleIssue.builder().message("Missing ref link").line(6).column(1).build(), atIndex(2))
                .hasSize(3);

    }

    @Test
    public void testValidateTypeOfChangesGrouped() {
        assertThat(of(Heading.class).descendants(using("/Main.md")))
                .map(GuidingPrinciples::validateTypeOfChangesGrouped)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("/InvalidTypeOfChange.md")))
                .map(GuidingPrinciples::validateTypeOfChangesGrouped)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Cannot parse 'Stuff'").line(7).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateLinkable() {
        assertThat(of(Heading.class).descendants(using("/Main.md")))
                .map(GuidingPrinciples::validateLinkable)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("/MissingReference.md")))
                .map(GuidingPrinciples::validateLinkable)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(RuleIssue.builder().message("Missing reference '1.1.0'").line(5).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateLatestVersionFirst() {
        assertThat(validateLatestVersionFirst(using("/Main.md")))
                .isNull();

        assertThat(validateLatestVersionFirst(using("/Empty.md")))
                .isNull();

        assertThat(validateLatestVersionFirst(using("/NotLatestVersionFirst.md")))
                .isEqualTo(RuleIssue.builder().message("Versions not sorted").line(3).column(1).build());

        assertThat(validateLatestVersionFirst(using("/UnsortedVersion.md")))
                .isEqualTo(RuleIssue.builder().message("Versions not sorted").line(3).column(1).build());

        assertThat(validateLatestVersionFirst(using("/InvalidVersion.md")))
                .isEqualTo(RuleIssue.builder().message("Versions not sorted").line(7).column(1).build());
    }
}
