package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.LinkRef;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@lombok.Value
public class Version implements BaseSection {

    @lombok.NonNull
    String ref;

    boolean link;

    @lombok.NonNull
    LocalDate date;

    public boolean isUnreleased() {
        return "Unreleased".equals(ref);
    }

    @Override
    public Heading toHeading() {
        return null;
    }

    public static Version parse(Heading heading) {
        if (!isVersionLevel(heading)) {
            throw new IllegalArgumentException("Invalid heading level");
        }

        ReversiblePeekingIterator<Node> children = heading.getChildIterator();
        if (!children.hasNext()) {
            throw new IllegalArgumentException("Missing version");
        }

        Node version = children.next();
        BasedSequence versionText = version instanceof LinkRef ? version.getChildChars() : version.getChars();

        if (versionText.matchChars("Unreleased")) {
            return new Version("Unreleased", version instanceof LinkRef, LocalDate.MAX);
        }

        if (!children.hasNext()) {
            throw new IllegalArgumentException("Missing date");
        }

        Node date = children.next();
        BasedSequence dateText = date.getChars().trim();

        if (!dateText.startsWith("-")) {
            throw new IllegalArgumentException("Missing date prefix");
        }

        BasedSequence x = dateText.safeSubSequence(1).trim();

        try {
            return new Version(versionText.toString(), version instanceof LinkRef, LocalDate.parse(x));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date");
        }
    }

    public static boolean isVersionLevel(Heading heading) {
        return heading.getLevel() == 2;
    }
}
