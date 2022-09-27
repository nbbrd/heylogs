package nbbrd.heylogs;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionFilterTest {

    @Test
    public void testRef() {
        assertThat(VersionFilter.builder().build())
                .describedAs("Empty reference")
                .is(containing(unreleased))
                .is(containing(v1_1_1));

        assertThat(VersionFilter.builder().ref("Unreleased").build())
                .describedAs("Full reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_1));

        assertThat(VersionFilter.builder().ref("1.1.0").build())
                .describedAs("Full reference")
                .isNot(containing(unreleased))
                .is(containing(v1_1_1));

        assertThat(VersionFilter.builder().ref("rel").build())
                .describedAs("Partial reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_1));

        assertThat(VersionFilter.builder().ref("other").build())
                .describedAs("Unknown reference")
                .isNot(containing(unreleased))
                .isNot(containing(v1_1_1));

        assertThat(VersionFilter.builder().ref("other-SNAPSHOT").build())
                .describedAs("Matching unreleased pattern reference")
                .is(containing(unreleased))
                .isNot(containing(v1_1_1));
    }

    private static Condition<VersionFilter> containing(Version version) {
        return new Condition<>(parent -> parent.contains(version), "Must contain %s", version);
    }

    private final Version unreleased = new Version("Unreleased", LocalDate.MAX);
    private final Version v1_1_1 = new Version("1.1.0", LocalDate.parse("2019-02-15"));
}
