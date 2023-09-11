package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.LinkRef;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Iterator;

@lombok.Value(staticConstructor = "of")
@RepresentableAs(Heading.class)
public class Version implements BaseSection {

    private static final String UNRELEASED_KEYWORD = "unreleased";
    private static final int HEADING_LEVEL = 2;

    @lombok.NonNull
    String ref;

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
            secondPart.setChars(BasedSequence.of(" - ").append(date.toString()));
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

        String ref = parseRef(parts.next());

        if (ref.equalsIgnoreCase(UNRELEASED_KEYWORD)) {
            if (parts.hasNext()) {
                throw new IllegalArgumentException("Unexpected additional part: '" + parts.next().getChars() + "'");
            }

            return new Version(ref, LocalDate.MAX);
        }

        if (!parts.hasNext()) {
            throw new IllegalArgumentException("Missing date part");
        }

        LocalDate date = parseDate(parts.next());

        if (parts.hasNext()) {
            throw new IllegalArgumentException("Unexpected additional part: '" + parts.next().getChars() + "'");
        }

        return new Version(ref, date);
    }

    private static String parseRef(Node firstPart) throws IllegalArgumentException {
        if (!(firstPart instanceof LinkRef)) {
            throw new IllegalArgumentException("Missing ref link");
        }
        return ((LinkRef) firstPart).getReference().toString();
    }

    private static LocalDate parseDate(Node secondPart) throws IllegalArgumentException {
        BasedSequence date = secondPart.getChars();

        BasedSequence trimmedStart = date.trimStart();
        // The unicode en dash ("–") and em dash ("—") are also accepted as separators
        if (!trimmedStart.startsWith("-") && !trimmedStart.startsWith("–") && !trimmedStart.startsWith("—")) {
            throw new IllegalArgumentException("Missing date prefix");
        }

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
