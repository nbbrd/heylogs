package nbbrd.heylogs.ext.gitlab;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabBlobLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;
class GitLabBlobLinkTest {
    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/blob/v1.0.0/CHANGELOG.md")));
    }
    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("too few segments")
                .isThrownBy(() -> parse(urlOf("https://gitlab.com/nbbrd/-/blob/main")))
                .withMessage("GitLab blob link must have at least 6 path segments");
        assertThat(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/blob/v1.0.0/CHANGELOG.md")))
                .returns(urlOf("https://gitlab.com"), GitLabBlobLink::getBase)
                .returns("heylogs-ext-gitlab", GitLabBlobLink::getProject)
                .returns("v1.0.0", GitLabBlobLink::getBranchName)
                .returns(Arrays.asList("CHANGELOG.md"), GitLabBlobLink::getFilePath)
                .hasToString("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/blob/v1.0.0/CHANGELOG.md");
        assertThat(parse(urlOf("https://gitlab.com/group/subgroup/project/-/blob/main/docs/CHANGELOG.md")))
                .describedAs("nested namespace")
                .returns("project", GitLabBlobLink::getProject)
                .returns("main", GitLabBlobLink::getBranchName)
                .returns(Arrays.asList("docs", "CHANGELOG.md"), GitLabBlobLink::getFilePath)
                .hasToString("https://gitlab.com/group/subgroup/project/-/blob/main/docs/CHANGELOG.md");
    }
    @Test
    public void testToRef() {
        assertThat(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/blob/v1.0.0/CHANGELOG.md")).toRef(null))
                .isNull();
    }
}