package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Document;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.function.Function;

import static nbbrd.heylogs.VersionFilter.builder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class VersionFilterTest {

    @Test
    public void testRef() {
        assertThat(builder().build())
                .describedAs("Empty reference")
                .is(containing(unreleased))
                .is(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(builder().ref("Unreleased").build())
                .describedAs("Full reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(builder().ref("1.1.0").build())
                .describedAs("Full reference")
                .isNot(containing(unreleased))
                .is(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(builder().ref("rel").build())
                .describedAs("Partial reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(builder().ref("1.").build())
                .describedAs("Partial reference")
                .isNot(containing(unreleased))
                .is(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(builder().ref("other").build())
                .describedAs("Unknown reference")
                .isNot(containing(unreleased))
                .isNot(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(builder().ref("other-SNAPSHOT").build())
                .describedAs("Matching unreleased pattern reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_0))
                .isNot(containing(v1_0_0));
    }

    @Test
    public void testTimeRange() {
        Function<TimeRange, VersionFilter> onTimeRange = o -> builder().timeRange(o).build();

        assertThat(TimeRange.ALL)
                .extracting(onTimeRange)
                .is(containing(unreleased))
                .is(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(TimeRange.of(v1_0_0.getDate(), v1_1_0.getDate()))
                .extracting(onTimeRange)
                .isNot(containing(unreleased))
                .is(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(TimeRange.of(v1_0_0.getDate(), v1_0_0.getDate()))
                .extracting(onTimeRange)
                .isNot(containing(unreleased))
                .isNot(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(TimeRange.of(LocalDate.MIN, v1_0_0.getDate()))
                .extracting(onTimeRange)
                .isNot(containing(unreleased))
                .isNot(containing(v1_1_0))
                .is(containing(v1_0_0));

        assertThat(TimeRange.of(v1_1_0.getDate(), v1_1_0.getDate()))
                .extracting(onTimeRange)
                .isNot(containing(unreleased))
                .is(containing(v1_1_0))
                .isNot(containing(v1_0_0));

        assertThat(TimeRange.of(v1_1_0.getDate(), LocalDate.MAX))
                .extracting(onTimeRange)
                .is(containing(unreleased))
                .is(containing(v1_1_0))
                .isNot(containing(v1_0_0));
    }


    @Test
    public void testApply() {
        Function<VersionFilter, String> usingMain = o -> {
            Document doc = Sample.using("Main.md");
            o.apply(doc);
            return Sample.FORMATTER.render(doc);
        };

        assertThat(builder().ref("1.1.0").build())
                .extracting(usingMain, STRING)
                .isEqualTo(
                        "## [1.1.0] - 2019-02-15\n" +
                                "\n" +
                                "### Added\n" +
                                "\n" +
                                "- Danish translation from [@frederikspang](https://github.com/frederikspang).\n" +
                                "- Georgian translation from [@tatocaster](https://github.com/tatocaster).\n" +
                                "- Changelog inconsistency section in Bad Practices\n" +
                                "\n" +
                                "### Changed\n" +
                                "\n" +
                                "- Fixed typos in Italian translation from [@lorenzo-arena](https://github.com/lorenzo-arena).\n" +
                                "- Fixed typos in Indonesian translation from [@ekojs](https://github.com/ekojs).\n" +
                                "\n" +
                                "[1.1.0]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...v1.1.0\n" +
                                "\n");

        assertThat(builder().ref("zzz").build())
                .extracting(usingMain, STRING)
                .isEmpty();
    }

    private static Condition<VersionFilter> containing(Version version) {
        return new Condition<>(parent -> parent.contains(version), "Must contain %s", version);
    }

    private final Version unreleased = Version.of("Unreleased", LocalDate.MAX);
    private final Version v1_1_0 = Version.of("1.1.0", LocalDate.parse("2019-02-15"));
    private final Version v1_0_0 = Version.of("1.0.0", LocalDate.parse("2017-06-20"));
}
