package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;

import java.util.stream.Stream;

@lombok.AllArgsConstructor
public enum TypeOfChange implements BaseSection {

    ADDED("Added"),
    CHANGED("Changed"),
    DEPRECATED("Deprecated"),
    REMOVED("Removed"),
    FIXED("Fixed"),
    SECURITY("Security");

    @lombok.Getter
    final String label;

    @Override
    public Heading toHeading() {
        return null;
    }

    public static TypeOfChange parse(Heading heading) {
        return Stream.of(values())
                .filter(value -> value.getLabel().contentEquals(heading.getText()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot parse " + heading.getText()));
    }

    public static boolean isTypeOfChangeLevel(Heading heading) {
        return heading.getLevel() == 3;
    }
}
