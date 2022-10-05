package nbbrd.heylogs;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static nbbrd.heylogs.Sample.using;
import static org.assertj.core.api.Assertions.assertThat;

class ScanTest {

    @Test
    void of() {
        assertThat(Scan.of(using("Empty.md")))
                .isEqualTo(new Scan(
                        0,
                        Scan.TimeRange.NONE,
                        new Scan.SemverSummary(true, " ()"),
                        true
                ));

        assertThat(Scan.of(using("Main.md")))
                .isEqualTo(new Scan(
                        13,
                        new Scan.TimeRange(LocalDate.of(2014, 5, 31), LocalDate.of(2019, 2, 15)),
                        new Scan.SemverSummary(true, " (1 MAJOR, 4 MINOR, 7 PATCH)"),
                        true
                ));

        assertThat(Scan.of(using("InvalidSemver.md")))
                .isEqualTo(new Scan(
                        2,
                        new Scan.TimeRange(LocalDate.of(2019, 2, 15), LocalDate.of(2019, 2, 15)),
                        new Scan.SemverSummary(false, ""),
                        true
                ));
    }
}