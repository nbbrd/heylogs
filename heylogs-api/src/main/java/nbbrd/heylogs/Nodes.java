package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import lombok.Value;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Stream.concat;

@Value(staticConstructor = "of")
public class Nodes<T extends Node> {

    @lombok.NonNull
    Class<T> type;

    public @NonNull Stream<T> descendants(@NonNull Node root) {
        return StreamSupport.stream(root.getDescendants().spliterator(), false)
                .filter(type::isInstance)
                .map(type::cast);
    }

    public static @NonNull Stream<Node> walk(@NonNull Node root) {
        return concat(Stream.of(root), Nodes.of(Node.class).descendants(root));
    }
}
