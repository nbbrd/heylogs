package internal.heylogs;

import com.vladsch.flexmark.ast.Heading;
import lombok.NonNull;
import nbbrd.design.SealedType;
import nbbrd.heylogs.Section;

@SealedType({
        ChangelogHeading.class,
        VersionHeading.class,
        TypeOfChangeHeading.class
})
public interface SectionHeading<S extends Section> {

    @NonNull
    S getSection();

    @NonNull
    Heading getHeading();
}
