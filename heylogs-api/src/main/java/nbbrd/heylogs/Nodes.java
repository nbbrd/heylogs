package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import lombok.NonNull;
import lombok.Value;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
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

    public static @NonNull Stream<Node> next(@NonNull Node seed, @NonNull Predicate<Node> hasNext) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(getNextNodeIterator(seed, hasNext), Spliterator.SIZED), false);
    }

    private static Iterator<Node> getNextNodeIterator(Node seed, Predicate<Node> hasNext) {
        return new Iterator<Node>() {
            Node current = seed;

            @Override
            public boolean hasNext() {
                Node next = current.getNext();
                return next != null && hasNext.test(next);
            }

            @Override
            public Node next() {
                return current = current.getNext();
            }
        };
    }
}
