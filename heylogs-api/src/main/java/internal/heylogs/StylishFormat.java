package internal.heylogs;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Status;
import nbbrd.heylogs.spi.Format;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.Locale.ROOT;

// https://eslint.org/docs/latest/user-guide/formatters/#stylish
@ServiceProvider
public final class StylishFormat implements Format {

    public static final String ID = "stylish";

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public void formatFailures(@NonNull Appendable appendable, @NonNull String source, @NonNull List<Failure> failures) throws IOException {
        appendable
                .append(source)
                .append(lineSeparator());

        int l = failures.stream().mapToInt(failure -> getNumberOfDigits(failure.getLine())).max().orElse(0);
        int c = failures.stream().mapToInt(failure -> getNumberOfDigits(failure.getColumn())).max().orElse(0);
        int m = failures.stream().mapToInt(failure -> failure.getMessage().length()).max().orElse(0);

        for (Failure x : failures) {
            appendable
                    .append(String.format(ROOT, "  %" + l + "d:%-" + c + "d  error  %-" + m + "s  %s", x.getLine(), x.getColumn(), x.getMessage(), x.getRuleId()))
                    .append(lineSeparator());
        }

        appendable.append(lineSeparator());
        switch (failures.size()) {
            case 0:
                appendable.append("  No problem");
                break;
            case 1:
                appendable.append("  1 problem");
                break;
            default:
                appendable.append(String.format(ROOT, "  %d problems", failures.size()));
                break;
        }
        appendable.append(lineSeparator());
    }

    @MightBePromoted
    private static int getNumberOfDigits(int number) {
        return (int) (Math.log10(number) + 1);
    }

    @Override
    public void formatStatus(@NonNull Appendable appendable, @NonNull String source, @NonNull Status status) throws IOException {
        appendable.append(source);
        appendable.append(lineSeparator());
        if (status.getReleaseCount() == 0) {
            appendable.append("  No release found");
            appendable.append(lineSeparator());
        } else {
            appendable.append(String.format(ROOT, "  Found %d releases", status.getReleaseCount()));
            appendable.append(lineSeparator());
            appendable.append(String.format(ROOT, "  Ranging from %s to %s", status.getTimeRange().getFrom(), status.getTimeRange().getTo()));
            appendable.append(lineSeparator());

            if (status.isCompatibleWithSemver()) {
                appendable.append("  Compatible with Semantic Versioning").append(status.getSemverDetails());
                appendable.append(lineSeparator());
            } else {
                appendable.append("  Not compatible with Semantic Versioning");
                appendable.append(lineSeparator());
            }
        }
        appendable.append(status.isHasUnreleasedSection() ? "  Has an unreleased version" : "  Has no unreleased version");
        appendable.append(lineSeparator());
    }
}
