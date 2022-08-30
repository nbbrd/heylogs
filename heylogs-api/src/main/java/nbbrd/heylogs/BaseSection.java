package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import nbbrd.design.SealedType;

@SealedType({
        Changelog.class,
        Version.class,
        TypeOfChange.class
})
public interface BaseSection {

    Heading toHeading();
}
