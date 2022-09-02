package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.stream.Stream;

@lombok.AllArgsConstructor
public enum TypeOfChange implements BaseSection {

    ADDED("Added"),
    CHANGED("Changed"),
    DEPRECATED("Deprecated"),
    REMOVED("Removed"),
    FIXED("Fixed"),
    SECURITY("Security");

    private static final int HEADING_LEVEL = 3;

    @lombok.Getter
    final String label;

    @Override
    public Heading toHeading() {
        Heading result = new Heading();
        result.setOpeningMarker(BasedSequence.repeatOf("#", HEADING_LEVEL));
        result.setLevel(HEADING_LEVEL);
        result.appendChild(new Text(BasedSequence.of(label)));
        return result;
    }

    public static TypeOfChange parse(Heading heading) {
        if (!isTypeOfChangeLevel(heading)) {
            throw new IllegalArgumentException("Invalid heading level");
        }
        return Stream.of(values())
                .filter(value -> value.getLabel().contentEquals(heading.getText()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot parse '" + heading.getText() + "'"));
    }

    public static boolean isTypeOfChangeLevel(Heading heading) {
        return heading.getLevel() == HEADING_LEVEL;
    }
}
