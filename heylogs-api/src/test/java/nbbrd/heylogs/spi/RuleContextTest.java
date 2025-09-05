package nbbrd.heylogs.spi;

import internal.heylogs.base.PrefixTagging;
import nbbrd.heylogs.Config;
import org.junit.jupiter.api.Test;
import tests.heylogs.spi.MockedCompareLink;
import tests.heylogs.spi.MockedForgeLink;

import static internal.heylogs.base.BaseVersionings.REGEX_VERSIONING;
import static internal.heylogs.spi.URLExtractor.urlOf;
import static org.assertj.core.api.Assertions.assertThat;

class RuleContextTest {

    @Test
    public void testFindAllForges() {
        RuleContext baseContext = RuleContext
                .builder()
                .forge(ForgeSupport
                        .builder()
                        .id("abc").name("").moduleId("")
                        .compareLinkFactory(MockedCompareLink::parse)
                        .knownHostPredicate(url -> url.getHost().contains("github"))
                        .linkParser(ForgeRefType.ISSUE, MockedForgeLink::parse)
                        .build())
                .forge(ForgeSupport
                        .builder()
                        .id("other").name("").moduleId("")
                        .compareLinkFactory(MockedCompareLink::parse)
                        .knownHostPredicate(url -> url.getHost().contains("other"))
                        .linkParser(ForgeRefType.ISSUE, MockedForgeLink::parse)
                        .build())
                .versioning(REGEX_VERSIONING)
                .tagging(new PrefixTagging())
                .build();

        assertThat(baseContext.findAllForges(urlOf("http://github.com/")))
                .extracting(Forge::getForgeId)
                .containsExactly("abc");

        assertThat(baseContext.findAllForges(urlOf("http://unknown.com/")))
                .isEmpty();

        assertThat(baseContext.withConfig(Config.builder().forgeOf("abc").build()).findAllForges(urlOf("http://unknown.com/")))
                .extracting(Forge::getForgeId)
                .containsExactly("abc");

        assertThat(baseContext.withConfig(Config.builder().domainOf("unknown:abc").build()).findAllForges(urlOf("http://unknown.com/")))
                .extracting(Forge::getForgeId)
                .containsExactly("abc");

        assertThat(baseContext.withConfig(Config.builder().domainOf("unknown.com:abc").build()).findAllForges(urlOf("http://unknown.com/")))
                .extracting(Forge::getForgeId)
                .containsExactly("abc");
    }
}