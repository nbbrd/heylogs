package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabTagLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitLabTagLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/tags/v1.0.0")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing tag value")
                .isThrownBy(() -> parse(urlOf("https://gitlab.com/nbbrd/-/tags")))
                .withMessage("GitLab tags number link must have at least 4 path segments");

        assertThat(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/tags/v1.0.0")))
                .returns(urlOf("https://gitlab.com"), GitLabTagLink::getBase)
                .returns("heylogs-ext-gitlab", GitLabTagLink::getProject)
                .returns("v1.0.0", GitLabTagLink::getBranchName)
                .hasToString("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/tags/v1.0.0");

        assertThat(parse(urlOf("https://gitlab.com/group/subgroup/project/-/tags/v2.0.0")))
                .describedAs("nested namespace")
                .returns("project", GitLabTagLink::getProject)
                .returns("v2.0.0", GitLabTagLink::getBranchName)
                .hasToString("https://gitlab.com/group/subgroup/project/-/tags/v2.0.0");
    }

    @Test
    public void testToRef() {
        assertThat(parse(urlOf("https://gitlab.com/nbbrd/heylogs-ext-gitlab/-/tags/v1.0.0")).toRef(null))
                .isNull();
    }
}
