package internal.heylogs;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.Nodes;
import nbbrd.heylogs.TypeOfChange;

import java.util.stream.Stream;

@lombok.Value(staticConstructor = "of")
public class TypeOfChangeHeading implements SectionHeading<TypeOfChange> {

    public static boolean isParsable(@NonNull Node node) {
        return node instanceof Heading && TypeOfChange.isTypeOfChangeLevel((Heading) node);
    }

    @StaticFactoryMethod
    public static @NonNull TypeOfChangeHeading parse(@NonNull Node node) {
        if (!(node instanceof Heading)) {
            throw new IllegalArgumentException("Node must be an instance of Heading");
        }
        return TypeOfChangeHeading.of(TypeOfChange.parse((Heading) node), (Heading) node);
    }

    @NonNull
    TypeOfChange section;

    @NonNull
    Heading heading;

    public @NonNull Stream<BulletListItem> getBulletListItems() {
        return Nodes
                .nextWhile(heading, TypeOfChangeHeading::isNotHeading)
                .filter(BulletList.class::isInstance)
                .map(BulletList.class::cast)
                .flatMap(Nodes.of(BulletListItem.class)::descendants);
    }

    private static boolean isNotHeading(Node next) {
        return !(next instanceof Heading);
    }
}
