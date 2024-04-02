package nbbrd.heylogs;

import internal.heylogs.SemverRule;
import nbbrd.heylogs.spi.Rule;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static _test.Sample.using;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static nbbrd.heylogs.spi.RuleSeverity.ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

public class CheckerTest {

    @Test
    public void testFactories() {
        assertThat(Checker.builder().build())
                .returns(0, checker -> checker.getRules().size())
                .returns(0, checker -> checker.getFormats().size());

        assertThat(Checker.ofServiceLoader())
                .extracting(Checker::getRules, list(Rule.class))
                .hasSizeGreaterThan(1)
                .map(Rule::getId)
                .doesNotContain("semver");

        assertThat(Checker.ofServiceLoader().toBuilder().rule(new SemverRule()).build())
                .extracting(Checker::getRules, list(Rule.class))
                .hasSizeGreaterThan(1)
                .map(Rule::getId)
                .contains("semver");
    }

    @Test
    public void testValidate() {
        assertThat(Checker.builder().build().validate(using("/InvalidVersion.md")))
                .isEmpty();

        assertThat(Checker.ofServiceLoader().validate(using("/InvalidVersion.md")))
                .isNotEmpty();
    }

    @Test
    public void testFormatFailures() throws IOException {
        assertThatIOException()
                .isThrownBy(() -> Checker.builder().build().formatFailures(new StringBuilder(), "", emptyList()));

        assertThatIOException()
                .isThrownBy(() -> Checker.ofServiceLoader().toBuilder().formatId("other").build().formatFailures(new StringBuilder(), "", emptyList()));

        StringBuilder output = new StringBuilder();
        Checker.ofServiceLoader().formatFailures(output, "file1", asList(Failure.builder().ruleId("rule1").ruleSeverity(ERROR).message("some message").line(10).column(20).build()));
        assertThat(output.toString())
                .isEqualToIgnoringNewLines(
                        "file1\n" +
                                "  10:20  error  some message  rule1\n" +
                                "  \n" +
                                "  1 problem\n"
                );
    }
}
