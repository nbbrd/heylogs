package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import nbbrd.design.RepresentableAs;

@RepresentableAs(Heading.class)
public enum Changelog implements BaseSection {
    INSTANCE;

    public static Changelog parse(Heading heading) {
        if (!isChangelogLevel(heading)) {
            throw new IllegalArgumentException("Invalid level");
        }
        if (!"Changelog".contentEquals(heading.getText())) {
            throw new IllegalArgumentException("Invalid text");
        }
        return INSTANCE;
    }

    @Override
    public Heading toHeading() {
        return new Heading(BasedSequence.of("Changelog"));
    }

    public static boolean isChangelogLevel(Heading heading) {
        return heading.getLevel() == 1;
    }
}
