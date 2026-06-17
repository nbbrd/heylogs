package nbbrd.heylogs;

import com.vladsch.flexmark.ast.BulletList;
import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.util.ReferenceRepository;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import internal.heylogs.ChangelogHeading;
import internal.heylogs.FlexmarkIO;
import internal.heylogs.TypeOfChangeHeading;
import internal.heylogs.VersionHeading;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.Util.illegalArgumentToNull;

@lombok.Value
public class ChangelogContent {

    @NonNull String title;

    @Nullable String description;

    @NonNull List<VersionContent> versions;

    public static @NonNull ChangelogContent of(@NonNull Document document) {
        ChangelogHeading changelog = ChangelogHeading.root(document)
                .orElseThrow(() -> new IllegalArgumentException("No changelog heading found"));
        String title = changelog.getHeading().getText().toString();
        String description = extractDescription(changelog);
        List<VersionContent> versions = changelog.getVersions()
                .map(ChangelogContent::toVersionContent)
                .collect(toList());
        return new ChangelogContent(title, description, versions);
    }

    public @NonNull Document toDocument() {
        Document document = new Document(null, BasedSequence.NULL);

        document.appendChild(Changelog.INSTANCE.toHeading());

        if (description != null) {
            Document parsed = FlexmarkIO.newParser().parse(description + "\n");
            for (Node child = parsed.getFirstChild(); child != null; ) {
                Node next = child.getNext();
                document.appendChild(child);
                child = next;
            }
        }

        List<Reference> references = new ArrayList<>();
        for (VersionContent vc : versions) {
            Version v = vc.getVersion();
            document.appendChild(v.toHeading());
            for (TypeOfChangeContent tc : vc.getGroups()) {
                document.appendChild(tc.getTypeOfChange().toHeading());
                BulletList list = new BulletList();
                for (String item : tc.getItems()) {
                    list.appendChild(toBulletListItem(item));
                }
                document.appendChild(list);
            }
            if (v.getLink() != null) {
                references.add(VersionHeading.newReference(v, v.getLink()));
            }
        }

        references.forEach(document::appendChild);
        return document;
    }

    static @NonNull Optional<ChangelogContent> ofVersionsOnly(@NonNull Document document) {
        ReferenceRepository repository = Parser.REFERENCES.get(document);
        List<VersionContent> versions = Nodes
                .of(Heading.class)
                .descendants(document)
                .filter(VersionHeading::isParsable)
                .map(illegalArgumentToNull(node -> VersionHeading.parse(repository, node)))
                .filter(Objects::nonNull)
                .map(ChangelogContent::toVersionContent)
                .collect(toList());
        if (versions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ChangelogContent("Changelog", null, versions));
    }

    private static @Nullable String extractDescription(ChangelogHeading changelog) {
        StringBuilder sb = new StringBuilder();
        Node node = changelog.getHeading().getNext();
        while (node != null && !VersionHeading.isParsable(node)) {
            sb.append(node.getChars());
            node = node.getNext();
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private static VersionContent toVersionContent(VersionHeading vh) {
        List<TypeOfChangeContent> groups = vh.getTypeOfChanges()
                .map(ChangelogContent::toTypeOfChangeContent)
                .collect(toList());
        Version version = vh.getSection();
        if (version.getLink() == null) {
            try {
                URL url = vh.getURL();
                version = Version.of(version.getRef(), url, version.getSeparator(), version.getDate(), version.isYanked());
            } catch (IllegalArgumentException ignored) {
                // Reference URL unavailable — keep version as-is
            }
        }
        return new VersionContent(version, groups);
    }

    private static TypeOfChangeContent toTypeOfChangeContent(TypeOfChangeHeading toc) {
        List<String> items = toc.getBulletListItems()
                .map(item -> item.getChars().toString().trim().replaceFirst("^[-*+]\\s+", ""))
                .collect(toList());
        return new TypeOfChangeContent(toc.getSection(), items);
    }

    private static BulletListItem toBulletListItem(String rawMarkdown) {
        Document parsed = FlexmarkIO.newParser().parse("- " + rawMarkdown + "\n");
        BulletList bulletList = (BulletList) parsed.getChildOfType(BulletList.class);
        return (BulletListItem) bulletList.getFirstChild();
    }

    @lombok.Value
    public static class VersionContent {
        @NonNull Version version;
        @NonNull List<TypeOfChangeContent> groups;
    }

    @lombok.Value
    public static class TypeOfChangeContent {
        @NonNull TypeOfChange typeOfChange;
        @NonNull List<String> items;
    }
}




