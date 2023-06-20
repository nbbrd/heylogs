package nbbrd.heylogs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static _test.Sample.using;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;

class ScannerTest {

    @Test
    void testScan() {
        Scanner x = Scanner.ofServiceLoader();

        assertThat(x.scan(using("/Empty.md")))
                .isEqualTo(new Status(
                        0,
                        TimeRange.ALL,
                        true, " ()",
                        true
                ));

        assertThat(x.scan(using("/Main.md")))
                .isEqualTo(new Status(
                        13,
                        TimeRange.of(LocalDate.of(2014, 5, 31), LocalDate.of(2019, 2, 15)),
                        true, " (1 MAJOR, 4 MINOR, 7 PATCH)",
                        true
                ));

        assertThat(x.scan(using("/InvalidSemver.md")))
                .isEqualTo(new Status(
                        2,
                        TimeRange.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)),
                        false, "",
                        true
                ));

        assertThat(x.scan(using("/InvalidVersion.md")))
                .isEqualTo(new Status(
                        1,
                        TimeRange.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)),
                        true, " ()",
                        true
                ));
    }

    @Test
    public void testFormatStatus() throws IOException {
        assertThatIOException()
                .isThrownBy(() -> Scanner.builder().build().formatStatus(new StringBuilder(), "", Status.builder().build()));

        assertThatIOException()
                .isThrownBy(() -> Scanner.ofServiceLoader().toBuilder().formatId("other").build().formatStatus(new StringBuilder(), "", Status.builder().build()));

        StringBuilder output = new StringBuilder();
        Scanner.ofServiceLoader().formatStatus(output, "file1", new Status(
                1,
                TimeRange.of(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)),
                true, " ()",
                true
        ));
        assertThat(output.toString())
                .isEqualToIgnoringNewLines(
                        "file1\n" +
                                "  Found 1 releases\n" +
                                "  Ranging from 2019-02-15 to 2019-02-15\n" +
                                "  Compatible with Semantic Versioning ()\n" +
                                "  Has an unreleased version\n"
                );
    }
}