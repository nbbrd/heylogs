package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.FlexmarkIO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

public class ChangelogContentTest {

    @Test
    void shouldParseMainFixture() throws IOException {
        Document doc = FlexmarkIO.newTextParser().parseFile(
                Paths.get("src/test/resources/Main.md").toFile(), StandardCharsets.UTF_8);

        ChangelogContent content = ChangelogContent.of(doc);

        assertThat(content.getTitle()).isEqualTo("Changelog");
        assertThat(content.getDescription()).isNotNull();
        assertThat(content.getVersions()).hasSize(14);

        ChangelogContent.VersionContent unreleased = content.getVersions().get(0);
        assertThat(unreleased.getVersion().isUnreleased()).isTrue();
        assertThat(unreleased.getGroups()).isNotEmpty();

        ChangelogContent.TypeOfChangeContent firstGroup = unreleased.getGroups().get(0);
        assertThat(firstGroup.getTypeOfChange()).isEqualTo(TypeOfChange.ADDED);
        assertThat(firstGroup.getItems()).isNotEmpty();
        assertThat(firstGroup.getItems().get(0)).doesNotStartWith("-");
    }

    @Test
    void shouldReturnNullDescriptionWhenNoPreamble() throws IOException {
        Document doc = FlexmarkIO.newTextParser().parseFile(
                Paths.get("src/test/resources/FirstRelease.md").toFile(), StandardCharsets.UTF_8);

        ChangelogContent content = ChangelogContent.of(doc);

        assertThat(content.getDescription()).isNull();
    }

    @Test
    void shouldRoundTripThroughDocument() throws IOException {
        Document original = FlexmarkIO.newTextParser().parseFile(
                Paths.get("src/test/resources/Main.md").toFile(), StandardCharsets.UTF_8);

        ChangelogContent content = ChangelogContent.of(original);

        // Serialize to Markdown then re-parse to get a fully initialised AST
        String markdown = FlexmarkIO.newTextFormatter().formatToString(content.toDocument());
        Document reparsed = FlexmarkIO.newParser().parse(markdown);
        ChangelogContent roundTripped = ChangelogContent.of(reparsed);

        assertThat(roundTripped.getTitle()).isEqualTo(content.getTitle());
        assertThat(roundTripped.getVersions()).hasSameSizeAs(content.getVersions());

        for (int i = 0; i < content.getVersions().size(); i++) {
            ChangelogContent.VersionContent expected = content.getVersions().get(i);
            ChangelogContent.VersionContent actual = roundTripped.getVersions().get(i);
            assertThat(actual.getVersion().getRef()).isEqualTo(expected.getVersion().getRef());
            assertThat(actual.getGroups()).hasSameSizeAs(expected.getGroups());
        }
    }

    @Test
    void shouldThrowWhenNoChangelogHeading() {
        Document doc = FlexmarkIO.newParser().parse("## Some section\n\n- item\n");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> ChangelogContent.of(doc))
                .withMessage("No changelog heading found");
    }
}