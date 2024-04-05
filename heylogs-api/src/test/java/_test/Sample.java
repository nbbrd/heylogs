package _test;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import nbbrd.heylogs.*;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.io.function.IOConsumer;
import org.assertj.core.util.URLs;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

import static nbbrd.heylogs.spi.RuleSeverity.ERROR;

public class Sample {

    public static final Parser PARSER = Parser.builder().build();
    public static final Formatter FORMATTER = Formatter.builder().build();

    public static Document using(String name) {
        try (InputStream stream = Sample.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing resource '" + name + "'");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
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

    public static final Problem PROBLEM1 = Problem.builder().id("rule1").severity(ERROR).issue(RuleIssue.builder().message("boom").line(5).column(18).build()).build();
    public static final Problem PROBLEM2 = Problem.builder().id("rule222").severity(ERROR).issue(RuleIssue.builder().message("hello world").line(35).column(2).build()).build();

    public static final Check CHECK1 = Check.builder().source("source1").build();
    public static final Check CHECK2 = Check.builder().source("source2").problem(PROBLEM1).build();
    public static final Check CHECK3 = Check.builder().source("source3").problem(PROBLEM1).problem(PROBLEM2).build();

    public static final Summary SUMMARY_1 = Summary.builder().build();
    public static final Summary SUMMARY_2 = Summary
            .builder()
            .compatibility("Strange Versioning")
            .releaseCount(3)
            .hasUnreleasedSection(true)
            .timeRange(TimeRange.of(LocalDate.of(2010, 1, 1), LocalDate.of(2011, 1, 1)))
            .build();

    public static final Scan SCAN1 = Scan.builder().source("source1").summary(SUMMARY_1).build();
    public static final Scan SCAN2 = Scan.builder().source("source2").summary(SUMMARY_2).build();

    public static final Resource RESOURCE1 = Resource.builder().type("a").category("stuff").id("hello").name("(A) Hello").build();
    public static final Resource RESOURCE2 = Resource.builder().type("world").category("stuff").id("b").name("World (B)").build();

    //@MightBePromoted
    public static String writing(IOConsumer<? super Appendable> content) {
        StringBuilder result = new StringBuilder();
        content.asUnchecked().accept(result);
        return result.toString();
    }

    public static String contentOf(Class<?> anchor, String resourceName) {
        return URLs.contentOf(Objects.requireNonNull(anchor.getResource(resourceName)), StandardCharsets.UTF_8);
    }
}
