package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Failure;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static internal.heylogs.GuidingPrinciples.*;
import static nbbrd.heylogs.Nodes.of;
import static _test.Sample.using;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class GuidingPrinciplesTest {

    @Test
    public void testSample() {
        Node sample = using("/Main.md");
        for (GuidingPrinciples rule : GuidingPrinciples.values()) {
            assertThat(of(Node.class).descendants(sample).map(rule::validate).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateForHumans() {
        assertThat(validateForHumans(using("/Main.md")))
                .isNull();

        assertThat(validateForHumans(using("/Empty.md")))
                .isEqualTo(Failure.builder().rule(FOR_HUMANS).message("Missing Changelog heading").line(1).column(1).build());

        assertThat(validateForHumans(using("/NoChangelog.md")))
                .isEqualTo(Failure.builder().rule(FOR_HUMANS).message("Invalid text").line(1).column(1).build());

        assertThat(validateForHumans(using("/TooManyChangelog.md")))
                .isEqualTo(Failure.builder().rule(FOR_HUMANS).message("Too many Changelog headings").line(1).column(1).build());
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
                .contains(Failure.builder().rule(ALL_H2_CONTAIN_A_VERSION).message("Invalid date format").line(2).column(1).build(), atIndex(0))
                .contains(Failure.builder().rule(ALL_H2_CONTAIN_A_VERSION).message("Missing date part").line(3).column(1).build(), atIndex(1))
                .contains(Failure.builder().rule(ALL_H2_CONTAIN_A_VERSION).message("Missing ref link").line(4).column(1).build(), atIndex(2))
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
                .contains(Failure.builder().rule(TYPE_OF_CHANGES_GROUPED).message("Cannot parse 'Stuff'").line(7).column(1).build(), atIndex(0))
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
                .contains(Failure.builder().rule(LINKABLE).message("Missing reference '1.1.0'").line(5).column(1).build(), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateLatestVersionFirst() {
        assertThat(validateLatestVersionFirst(using("/Main.md")))
                .isNull();

        assertThat(validateLatestVersionFirst(using("/Empty.md")))
                .isNull();

        assertThat(validateLatestVersionFirst(using("/NotLatestVersionFirst.md")))
                .isEqualTo(Failure.builder().rule(LATEST_VERSION_FIRST).message("Versions not sorted").line(3).column(1).build());

        assertThat(validateLatestVersionFirst(using("/UnsortedVersion.md")))
                .isEqualTo(Failure.builder().rule(LATEST_VERSION_FIRST).message("Versions not sorted").line(3).column(1).build());

        assertThat(validateLatestVersionFirst(using("/InvalidVersion.md")))
                .isEqualTo(Failure.builder().rule(LATEST_VERSION_FIRST).message("Versions not sorted").line(5).column(1).build());
    }
}
