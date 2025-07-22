package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;

@RepresentableAs(Heading.class)
public enum Changelog implements Section {

    INSTANCE;

    private static final int HEADING_LEVEL = 1;

    @StaticFactoryMethod
    public static @NonNull Changelog parse(@NonNull Heading heading) {
        if (!isChangelogLevel(heading)) {
            throw new IllegalArgumentException("Invalid heading level");
        }
        if (!"Changelog".contentEquals(heading.getText())) {
            throw new IllegalArgumentException("Invalid text");
        }
        return INSTANCE;
    }

    @Override
    public @NonNull Heading toHeading() {
        Heading result = new Heading();
        result.setOpeningMarker(BasedSequence.repeatOf("#", HEADING_LEVEL));
        result.setLevel(HEADING_LEVEL);
        result.appendChild(new Text(BasedSequence.of("Changelog")));
        return result;
    }

    public static boolean isChangelogLevel(Heading heading) {
        return heading.getLevel() == HEADING_LEVEL;
    }
}
