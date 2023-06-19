package internal.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.spi.Format;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.Locale.ROOT;

// https://eslint.org/docs/latest/user-guide/formatters/#stylish
@ServiceProvider
public final class StylishFormat implements Format {

    @Override
    public @NonNull String getId() {
        return "stylish";
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

    private static int getNumberOfDigits(int number) {
        return (int) (Math.log10(number) + 1);
    }
}
