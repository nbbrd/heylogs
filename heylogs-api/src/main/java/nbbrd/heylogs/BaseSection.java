package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import lombok.NonNull;
import nbbrd.design.SealedType;

@SealedType({
        Changelog.class,
        Version.class,
        TypeOfChange.class
})
public interface BaseSection {

    @NonNull Heading toHeading();
}
