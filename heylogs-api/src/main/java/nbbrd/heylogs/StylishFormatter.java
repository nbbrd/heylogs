package nbbrd.heylogs;

import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.util.List;

import static java.lang.System.lineSeparator;

// https://eslint.org/docs/latest/user-guide/formatters/#stylish
@ServiceProvider
public final class StylishFormatter implements FailureFormatter {

    @Override
    public String getName() {
        return "stylish";
    }

    @Override
    public void format(Appendable appendable, String source, List<Failure> failures) throws IOException {
        appendable
                .append(source)
                .append(lineSeparator());

        int l = failures.stream().mapToInt(failure -> getNumberOfDigits(failure.getLine())).max().orElse(0);
        int c = failures.stream().mapToInt(failure -> getNumberOfDigits(failure.getColumn())).max().orElse(0);
        int m = failures.stream().mapToInt(failure -> failure.getMessage().length()).max().orElse(0);

        for (Failure failure : failures) {
            appendable
                    .append(String.format("  %" + l + "d:%-" + c + "d  error  %-" + m + "s  %s", failure.getLine(), failure.getColumn(), failure.getMessage(), failure.getRule()))
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
                appendable.append(String.format("  %d problems", failures.size()));
                break;
        }
        appendable.append(lineSeparator());
    }

    private static int getNumberOfDigits(int number) {
        return (int) (Math.log10(number) + 1);
    }
}
