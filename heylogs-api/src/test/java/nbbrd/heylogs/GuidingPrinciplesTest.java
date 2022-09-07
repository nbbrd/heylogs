package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static nbbrd.heylogs.GuidingPrinciples.*;
import static nbbrd.heylogs.Nodes.of;
import static nbbrd.heylogs.Sample.using;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class GuidingPrinciplesTest {

    @Test
    public void testSample() {
        Node sample = using("Main.md");
        for (GuidingPrinciples rule : GuidingPrinciples.values()) {
            assertThat(of(Node.class).descendants(sample).map(rule::validate).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateForHumans() {
        assertThat(validateForHumans(using("Main.md")))
                .isNull();

        assertThat(validateForHumans(using("Empty.md")))
                .isEqualTo(Failure.of(FOR_HUMANS, "Missing Changelog heading", 1, 1));

        assertThat(validateForHumans(using("NoChangelog.md")))
                .isEqualTo(Failure.of(FOR_HUMANS, "Invalid text", 1, 1));

        assertThat(validateForHumans(using("TooManyChangelog.md")))
                .isEqualTo(Failure.of(FOR_HUMANS, "Too many Changelog headings", 1, 1));
    }

    @Test
    public void testValidateEntryForEveryVersions() {
        assertThat(of(Heading.class).descendants(using("Main.md")))
                .map(GuidingPrinciples::validateEntryForEveryVersions)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("InvalidVersion.md")))
                .map(GuidingPrinciples::validateEntryForEveryVersions)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(Failure.of(ENTRY_FOR_EVERY_VERSIONS, "Invalid date format", 2, 1), atIndex(0))
                .contains(Failure.of(ENTRY_FOR_EVERY_VERSIONS, "Missing date part", 3, 1), atIndex(1))
                .contains(Failure.of(ENTRY_FOR_EVERY_VERSIONS, "Missing ref link", 4, 1), atIndex(2))
                .hasSize(3);

    }

    @Test
    public void testValidateTypeOfChangesGrouped() {
        assertThat(of(Heading.class).descendants(using("Main.md")))
                .map(GuidingPrinciples::validateTypeOfChangesGrouped)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("InvalidTypeOfChange.md")))
                .map(GuidingPrinciples::validateTypeOfChangesGrouped)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(Failure.of(TYPE_OF_CHANGES_GROUPED, "Cannot parse 'Stuff'", 7, 1), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateLinkable() {
        assertThat(of(Heading.class).descendants(using("Main.md")))
                .map(GuidingPrinciples::validateLinkable)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(Heading.class).descendants(using("MissingReference.md")))
                .map(GuidingPrinciples::validateLinkable)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(Failure.of(LINKABLE, "Missing reference '1.1.0'", 5, 1), atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateLatestVersionFirst() {
        assertThat(validateLatestVersionFirst(using("Main.md")))
                .isNull();

        assertThat(validateLatestVersionFirst(using("Empty.md")))
                .isNull();

        assertThat(validateLatestVersionFirst(using("NotLatestVersionFirst.md")))
                .isEqualTo(Failure.of(LATEST_VERSION_FIRST, "Versions not sorted", 3, 1));

        assertThat(validateLatestVersionFirst(using("UnsortedVersion.md")))
                .isEqualTo(Failure.of(LATEST_VERSION_FIRST, "Versions not sorted", 3, 1));

        assertThat(validateLatestVersionFirst(using("InvalidVersion.md")))
                .isEqualTo(Failure.of(LATEST_VERSION_FIRST, "Versions not sorted", 5, 1));
    }
}
