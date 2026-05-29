package nbbrd.heylogs.ext.forgejo;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.forgejo.ForgejoBlobLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;
class ForgejoBlobLinkTest {
    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/src/branch/master/CHANGELOG.md")));
    }
    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing file path")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/nbbrd/heylogs/src/branch/main")))
                .withMessage("Forgejo blob link must have at least 6 path segments, found 5");
        assertThatIllegalArgumentException()
                .describedAs("invalid ref type")
                .isThrownBy(() -> parse(urlOf("https://codeberg.org/nbbrd/heylogs/src/other/main/CHANGELOG.md")))
                .withMessage("Invalid path item: expecting [branch, tag, commit], found 'other'");
        assertThat(parse(urlOf("https://codeberg.org/Freeyourgadget/Gadgetbridge/src/branch/master/CHANGELOG.md")))
                .returns(urlOf("https://codeberg.org"), ForgejoBlobLink::getBase)
                .returns("Freeyourgadget", ForgejoBlobLink::getOwner)
                .returns("Gadgetbridge", ForgejoBlobLink::getRepo)
                .returns("branch", ForgejoBlobLink::getRefType)
                .returns("master", ForgejoBlobLink::getBranchName)
                .returns(Arrays.asList("CHANGELOG.md"), ForgejoBlobLink::getFilePath)
                .hasToString("https://codeberg.org/Freeyourgadget/Gadgetbridge/src/branch/master/CHANGELOG.md");
        assertThat(parse(urlOf("https://codeberg.org/nbbrd/heylogs/src/tag/v1.0.0/CHANGELOG.md")))
                .returns("tag", ForgejoBlobLink::getRefType)
                .returns("v1.0.0", ForgejoBlobLink::getBranchName)
                .returns(Arrays.asList("CHANGELOG.md"), ForgejoBlobLink::getFilePath)
                .hasToString("https://codeberg.org/nbbrd/heylogs/src/tag/v1.0.0/CHANGELOG.md");
        assertThat(parse(urlOf("https://codeberg.org/nbbrd/heylogs/src/commit/abc1234/docs/CHANGELOG.md")))
                .returns("commit", ForgejoBlobLink::getRefType)
                .returns("abc1234", ForgejoBlobLink::getBranchName)
                .returns(Arrays.asList("docs", "CHANGELOG.md"), ForgejoBlobLink::getFilePath)
                .hasToString("https://codeberg.org/nbbrd/heylogs/src/commit/abc1234/docs/CHANGELOG.md");
        assertThat(parse(urlOf("https://localhost:8080/nbbrd/heylogs/src/branch/main/CHANGELOG.md")))
                .returns(urlOf("https://localhost:8080"), ForgejoBlobLink::getBase)
                .returns("nbbrd", ForgejoBlobLink::getOwner)
                .returns("heylogs", ForgejoBlobLink::getRepo)
                .hasToString("https://localhost:8080/nbbrd/heylogs/src/branch/main/CHANGELOG.md");
    }
    @Test
    public void testToRef() {
        assertThat(parse(urlOf("https://codeberg.org/nbbrd/heylogs/src/branch/main/CHANGELOG.md")).toRef(null))
                .isNull();
    }
}