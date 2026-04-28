package nbbrd.heylogs;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.ReferenceNode;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import internal.heylogs.ChangelogHeading;
import internal.heylogs.FlexmarkIO;
import internal.heylogs.TypeOfChangeHeading;
import internal.heylogs.VersionHeading;
import internal.heylogs.base.GuidingPrinciples;
import nbbrd.heylogs.spi.URLExtractor;
import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.*;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static nbbrd.heylogs.TimeRange.toTimeRange;
import static nbbrd.heylogs.Util.illegalArgumentToNull;
import static nbbrd.heylogs.spi.ForgeSupport.*;
import static nbbrd.heylogs.spi.FormatSupport.onFormatFileFilter;
import static nbbrd.heylogs.spi.FormatSupport.onFormatId;
import static nbbrd.heylogs.spi.Versioning.NO_VERSIONING_FILTER;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class Heylogs {

    @StaticFactoryMethod
    public static @NonNull Heylogs ofServiceLoader() {
        return Heylogs
                .builder()
                .rules(RuleLoader.load())
                .formats(FormatLoader.load())
                .versionings(VersioningLoader.load())
                .forges(ForgeLoader.load())
                .taggings(TaggingLoader.load())
                .httpFactory(HttpFactoryLoader.load())
                .build();
    }

    @NonNull
    @lombok.Singular
    List<Rule> rules;

    @NonNull
    @lombok.Singular
    List<Format> formats;

    @NonNull
    @lombok.Singular
    List<Versioning> versionings;

    @NonNull
    @lombok.Singular
    List<Forge> forges;

    @NonNull
    @lombok.Singular
    List<Tagging> taggings;

    @NonNull
    @lombok.Builder.Default
    HttpFactory httpFactory = HttpFactory.noOp();

    @lombok.Builder.Default
    boolean validateAfterModification = false;

    public @NonNull List<Problem> check(@NonNull Document document, @NonNull Config config) {
        checkConfig(config);

        return problemStreamOf(document, rules, initContext(config)).collect(toList());
    }

    public void extract(@NonNull Document document, @NonNull Filter filter) {
        if (isNotValidAgainstGuidingPrinciples(document)) {
            throw new IllegalArgumentException("Invalid changelog");
        }

        int found = 0;
        boolean keep = false;

        List<String> refNodes = new ArrayList<>();
        List<Reference> references = new ArrayList<>();

        for (Node current : document.getChildren()) {

            boolean versionHeading = current instanceof Heading
                    && Version.isVersionLevel((Heading) current);

            if (versionHeading) {
                if (found >= filter.getLimit() || !filter.contains(Version.parse((Heading) current))) {
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
                if (versionHeading && filter.isIgnoreContent()) {
                    keep = false;
                }
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

    public @NonNull List<Resource> list() {
        return concat(
                rules.stream().map(Resource::of),
                formats.stream().map(Resource::of),
                versionings.stream().map(Resource::of),
                forges.stream().map(Resource::of),
                taggings.stream().map(Resource::of)
        ).sorted(Resource.DEFAULT_COMPARATOR).collect(toList());
    }

    public void release(@NonNull Document document, @NonNull Version newVersion, @NonNull Config config) throws IllegalArgumentException {
        checkConfig(config);

        Predicate<CharSequence> versioningPredicate = getVersioningPredicate(config.getVersioning());
        if (versioningPredicate != null && !versioningPredicate.test(newVersion.getRef())) {
            throw new IllegalArgumentException("Invalid version '" + newVersion.getRef() + "' for versioning '" + config.getVersioning() + "'");
        }

        if (isNotValidAgainstGuidingPrinciples(document)) {
            throw new IllegalArgumentException("Invalid changelog");
        }

        ChangelogHeading changelog = ChangelogHeading.root(document)
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate changelog header"));

        VersionHeading unreleased = changelog.getVersions()
                .filter(versionNode -> versionNode.getSection().isUnreleased())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate unreleased header"));

        Forge forge = config.getForge() != null
                ? findForge(onForgeConfig(config.getForge())).orElseThrow(() -> new IllegalArgumentException("Cannot find forge with id '" + config.getForge().getId() + "'"))
                : findForge(onHost(unreleased.getURL(), config.getDomains())).orElseThrow(() -> new IllegalArgumentException("Cannot determine forge"));

        CompareLink originalLink = findCompareLink(forge, unreleased.getURL())
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine compare link"));

        String releaseTag = getTag(config.getTagging(), newVersion.getRef());
        CompareLink releaseLink = originalLink.derive(releaseTag);
        VersionHeading release = VersionHeading.of(newVersion, releaseLink.toURL());

        CompareLink updatedLink = releaseLink.derive("HEAD");
        VersionHeading updated = VersionHeading.of(unreleased.getSection(), updatedLink.toURL());

        changelog.getRepository().putRawKey(release.getReference().getReference(), release.getReference());
        changelog.getRepository().putRawKey(updated.getReference().getReference(), updated.getReference());

        unreleased.getHeading().appendChild(release.getHeading());
        unreleased.getReference().insertAfter(release.getReference());
        unreleased.getReference().insertBefore(updated.getReference());
        unreleased.getReference().unlink();

        validateAfterModification(document, config);
    }

    public @NonNull Document init(@NonNull Config config, @Nullable String template, @Nullable URL projectUrl) {
        checkConfig(config);

        Map<String, Object> context = new HashMap<>();

        if (config.getVersioning() != null) {
            Versioning versioning = findVersioning(config.getVersioning()::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find versioning with id '" + config.getVersioning().getId() + "'"));
            Map<String, Object> versioningContext = new HashMap<>();
            versioningContext.put("id", config.getVersioning().getId());
            versioningContext.put("arg", config.getVersioning().getArg());
            versioningContext.put("name", versioning.getVersioningName());
            versioningContext.put("url", versioning.getVersioningUrl().toString());
            context.put("versioning", versioningContext);
        }

        if (config.getTagging() != null) {
            Map<String, Object> taggingContext = new HashMap<>();
            taggingContext.put("id", config.getTagging().getId());
            taggingContext.put("arg", config.getTagging().getArg());
            context.put("tagging", taggingContext);
        }

        if (config.getForge() != null) {
            Map<String, Object> forgeContext = new HashMap<>();
            forgeContext.put("id", config.getForge().getId());
            context.put("forge", forgeContext);
        }

        List<Map<String, Object>> rulesContext = new ArrayList<>();
        for (RuleConfig rule : config.getRules()) {
            Map<String, Object> ruleMap = new HashMap<>();
            ruleMap.put("id", rule.getId());
            ruleMap.put("severity", rule.getSeverity() != null ? rule.getSeverity().toString() : null);
            rulesContext.add(ruleMap);
        }
        context.put("rules", rulesContext);

        List<Map<String, Object>> domainsContext = new ArrayList<>();
        for (DomainConfig domain : config.getDomains()) {
            Map<String, Object> domainMap = new HashMap<>();
            domainMap.put("domain", domain.getDomain());
            domainMap.put("forgeId", domain.getForgeId());
            domainsContext.add(domainMap);
        }
        context.put("domains", domainsContext);

        URL effectiveProjectUrl;
        effectiveProjectUrl = projectUrl == null ? DEFAULT_PROJECT_URL : projectUrl;
        context.put("projectUrl", effectiveProjectUrl.toString());

        DefaultMustacheFactory factory = new DefaultMustacheFactory();
        Mustache mustache = template != null
                ? factory.compile(new StringReader(template), "custom")
                : factory.compile("internal/heylogs/init.mustache");

        try {
            StringWriter writer = new StringWriter();
            mustache.execute(writer, context).flush();
            return FlexmarkIO.newParser().parse(writer.toString());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to render init template", ex);
        }
    }

    public void yank(@NonNull Document document, @NonNull String ref, @NonNull Config config) throws IllegalArgumentException {
        if (ref.isEmpty()) {
            throw new IllegalArgumentException("Ref must not be empty");
        }

        ChangelogHeading changelog = ChangelogHeading.root(document)
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate changelog header"));

        VersionHeading target = changelog.getVersions()
                .filter(versionNode -> ref.equalsIgnoreCase(versionNode.getSection().getRef()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate version '" + ref + "'"));

        Version version = target.getSection();

        if (version.isUnreleased()) {
            throw new IllegalArgumentException("Cannot yank unreleased version");
        }

        if (version.isYanked()) {
            throw new IllegalArgumentException("Version '" + ref + "' is already yanked");
        }

        // Create a new heading with yanked=true and replace the old heading
        Version yankedVersion = Version.of(version.getRef(), version.getLink(), version.getSeparator(), version.getDate(), true);
        Heading newHeading = yankedVersion.toHeading();
        target.getHeading().insertBefore(newHeading);
        target.getHeading().unlink();

        validateAfterModification(document, config);
    }

    public void fetch(@NonNull Document document, @NonNull TypeOfChange typeOfChange, @NonNull String id, @NonNull Config config) throws IOException {
        URL url = illegalArgumentToNull(URLExtractor::urlOf).apply(id);
        String message = url != null
                ? fetchMessageByUrl(url)
                : fetchMessageByRef(document, id, config);
        push(document, typeOfChange, message, config);
    }

    private @NonNull String fetchMessageByUrl(@NonNull URL url) throws IOException {
        Forge forge = forges.stream()
                .filter(item -> item.isKnownHost(url))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No forge found for URL: " + url));

        for (ForgeLinkType type : ForgeLinkType.values()) {
            MessageFetcher fetcher = forge.getMessageFetcher(type);
            if (fetcher != null) {
                ForgeLinkParser linkParser = forge.getLinkParser(type);
                if (linkParser != null) {
                    ForgeLink link = linkParser.parseForgeLinkOrNull(url);
                    if (link != null) {
                        return fetcher.fetchMessage(httpFactory.getClient(), link) + toMarkdown(link);
                    }
                }
            }
        }

        throw new IllegalArgumentException("Cannot resolve url '" + url + "' on forge '" + forge.getForgeName() + "'");
    }

    private @NonNull String fetchMessageByRef(@NonNull Document document, @NonNull String ref, @NonNull Config config) throws IOException {
        ChangelogHeading changelog = ChangelogHeading.root(document)
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate changelog header"));

        List<VersionHeading> versions = changelog.getVersions().collect(toList());
        if (versions.isEmpty()) {
            throw new IllegalArgumentException("Cannot locate any version heading to determine forge");
        }

        URL url = versions.get(0).getURL();
        Forge forge = config.getForge() != null
                ? findForge(onForgeConfig(config.getForge())).orElseThrow(() -> new IllegalArgumentException("Cannot find forge with id '" + config.getForge().getId() + "'"))
                : findForge(onHost(url, config.getDomains())).orElseThrow(() -> new IllegalArgumentException("Cannot determine forge"));

        ProjectLink projectLink = findProjectLink(forge, url)
                .orElseThrow(() -> new IllegalArgumentException("Cannot determine project URL from changelog"));

        for (ForgeLinkType type : ForgeLinkType.values()) {
            MessageFetcher fetcher = forge.getMessageFetcher(type);
            if (fetcher != null) {
                ForgeLinkResolver linkResolver = forge.getLinkResolver(type);
                if (linkResolver != null) {
                    ForgeLink link = linkResolver.resolveForgeLinkOrNull(projectLink.getProjectURL(), ref);
                    if (link != null) {
                        return fetcher.fetchMessage(httpFactory.getClient(), link) + toMarkdown(link);
                    }
                }
            }
        }

        throw new IllegalArgumentException("Cannot resolve ref '" + ref + "' for project URL '" + projectLink + "' on forge '" + forge.getForgeName() + "'");
    }

    public void push(@NonNull Document document, @NonNull TypeOfChange typeOfChange, @NonNull String message, @NonNull Config config) throws IllegalArgumentException {
        if (message.isEmpty()) {
            throw new IllegalArgumentException("Message must not be empty");
        }

        ChangelogHeading changelog = ChangelogHeading.root(document)
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate changelog header"));

        VersionHeading unreleased = changelog.getVersions()
                .filter(versionNode -> versionNode.getSection().isUnreleased())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate unreleased header"));

        // Find or create the type-of-change heading under unreleased
        Heading typeOfChangeHeading = unreleased.getTypeOfChanges()
                .filter(toc -> toc.getSection() == typeOfChange)
                .map(TypeOfChangeHeading::getHeading)
                .findFirst()
                .orElse(null);

        if (typeOfChangeHeading == null) {
            typeOfChangeHeading = typeOfChange.toHeading();
            // Skip preamble (non-heading nodes after the version heading)
            Node insertAfter = unreleased.getHeading();
            Node next = insertAfter.getNext();
            while (next != null && !VersionHeading.isParsable(next) && !TypeOfChangeHeading.isParsable(next) && !(next instanceof ReferenceNode)) {
                insertAfter = next;
                next = next.getNext();
            }
            // Walk through existing type-of-change headings to find the canonical insertion point
            while (next != null && TypeOfChangeHeading.isParsable(next)) {
                TypeOfChange existing = illegalArgumentToNull(TypeOfChange::parse).apply((Heading) next);
                if (existing != null && existing.ordinal() < typeOfChange.ordinal()) {
                    // Skip past this block (heading + all its content nodes)
                    insertAfter = next;
                    next = next.getNext();
                    while (next != null && !TypeOfChangeHeading.isParsable(next) && !VersionHeading.isParsable(next) && !(next instanceof ReferenceNode)) {
                        insertAfter = next;
                        next = next.getNext();
                    }
                } else {
                    break; // found the right insertion position
                }
            }
            insertAfter.insertAfter(typeOfChangeHeading);
        }

        // Find or create the bullet list after the type-of-change heading
        Node bulletListNode = typeOfChangeHeading.getNext();
        BulletList bulletList;
        if (bulletListNode instanceof BulletList) {
            bulletList = (BulletList) bulletListNode;
        } else {
            bulletList = new BulletList();
            bulletList.setOpeningMarker('-');
            typeOfChangeHeading.insertAfter(bulletList);
        }

        // Create the new bullet list item by parsing the message as markdown
        Document parsed = FlexmarkIO.newParser().parse("- " + message + "\n");
        BulletListItem newItem = Nodes.of(BulletListItem.class).descendants(parsed)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Failed to parse message as bullet list item"));

        bulletList.appendChild(newItem);

        validateAfterModification(document, config);
    }

    public @NonNull Summary scan(@NonNull Document document, @NonNull Config config) {
        if (isNotValidAgainstGuidingPrinciples(document)) {
            return Summary.INVALID;
        }

        ChangelogHeading changelog = ChangelogHeading.root(document).orElse(null);

        if (changelog == null) {
            return Summary.INVALID;
        }

        List<VersionHeading> versions = changelog.getVersions().collect(toList());

        if (versions.isEmpty()) {
            return Summary.EMPTY;
        }

        Map<Boolean, List<VersionHeading>> versionsByStatus = versions.stream().collect(partitioningBy(version -> version.getSection().isReleased()));

        List<Version> releases = versionsByStatus.get(true)
                .stream()
                .map(VersionHeading::getSection)
                .collect(toList());

        long yankedReleaseCount = releases.stream().filter(Version::isYanked).count();

        long unreleasedChanges = versionsByStatus.get(false)
                .stream()
                .findFirst()
                .map(version -> version.getTypeOfChanges().flatMap(TypeOfChangeHeading::getBulletListItems).count())
                .orElse(0L);

        VersionHeading first = versions.get(0);
        Forge forgeOrNull = config.getForge() != null
                ? findForge(onForgeConfig(config.getForge())).orElse(null)
                : findForge(onHost(first.getURL(), config.getDomains())).orElse(null);

        return Summary
                .builder()
                .valid(true)
                .releaseCount(releases.size())
                .yankedReleaseCount((int) yankedReleaseCount)
                .timeRange(releases.stream().map(Version::getDate).collect(toTimeRange()).orElse(TimeRange.ALL))
                .compatibilities(versioningStreamOf(versionings, releases).map(Versioning::getVersioningName).collect(toList()))
                .unreleasedChanges((int) unreleasedChanges)
                .forgeName(forgeOrNull != null ? forgeOrNull.getForgeName() : null)
                .forgeURL(
                        findProjectLink(forgeOrNull, first.getURL())
                                .map(ProjectLink::getProjectURL)
                                .orElse(URLExtractor.baseOf(first.getURL()))
                )
                .build();
    }

    public boolean format(@NonNull Document document) {
        if (isNotValidAgainstGuidingPrinciples(document)) {
            return false;
        }
        ChangelogHeading changelog = ChangelogHeading.root(document).orElse(null);
        if (changelog == null) {
            return false;
        }
        boolean changed = changelog.getVersions()
                .map(Heylogs::sortTypeOfChanges)
                .reduce(false, Boolean::logicalOr);
        changed |= changelog.getVersions()
                .map(Heylogs::removeEmptyTypeOfChanges)
                .reduce(false, Boolean::logicalOr);
        changed |= sortReferenceLinks(changelog, document);
        changed |= normalizeBulletMarkers(document);
        return changed;
    }

    private static boolean removeEmptyTypeOfChanges(@NonNull VersionHeading versionHeading) {
        if (!versionHeading.getSection().isReleased()) {
            return false; // leave unreleased sections untouched (works-in-progress)
        }

        List<List<Node>> toRemove = new ArrayList<>();
        Node node = versionHeading.getHeading().getNext();
        while (node != null && !isVersionBoundary(node)) {
            if (TypeOfChangeHeading.isParsable(node)) {
                TypeOfChangeHeading toc = illegalArgumentToNull(TypeOfChangeHeading::parse).apply(node);
                if (toc != null && !toc.getBulletListItems().findFirst().isPresent()) {
                    List<Node> block = new ArrayList<>();
                    block.add(node);
                    Node next = node.getNext();
                    while (next != null && !TypeOfChangeHeading.isParsable(next) && !isVersionBoundary(next)) {
                        block.add(next);
                        next = next.getNext();
                    }
                    toRemove.add(block);
                    node = next;
                    continue;
                }
            }
            node = node.getNext();
        }

        if (toRemove.isEmpty()) {
            return false;
        }
        for (List<Node> block : toRemove) {
            for (Node n : block) {
                n.unlink();
            }
        }
        return true;
    }

    private static boolean sortReferenceLinks(@NonNull ChangelogHeading changelog, @NonNull Document document) {
        List<Reference> allRefs = Nodes.of(Reference.class)
                .descendants(document)
                .collect(toList());

        if (allRefs.size() < 2) {
            return false;
        }

        // Build version ref list in current display order (already latest-first)
        List<String> versionRefs = changelog.getVersions()
                .map(vh -> vh.getSection().getRef().toLowerCase(Locale.ROOT))
                .collect(toList());

        // Build sorted list: version refs in version order, then remaining refs alphabetically
        List<Reference> sorted = new ArrayList<>();
        Set<String> placed = new HashSet<>();

        for (String versionRef : versionRefs) {
            allRefs.stream()
                    .filter(ref -> versionRef.equalsIgnoreCase(ref.getReference().toString()))
                    .findFirst()
                    .ifPresent(ref -> {
                        sorted.add(ref);
                        placed.add(ref.getReference().toString().toLowerCase(Locale.ROOT));
                    });
        }

        allRefs.stream()
                .filter(ref -> !placed.contains(ref.getReference().toString().toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(ref -> ref.getReference().toString().toLowerCase(Locale.ROOT)))
                .forEach(sorted::add);

        if (sorted.equals(allRefs)) {
            return false;
        }

        allRefs.forEach(Node::unlink);
        sorted.forEach(document::appendChild);
        return true;
    }

    private static boolean sortTypeOfChanges(@NonNull VersionHeading versionHeading) {
        Heading versionNode = versionHeading.getHeading();

        // Skip preamble (nodes before first type-of-change heading)
        Node insertionPoint = versionNode;
        Node node = versionNode.getNext();
        while (node != null && !TypeOfChangeHeading.isParsable(node) && !isVersionBoundary(node)) {
            insertionPoint = node;
            node = node.getNext();
        }

        if (node == null || isVersionBoundary(node)) {
            return false; // no type-of-change headings in this version
        }

        // Collect type-of-change blocks
        List<TypeOfChangeBlock> blocks = new ArrayList<>();
        while (node != null && !isVersionBoundary(node)) {
            if (TypeOfChangeHeading.isParsable(node)) {
                TypeOfChange typeOfChange = illegalArgumentToNull(TypeOfChange::parse).apply((Heading) node);
                if (typeOfChange == null) {
                    node = node.getNext();
                    continue;
                }
                List<Node> content = new ArrayList<>();
                content.add(node);
                Node next = node.getNext();
                while (next != null && !TypeOfChangeHeading.isParsable(next) && !isVersionBoundary(next)) {
                    content.add(next);
                    next = next.getNext();
                }
                blocks.add(new TypeOfChangeBlock(typeOfChange, content));
                node = next;
            } else {
                node = node.getNext();
            }
        }

        if (blocks.size() < 2) {
            return false; // nothing to sort
        }

        // Check if already sorted
        boolean sorted = true;
        for (int i = 1; i < blocks.size(); i++) {
            if (blocks.get(i).typeOfChange.ordinal() < blocks.get(i - 1).typeOfChange.ordinal()) {
                sorted = false;
                break;
            }
        }
        if (sorted) {
            return false;
        }

        // Sort blocks by canonical TypeOfChange order
        blocks.sort(Comparator.comparingInt(b -> b.typeOfChange.ordinal()));

        // Unlink all type-of-change nodes
        for (TypeOfChangeBlock block : blocks) {
            for (Node n : block.content) {
                n.unlink();
            }
        }

        // Re-insert in sorted order after insertionPoint
        Node lastInserted = insertionPoint;
        for (TypeOfChangeBlock block : blocks) {
            for (Node n : block.content) {
                lastInserted.insertAfter(n);
                lastInserted = n;
            }
        }
        return true;
    }

    private static boolean normalizeBulletMarkers(@NonNull Document document) {
        boolean changed = false;

        for (BulletList list : Nodes.of(BulletList.class).descendants(document).collect(toList())) {
            if (list.getOpeningMarker() != '-') {
                list.setOpeningMarker('-');
                changed = true;
            }
        }

        for (BulletListItem item : Nodes.of(BulletListItem.class).descendants(document).collect(toList())) {
            BasedSequence marker = item.getOpeningMarker();
            if (marker.isEmpty() || marker.charAt(0) != '-') {
                item.setOpeningMarker(BasedSequence.of("-"));
                changed = true;
            }
        }

        return changed;
    }

    private static boolean isVersionBoundary(@NonNull Node node) {
        return VersionHeading.isParsable(node) || node instanceof com.vladsch.flexmark.ast.Reference;
    }

    @lombok.Value
    private static class TypeOfChangeBlock {
        TypeOfChange typeOfChange;
        List<Node> content;
    }

    public void note(@NonNull Document document, @NonNull String summary, @NonNull Config config) {
        ChangelogHeading changelog = ChangelogHeading.root(document)
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate changelog header"));

        VersionHeading unreleased = changelog.getVersions()
                .filter(versionNode -> versionNode.getSection().isUnreleased())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot locate unreleased header"));

        // Find the first type-of-change or version heading after the Unreleased heading
        Node start = unreleased.getHeading();
        Node node = start.getNext();
        Node stopNode = null;
        while (node != null) {
            if (TypeOfChangeHeading.isParsable(node) || VersionHeading.isParsable(node)) {
                stopNode = node;
                break;
            }
            node = node.getNext();
        }

        // Collect reference link definitions to preserve
        java.util.List<Node> referenceDefs = new java.util.ArrayList<>();
        node = start.getNext();
        while (node != null && node != stopNode) {
            Node next = node.getNext();
            if (node instanceof com.vladsch.flexmark.ast.Reference) {
                referenceDefs.add(node);
            } else {
                node.unlink();
            }
            node = next;
        }

        // Insert the summary as Markdown (if not empty)
        if (!summary.trim().isEmpty()) {
            Document parsed = FlexmarkIO.newParser().parse(summary + "\n");
            Node insertAfter = start;
            Node lastSummaryNode = null;
            for (Node child = parsed.getFirstChild(); child != null; ) {
                Node nextChild = child.getNext();
                if (insertAfter.getParent() == null) {
                    document.appendChild(child);
                } else {
                    insertAfter.insertAfter(child);
                }
                insertAfter = child;
                lastSummaryNode = child;
                child = nextChild;
            }
            // If the next node is a reference definition, insert a blank paragraph to ensure proper markdown rendering
            if (lastSummaryNode != null && lastSummaryNode.getNext() instanceof com.vladsch.flexmark.ast.Reference) {
                com.vladsch.flexmark.ast.Paragraph blank = new com.vladsch.flexmark.ast.Paragraph();
                lastSummaryNode.insertAfter(blank);
            }
        }
        // Re-append reference definitions after summary (if any)
        Node last = document.getLastChild();
        for (Node ref : referenceDefs) {
            if (ref.getParent() == null) {
                document.appendChild(ref);
                last = ref;
            }
        }

        validateAfterModification(document, config);
    }

    public @NonNull List<ScrapedLink> scrape(@NonNull Document doc, @NonNull Config config) {
        RuleContext context = initContext(config);
        return Nodes.of(LinkNodeBase.class)
                .descendants(doc)
                .map(link -> {
                    String urlAsString = link.getUrl().toString();
                    URL url = Util.illegalArgumentToNull(URLExtractor::urlOf).apply(link.getUrl());
                    if (url != null) {
                        List<String> result = new ArrayList<>();
                        for (Forge forge : context.findAllForges(url)) {
                            for (ForgeLinkType type : ForgeLinkType.values()) {
                                ForgeLinkParser linkParser = forge.getLinkParser(type);
                                ForgeLink expectedLink = linkParser != null ? linkParser.parseForgeLinkOrNull(url) : null;
                                if (expectedLink != null) {
                                    result.add(forge.getForgeId() + ":" + type);
                                }
                            }
                        }
                        return new ScrapedLink(urlAsString.substring(0, Math.min(50, urlAsString.length())), link.getLineNumber(), result);
                    }
                    return new ScrapedLink(urlAsString, link.getLineNumber(), singletonList("invalid"));
                })
                .collect(toList());
    }

    public void formatProblems(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        findFormat(onFormatId(formatId))
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatProblems(appendable, list);
    }

    public void formatStatus(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException {
        findFormat(onFormatId(formatId))
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatStatus(appendable, list);
    }

    public void formatResources(@NonNull String formatId, @NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException {
        findFormat(onFormatId(formatId))
                .orElseThrow(() -> new IOException("Cannot find format '" + formatId + "'"))
                .formatResources(appendable, list);
    }

    public @NonNull Optional<String> getFormatIdByFile(@NonNull Path file) {
        return findFormat(onFormatFileFilter(file)).map(Format::getFormatId);
    }

    private Optional<Rule> findRule(@NonNull Predicate<Rule> predicate) {
        return rules.stream().filter(predicate).findFirst();
    }

    private Optional<Tagging> findTagging(@NonNull Predicate<Tagging> predicate) {
        return taggings.stream().filter(predicate).findFirst();
    }

    private Optional<Forge> findForge(@NonNull Predicate<Forge> predicate) {
        return forges.stream().filter(predicate).findFirst();
    }

    private Optional<Versioning> findVersioning(@NonNull Predicate<Versioning> predicate) {
        return versionings.stream().filter(predicate).findFirst();
    }

    private Optional<Format> findFormat(@NonNull Predicate<Format> predicate) {
        return formats.stream().filter(predicate).findFirst();
    }

    public static final String FIRST_FORMAT_AVAILABLE = "";

    private static boolean isNotValidAgainstGuidingPrinciples(Document document) {
        return problemStreamOf(document, asList(GuidingPrinciples.values()), RuleContext.DEFAULT).findFirst().isPresent();
    }

    @MightBePromoted
    @SafeVarargs
    private static <T> Stream<T> concat(Stream<T> first, Stream<T>... rest) {
        Stream<T> result = first;
        for (Stream<T> next : rest) {
            result = Stream.concat(result, next);
        }
        return result;
    }

    private static Stream<Versioning> versioningStreamOf(List<Versioning> list, List<Version> releases) {
        return list.stream()
                .filter(versioning -> versioning.getVersioningArgValidator().apply(null) == null)
                .filter(versioning -> {
                            Predicate<CharSequence> predicate = versioning.getVersioningPredicateOrNull(null);
                            return predicate != NO_VERSIONING_FILTER && releases.stream().map(Version::getRef).allMatch(predicate);
                        }
                );
    }

    private @Nullable Predicate<CharSequence> getVersioningPredicate(@Nullable VersioningConfig versioningConfig) {
        if (versioningConfig != null) {
            Versioning versioning = findVersioning(versioningConfig::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find versioning with id '" + versioningConfig.getId() + "'"));
            Predicate<CharSequence> predicate = versioning.getVersioningPredicateOrNull(versioningConfig.getArg());
            if (predicate == NO_VERSIONING_FILTER) {
                throw new IllegalArgumentException("Invalid version argument '" + versioningConfig.getArg() + "'");
            }
            return predicate;
        }
        return null;
    }

    private @NonNull String getTag(@Nullable TaggingConfig config, @NonNull String versionRef) {
        if (config != null) {
            Converter<String, String> tagFormatterOrNull = findTagging(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find tagging with id '" + config.getId() + "'"))
                    .getTagFormatterOrNull(config.getArg());
            if (tagFormatterOrNull != Tagging.CONVERSION_NOT_SUPPORTED) {
                return tagFormatterOrNull.apply(versionRef);
            }
        }
        return versionRef;
    }

    @VisibleForTesting
    void checkConfig(@NonNull Config config) throws IllegalArgumentException {
        checkVersioningConfig(config.getVersioning());
        checkTaggingConfig(config.getTagging());
        checkRuleConfigs(config.getRules());
        checkDomainConfigs(config.getDomains());
    }

    private void checkVersioningConfig(VersioningConfig config) throws IllegalArgumentException {
        if (config != null) {
            String error = findVersioning(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find versioning with id '" + config.getId() + "'"))
                    .getVersioningArgValidator()
                    .apply(config.getArg());
            if (error != null) {
                throw new IllegalArgumentException("Invalid versioning argument '" + config.getArg() + "': " + error);
            }
        }
    }

    private void checkTaggingConfig(TaggingConfig config) throws IllegalArgumentException {
        if (config != null) {
            String error = findTagging(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find tagging with id '" + config.getId() + "'"))
                    .getTaggingArgValidator()
                    .apply(config.getArg());
            if (error != null) {
                throw new IllegalArgumentException("Invalid tagging argument '" + config.getArg() + "': " + error);
            }
        }
    }

    private void checkRuleConfigs(List<RuleConfig> configs) throws IllegalArgumentException {
        for (RuleConfig config : configs) {
            findRule(config::isCompatibleWith)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find rule with id '" + config.getId() + "'"));
        }
    }

    private void checkDomainConfigs(List<DomainConfig> configs) throws IllegalArgumentException {
        for (DomainConfig config : configs) {
            findForge(onDomainConfig(config))
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find forge with id '" + config.getForgeId() + "'"));
        }
    }

    private RuleContext initContext(Config config) {
        return RuleContext
                .builder()
                .config(config)
                .forges(forges)
                .versionings(versionings)
                .taggings(taggings)
                .build();
    }

    /**
     * Validates a document after modification to ensure it remains valid.
     * Uses the check() method to validate with the provided configuration.
     * Can be disabled by setting validateAfterModification to false in the builder.
     *
     * @param document the document to validate
     * @param config   the configuration to use for validation
     * @throws IllegalStateException if the document has validation errors after modification
     */
    private void validateAfterModification(@NonNull Document document, @NonNull Config config) {
        if (!validateAfterModification) {
            return; // Validation is disabled
        }

        List<Problem> problems = check(document, config);
        if (!problems.isEmpty()) {
            // Only report ERROR-level problems
            List<Problem> errors = problems.stream()
                    .filter(p -> RuleSeverity.ERROR.equals(p.getSeverity()))
                    .collect(toList());

            if (!errors.isEmpty()) {
                StringBuilder message = new StringBuilder("Modified changelog has errors:");
                for (Problem problem : errors) {
                    message.append("\n  - ")
                            .append(problem.getId())
                            .append(": ")
                            .append(problem.getIssue().getMessage());
                }
                throw new IllegalStateException(message.toString());
            }
        }
    }

    private static @NonNull String toMarkdown(ForgeLink link) {
        return " [" + link.toRef(null) + "](" + link.toURL() + ")";
    }

    private static final URL DEFAULT_PROJECT_URL = URLExtractor.urlOf("https://example.com/");

    private static Optional<ProjectLink> findProjectLink(Forge forgeOrNull, URL url) {
        if (forgeOrNull != null) {
            ProjectLinkParser parser = forgeOrNull.getProjectLinkParser();
            if (parser != null) {
                return Optional.ofNullable(parser.parseForgeLinkOrNull(url));
            }
        }
        return Optional.empty();
    }

    private static Optional<CompareLink> findCompareLink(Forge forge, URL url) {
        ProjectLink projectLink = findProjectLink(forge, url).orElse(null);
        if (projectLink == null) return Optional.empty();
        if (projectLink instanceof CompareLink) return Optional.of((CompareLink) projectLink);
        CompareLinkConverter converter = forge.getCompareLinkConverter();
        return converter != null
                ? Optional.of(converter.convert(projectLink))
                : Optional.empty();
    }

    private static Stream<Problem> problemStreamOf(Document root, List<Rule> rules, RuleContext context) {
        return Nodes.walk(root)
                .flatMap(node -> rules.stream().map(rule -> getProblemOrNull(node, rule, context)).filter(Objects::nonNull));
    }

    private static Problem getProblemOrNull(Node node, Rule rule, RuleContext context) {
        RuleSeverity severity = context.findRuleSeverityOrNull(rule.getRuleId());
        if (severity == null) severity = rule.getRuleSeverity();
        if (!RuleSeverity.OFF.equals(severity)) {
            RuleIssue ruleIssueOrNull = rule.getRuleIssueOrNull(node, context);
            if (ruleIssueOrNull != null) {
                return Problem
                        .builder()
                        .id(rule.getRuleId())
                        .severity(severity)
                        .issue(ruleIssueOrNull)
                        .build();
            }
        }
        return null;
    }
}
