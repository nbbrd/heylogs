package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.io.*;

public class Sample {

    public static final Parser PARSER = Parser.builder().build();
    public static final Formatter FORMATTER = Formatter.builder().build();

    public static Document using(String name) {
        try (InputStream stream = Sample.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing resource '" + name + "'");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                return PARSER.parseReader(reader);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Heading asHeading(String text) {
        return (Heading) PARSER.parse(text).getChildOfType(Heading.class);
    }

    public static String asText(Heading heading) {
        Document doc = new Document(null, BasedSequence.NULL);
        doc.appendChild(heading);
        return FORMATTER.render(doc).trim();
    }
}
