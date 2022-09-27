package nbbrd.heylogs;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.RefNode;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@lombok.Value
@lombok.Builder
public class VersionFilter {

    public static final VersionFilter DEFAULT = VersionFilter.builder().build();
    @lombok.NonNull
    @lombok.Builder.Default
    String ref = "";

    @lombok.NonNull
    @lombok.Builder.Default
    Pattern unreleasedPattern = Pattern.compile("^.*-SNAPSHOT$");

    @lombok.NonNull
    @lombok.Builder.Default
    LocalDate from = LocalDate.MIN;

    @lombok.NonNull
    @lombok.Builder.Default
    LocalDate to = LocalDate.MAX;

    @lombok.Builder.Default
    int limit = Integer.MAX_VALUE;

    private boolean isUnreleasedPattern() {
        return unreleasedPattern.asPredicate().test(ref);
    }

    private boolean containsRef(Version version) {
        return (isUnreleasedPattern() && version.isUnreleased()) || version.getRef().contains(ref);
    }

    public boolean contains(Heading heading) {
        return contains(Version.parse(heading));
    }

    public boolean contains(Version version) {
        return containsRef(version)
                && from.compareTo(version.getDate()) <= 0
                && (to.isAfter(version.getDate()) || (to.equals(LocalDate.MAX) && version.isUnreleased()));
    }

    public void apply(Document root) {
        int found = 0;
        boolean keep = false;

        List<String> refNodes = new ArrayList<>();
        List<Reference> references = new ArrayList<>();

        for (Node current : root.getChildren()) {

            if (current instanceof Heading && Version.isVersionLevel((Heading) current)) {
                if (found >= getLimit() || !contains((Heading) current)) {
                    keep = false;
                } else {
                    found++;
                    keep = true;
                }
            }

            if (keep) {
                Nodes.of(RefNode.class)
                        .descendants(current)
                        .map(node -> node.getReference().toString())
                        .forEach(refNodes::add);
            } else {
                if (current instanceof Reference) {
                    references.add((Reference) current);
                } else {
                    current.unlink();
                }
            }
        }

        references
                .stream()
                .filter(reference -> !refNodes.contains(reference.getReference().toString()))
                .forEach(Node::unlink);
    }

    public static LocalDate parseLocalDate(CharSequence input) {
        try {
            return Year.parse(input).atDay(1);
        } catch (Exception ex1) {
            try {
                return YearMonth.parse(input).atDay(1);
            } catch (Exception ex2) {
                return LocalDate.parse(input);
            }
        }
    }
}
