package internal.heylogs;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import nbbrd.io.text.TextFormatter;
import nbbrd.io.text.TextParser;

public final class FlexmarkIO {

    private FlexmarkIO() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Parser newParser() {
        return Parser.builder().build();
    }

    public static TextParser<Document> newTextParser() {
        return TextParser.onParsingReader(newParser()::parseReader);
    }

    public static Formatter newFormatter() {
        Formatter.Builder result = Formatter.builder();
        result.set(Formatter.MAX_TRAILING_BLANK_LINES, 0);
        return result.build();
    }

    public static TextFormatter<Document> newTextFormatter() {
        return TextFormatter.onFormattingWriter(newFormatter()::render);
    }
}
