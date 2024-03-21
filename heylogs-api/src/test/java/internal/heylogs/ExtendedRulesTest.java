package internal.heylogs;

import _test.Sample;
import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.ast.Node;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.spi.Rule;
import nbbrd.service.ServiceId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.regex.Pattern;

import static _test.Sample.using;
import static internal.heylogs.ExtendedRules.*;
import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

public class ExtendedRulesTest {

    @Test
    public void testIdPattern() {
        assertThat(ExtendedRules.values())
                .extracting(Rule::getId)
                .allMatch(Pattern.compile(ServiceId.KEBAB_CASE).asPredicate());
    }

    @Test
    public void test() {
        Node sample = Sample.using("/Main.md");
        for (ExtendedRules rule : ExtendedRules.values()) {
            Assertions.assertThat(Nodes.of(Node.class).descendants(sample).map(rule::validate).filter(Objects::nonNull))
                    .isEmpty();
        }
    }

    @Test
    public void testValidateHttps() {
        assertThat(of(LinkNodeBase.class).descendants(using("/Main.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .isEmpty();

        assertThat(of(LinkNodeBase.class).descendants(using("/NonHttps.md")))
                .map(ExtendedRules::validateHttps)
                .isNotEmpty()
                .filteredOn(Objects::nonNull)
                .contains(Failure.builder().rule(HTTPS).message("Expecting HTTPS protocol").line(1).column(1).build(), atIndex(0))
                .contains(Failure.builder().rule(HTTPS).message("Expecting HTTPS protocol").line(2).column(7).build(), atIndex(1))
                .hasSize(2); // FIXME: should be 3
    }

    @Test
    public void testValidateConsistentSeparator() {
        assertThat(validateConsistentSeparator(using("/ErraticSeparator.md")))
                .isEqualTo(Failure.builder().rule(CONSISTENT_SEPARATOR).message("Expecting consistent version-date separator \\u002d, found [\\u002d, \\u2013, \\u2014]").line(1).column(1).build());

        assertThat(validateConsistentSeparator(using("/NonDefaultSeparator.md")))
                .isEqualTo(NO_PROBLEM);
    }
}
