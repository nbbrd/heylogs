package nbbrd.heylogs.ext.gitlab;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabRepositoryLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitLabRepositoryLinkTest {

    @Test
    void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://gitlab.com/nbbrd/heylogs")));
        assertForgeLinkCompliance(parse(urlOf("https://gitlab.com/group/subgroup/heylogs")));
    }

    @Test
    void testValidProject() {
        GitLabRepositoryLink link = parse(urlOf("https://gitlab.com/group/subgroup/heylogs"));
        assertThat(link.getNamespace()).isEqualTo(Arrays.asList("group", "subgroup"));
        assertThat(link.getProject()).isEqualTo("heylogs");
        assertThat(link.toURL().toString()).isEqualTo("https://gitlab.com/group/subgroup/heylogs");
    }

    @Test
    void testInvalidProject() {
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://gitlab.com/heylogs")));
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://gitlab.com/group/hey logs")));
        assertThatIllegalArgumentException().isThrownBy(() -> parse(urlOf("https://gitlab.com/gr oup/heylogs")));
    }
}

