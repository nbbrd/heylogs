package internal.heylogs;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;

public final class FlexmarkIO {

    private FlexmarkIO() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Parser newParser() {
        return Parser.builder().build();
    }

    public static Formatter newFormatter() {
        Formatter.Builder result = Formatter.builder();
        result.set(Formatter.MAX_TRAILING_BLANK_LINES, 0);
        return result.build();
    }
}
