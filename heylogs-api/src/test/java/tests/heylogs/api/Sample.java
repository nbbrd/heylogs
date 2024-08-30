package tests.heylogs.api;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import internal.heylogs.FlexmarkIO;
import nbbrd.heylogs.*;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.io.function.IOConsumer;
import org.assertj.core.util.URLs;

import java.time.LocalDate;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nbbrd.heylogs.spi.RuleSeverity.ERROR;
import static nbbrd.io.function.IOFunction.unchecked;

public class Sample {

    public static Document using(String name) {
        return unchecked((String x) -> FlexmarkIO.newTextParser().parseResource(Sample.class, x, UTF_8))
                .apply(name);
    }

    public static Heading asHeading(String text) {
        return unchecked(FlexmarkIO.newTextParser()::parseChars)
                .andThen(doc -> (Heading) doc.getChildOfType(Heading.class))
                .apply(text);
    }

    public static String asText(Heading heading) {
        Document doc = new Document(null, BasedSequence.NULL);
        doc.appendChild(heading);
        return unchecked(FlexmarkIO.newTextFormatter()::formatToString)
                .andThen(String::trim)
                .apply(doc);
    }

    public static final Problem PROBLEM1 = Problem.builder().id("rule1").severity(ERROR).issue(RuleIssue.builder().message("boom").line(5).column(18).build()).build();
    public static final Problem PROBLEM2 = Problem.builder().id("rule222").severity(ERROR).issue(RuleIssue.builder().message("hello world").line(35).column(2).build()).build();

    public static final Check CHECK1 = Check.builder().source("source1").build();
    public static final Check CHECK2 = Check.builder().source("source2").problem(PROBLEM1).build();
    public static final Check CHECK3 = Check.builder().source("source3").problem(PROBLEM1).problem(PROBLEM2).build();

    public static final Summary SUMMARY_1 = Summary.builder().build();
    public static final Summary SUMMARY_2 = Summary
            .builder()
            .valid(true)
            .compatibility("Strange Versioning")
            .releaseCount(3)
            .unreleasedChanges(3)
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
        return URLs.contentOf(Objects.requireNonNull(anchor.getResource(resourceName)), UTF_8);
    }
}
