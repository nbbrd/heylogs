package nbbrd.heylogs;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static _test.Sample.using;
import static org.assertj.core.api.Assertions.assertThat;

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
    }
}