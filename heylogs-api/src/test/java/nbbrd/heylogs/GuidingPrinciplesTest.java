package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static nbbrd.heylogs.GuidingPrinciples.validateForHumans;
import static nbbrd.heylogs.GuidingPrinciples.validateLatestVersionFirst;
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
                .contains("Missing Changelog");

        assertThat(validateForHumans(using("NoChangelog.md")))
                .contains("Invalid text");

        assertThat(validateForHumans(using("TooManyChangelog.md")))
                .contains("Too many Changelog");

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
                .contains("Invalid Heading node at line 2: Invalid date format", atIndex(0))
                .contains("Invalid Heading node at line 3: Missing date part", atIndex(1))
                .contains("Invalid Heading node at line 4: Missing ref link", atIndex(2))
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
                .contains("Invalid Heading node at line 7: Cannot parse 'Stuff'", atIndex(0))
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
                .contains("Invalid Heading node at line 5: Missing reference '1.1.0'", atIndex(0))
                .hasSize(1);
    }

    @Test
    public void testValidateLatestVersionFirst() {
        assertThat(validateLatestVersionFirst(using("Main.md")))
                .isNull();

        assertThat(validateLatestVersionFirst(using("Empty.md")))
                .isNull();

        assertThat(validateLatestVersionFirst(using("NotLatestVersionFirst.md")))
                .contains("not sorted");

        assertThat(validateLatestVersionFirst(using("UnsortedVersion.md")))
                .contains("not sorted");

        assertThat(validateLatestVersionFirst(using("InvalidVersion.md")))
                .contains("not sorted");
    }

}
