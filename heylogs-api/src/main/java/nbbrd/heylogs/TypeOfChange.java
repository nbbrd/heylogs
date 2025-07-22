package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;

import java.util.stream.Stream;

@lombok.Getter
@lombok.AllArgsConstructor
@RepresentableAs(Heading.class)
public enum TypeOfChange implements Section {

    ADDED("Added"),
    CHANGED("Changed"),
    DEPRECATED("Deprecated"),
    REMOVED("Removed"),
    FIXED("Fixed"),
    SECURITY("Security");

    private static final int HEADING_LEVEL = 3;

    final String label;

    @Override
    public @NonNull Heading toHeading() {
        Heading result = new Heading();
        result.setOpeningMarker(BasedSequence.repeatOf("#", HEADING_LEVEL));
        result.setLevel(HEADING_LEVEL);
        result.appendChild(new Text(BasedSequence.of(label)));
        return result;
    }

    @StaticFactoryMethod
    public static @NonNull TypeOfChange parse(@NonNull Heading heading) {
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
