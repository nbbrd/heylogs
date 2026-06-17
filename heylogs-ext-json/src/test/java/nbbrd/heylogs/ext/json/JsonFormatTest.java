package nbbrd.heylogs.ext.json;

import nbbrd.heylogs.ChangelogContent;
import nbbrd.heylogs.TypeOfChange;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.api.Sample.*;
import static tests.heylogs.spi.FormatAssert.assertFormatCompliance;

class JsonFormatTest {

    @Test
    public void testCompliance() {
        assertFormatCompliance(new JsonFormat());
    }

    @Test
    public void testGetSupportedFormatTypes() {
        Format x = new JsonFormat();

        assertThat(x.getSupportedFormatTypes())
                .containsExactlyInAnyOrder(FormatType.values());
    }

    @Test
    public void testFormatProblems() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK1))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/check1.json"));

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK2))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/check2.json"));

        assertThat(writing(appendable -> x.formatProblems(appendable, singletonList(CHECK3))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/check3.json"));
    }

    @Test
    public void testFormatStatus() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatStatus(appendable, singletonList(SCAN1))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/scan1.json"));

        assertThat(writing(appendable -> x.formatStatus(appendable, singletonList(SCAN2))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/scan2.json"));
    }

    @Test
    public void testFormatResource() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatResources(appendable, emptyList())))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/resource1.json"));

        assertThat(writing(appendable -> x.formatResources(appendable, singletonList(RESOURCE1))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/resource2.json"));

        assertThat(writing(appendable -> x.formatResources(appendable, asList(RESOURCE1, RESOURCE2))))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/resource3.json"));
    }

    @Test
    public void testFormatContent() {
        Format x = new JsonFormat();

        assertThat(writing(appendable -> x.formatContent(appendable, CONTENT1)))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/content1.json"));

        assertThat(writing(appendable -> x.formatContent(appendable, CONTENT2)))
                .isEqualToNormalizingNewlines(contentOf(JsonFormatTest.class, "/content2.json"));
    }

    @Test
    public void testParseContent() throws IOException {
        Format x = new JsonFormat();

        try (StringReader reader = new StringReader(contentOf(JsonFormatTest.class, "/content1.json"))) {
            assertThat(x.parseContent(reader)).isEqualTo(CONTENT1);
        }

        try (StringReader reader = new StringReader(contentOf(JsonFormatTest.class, "/content2.json"))) {
            assertThat(x.parseContent(reader)).isEqualTo(CONTENT2);
        }
    }

    @Test
    public void testParseClparseContent() throws IOException {
        Format x = new JsonFormat();

        try (StringReader reader = new StringReader(contentOf(JsonFormatTest.class, "/clparse.json"))) {
            ChangelogContent content = x.parseContent(reader);
            assertThat(content.getTitle()).isEqualTo("Changelog");
            assertThat(content.getVersions()).hasSize(3);

            ChangelogContent.VersionContent unreleased = content.getVersions().get(0);
            assertThat(unreleased.getVersion().isUnreleased()).isTrue();
            assertThat(unreleased.getVersion().getLink()).isNotNull();
            assertThat(unreleased.getGroups()).hasSize(1);
            assertThat(unreleased.getGroups().get(0).getTypeOfChange()).isEqualTo(TypeOfChange.FIXED);
            assertThat(unreleased.getGroups().get(0).getItems()).containsExactly("Fix a bug that caused undefined behavior");

            ChangelogContent.VersionContent v100 = content.getVersions().get(1);
            assertThat(v100.getVersion().getRef()).isEqualTo("1.0.0");
            assertThat(v100.getGroups()).hasSize(2);

            ChangelogContent.VersionContent v001 = content.getVersions().get(2);
            assertThat(v001.getVersion().getRef()).isEqualTo("0.0.1");
        }
    }

    @Test
    public void testClparseRoundtrip() throws IOException {
        Format x = new JsonFormat();

        String originalJson = contentOf(JsonFormatTest.class, "/clparse.json");
        ChangelogContent content;
        try (StringReader reader = new StringReader(originalJson)) {
            content = x.parseContent(reader);
        }
        assertThat(writing(appendable -> x.formatContent(appendable, content)))
                .isEqualToNormalizingNewlines(originalJson);
    }

}