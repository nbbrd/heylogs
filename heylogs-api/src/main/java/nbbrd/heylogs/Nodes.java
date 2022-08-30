package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import lombok.Value;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Value(staticConstructor = "of")
public class Nodes<T extends Node> {

    @lombok.NonNull
    Class<T> type;

    public Stream<T> descendants(Node root) {
        return StreamSupport.stream(root.getDescendants().spliterator(), false)
                .filter(type::isInstance)
                .map(type::cast);
    }
}
