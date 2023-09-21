package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.LinkRef;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.misc.CharPredicate;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.VisibleForTesting;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Iterator;

@lombok.Value(staticConstructor = "of")
@RepresentableAs(Heading.class)
public class Version implements BaseSection {

    private static final String UNRELEASED_KEYWORD = "unreleased";
    private static final int HEADING_LEVEL = 2;

    @VisibleForTesting
    static final char HYPHEN = '-';

    @VisibleForTesting
    static final char EN_DASH = '–';

    @VisibleForTesting
    static final char EM_DASH = '—';

    // The unicode en dash ("–") and em dash ("—") are also accepted as separators
    private static final CharPredicate VALID_SEPARATOR = CharPredicate.anyOf(HYPHEN, EN_DASH, EM_DASH);

    @lombok.NonNull
    String ref;

    char separator;

    @lombok.NonNull
    LocalDate date;

    public boolean isUnreleased() {
        return UNRELEASED_KEYWORD.equalsIgnoreCase(ref);
    }

    @Override
    public @NonNull Heading toHeading() {
        Heading result = new Heading();
        result.setOpeningMarker(BasedSequence.repeatOf("#", HEADING_LEVEL));
        result.setLevel(HEADING_LEVEL);

        LinkRef firstPart = new LinkRef();
        firstPart.setReferenceOpeningMarker(BasedSequence.of("["));
        firstPart.setReferenceClosingMarker(BasedSequence.of("]"));
        firstPart.setReference(BasedSequence.of(ref));
        result.appendChild(firstPart);

        if (!isUnreleased()) {
            Text secondPart = new Text();
            secondPart.setChars(BasedSequence.of(" " + separator + " ").append(date.toString()));
            result.appendChild(secondPart);
        }

        return result;
    }

    @StaticFactoryMethod
    public static @NonNull Version parse(@NonNull Heading heading) {
        if (!isVersionLevel(heading)) {
            throw new IllegalArgumentException("Invalid heading level");
        }

        Iterator<Node> parts = heading.getChildIterator();

        if (!parts.hasNext()) {
            throw new IllegalArgumentException("Missing ref part");
        }
        Node firstPart = parts.next();

        String ref = parseRef(firstPart);

        if (ref.equalsIgnoreCase(UNRELEASED_KEYWORD)) {
            if (parts.hasNext()) {
                throw new IllegalArgumentException("Unexpected additional part: '" + parts.next().getChars() + "'");
            }

            return new Version(ref, HYPHEN, LocalDate.MAX);
        }

        if (!parts.hasNext()) {
            throw new IllegalArgumentException("Missing date part");
        }
        Node secondPart = parts.next();

        char separator = parseSeparator(secondPart);
        LocalDate date = parseDate(secondPart);

        if (parts.hasNext()) {
            throw new IllegalArgumentException("Unexpected additional part: '" + parts.next().getChars() + "'");
        }

        return new Version(ref, separator, date);
    }

    private static String parseRef(Node firstPart) throws IllegalArgumentException {
        if (!(firstPart instanceof LinkRef)) {
            throw new IllegalArgumentException("Missing ref link");
        }
        return ((LinkRef) firstPart).getReference().toString();
    }

    private static char parseSeparator(Node secondPart) throws IllegalArgumentException {
        BasedSequence text = secondPart.getChars().trimStart();

        if (!text.startsWith(VALID_SEPARATOR)) {
            throw new IllegalArgumentException("Missing date prefix");
        }

        return text.charAt(0);
    }

    private static LocalDate parseDate(Node secondPart) throws IllegalArgumentException {
        BasedSequence date = secondPart.getChars();

        BasedSequence x = date.safeSubSequence(3).trim();

        try {
            return LocalDate.parse(x);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format");
        }
    }

    public static boolean isVersionLevel(Heading heading) {
        return heading.getLevel() == HEADING_LEVEL;
    }
}
