package nbbrd.heylogs.ext.github;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubBlobLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;
class GitHubBlobLinkTest {
    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://github.com/JabRef/jabref/blob/v4.3.1/CHANGELOG.md")));
    }
    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing file path")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/blob/main")))
                .withMessage("GitHub blob link must have at least 5 path segments, found 4");
        assertThatIllegalArgumentException()
                .describedAs("invalid blob keyword")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/tree/main/CHANGELOG.md")))
                .withMessage("Invalid path item: expecting [blob], found 'tree'");
        assertThat(parse(urlOf("https://github.com/JabRef/jabref/blob/v4.3.1/CHANGELOG.md")))
                .returns(urlOf("https://github.com"), GitHubBlobLink::getBase)
                .returns("JabRef", GitHubBlobLink::getOwner)
                .returns("jabref", GitHubBlobLink::getRepo)
                .returns("v4.3.1", GitHubBlobLink::getBranchName)
                .returns(Arrays.asList("CHANGELOG.md"), GitHubBlobLink::getFilePath)
                .hasToString("https://github.com/JabRef/jabref/blob/v4.3.1/CHANGELOG.md");
        assertThat(parse(urlOf("https://github.com/nbbrd/jdplus-sdmx/blob/develop/CHANGELOG.md")))
                .returns("develop", GitHubBlobLink::getBranchName)
                .returns(Arrays.asList("CHANGELOG.md"), GitHubBlobLink::getFilePath)
                .hasToString("https://github.com/nbbrd/jdplus-sdmx/blob/develop/CHANGELOG.md");
        assertThat(parse(urlOf("https://github.com/nbbrd/jdplus-sdmx/blob/develop/docs/CHANGELOG.md")))
                .returns(Arrays.asList("docs", "CHANGELOG.md"), GitHubBlobLink::getFilePath)
                .hasToString("https://github.com/nbbrd/jdplus-sdmx/blob/develop/docs/CHANGELOG.md");
        assertThat(parse(urlOf("https://localhost:8080/nbbrd/heylogs/blob/main/CHANGELOG.md")))
                .returns(urlOf("https://localhost:8080"), GitHubBlobLink::getBase)
                .returns("nbbrd", GitHubBlobLink::getOwner)
                .returns("heylogs", GitHubBlobLink::getRepo)
                .returns("main", GitHubBlobLink::getBranchName)
                .returns(Arrays.asList("CHANGELOG.md"), GitHubBlobLink::getFilePath)
                .hasToString("https://localhost:8080/nbbrd/heylogs/blob/main/CHANGELOG.md");
    }
    @Test
    public void testToRef() {
        assertThat(parse(urlOf("https://github.com/nbbrd/heylogs/blob/main/CHANGELOG.md")).toRef(null))
                .isNull();
    }
}