package internal.heylogs;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;
import com.vladsch.flexmark.util.misc.Pair;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Version;
import nbbrd.heylogs.spi.Rule;
import nbbrd.service.ServiceProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.jooq.lambda.Unchecked;
import org.tinylog.Logger;

@ServiceProvider
public final class GitDiffRule implements Rule {

    private Path path;

    private String oldRevStr;
    private String newRevStr;
    private boolean useHeadAndParent = true;

    private List<Pair<Integer, Integer>> commitModificationRanges;

    private Set<Failure> reported;
    private boolean isGitRepository;
    private boolean initialized;
    private boolean processedReference;
    private Version latestFrozenVersion;
    private boolean latestFrozenVersionSeen;
    private Heading previousFrozenHeading;
    // the first link to a Diff (at the end of CHANGELOG.md)
    private Node firstReference;

    public GitDiffRule() {
        resetFields();
        useHeadAndParent = true;
    }

    public GitDiffRule(String oldRevStr, String newRevStr) {
        resetFields();
        useHeadAndParent = false;
        this.oldRevStr = oldRevStr;
        this.newRevStr = newRevStr;
    }

    @VisibleForTesting
    GitDiffRule(Path path) {
        resetFields();
        this.path = path;
    }

    public void resetFields() {
        this.reported = new HashSet<>();
        this.isGitRepository = true;
        this.initialized = false;
        this.processedReference = false;
        this.latestFrozenVersion = null;
        this.latestFrozenVersionSeen = false;
        this.previousFrozenHeading = null;
        this.firstReference = null;
    }

    /**
     * <p>
     *     Changes the underlying git repository.
     * </p>
     * <p>
     *     Normally, one would re-instantiate the class. The current infrastructure, however, demands that each check is initialized once.
     * </p>
     */
    public void setPath(Path path) {
        if (path.equals(this.path)) {
            return;
        }
        resetFields();
        this.path = path;
    }

    @Override
    public String getId() {
        return "releasechangesfrozen";
    }

    @Override
    public boolean isAvailable() {
        return Rule.isEnabled(System.getProperties(), getId());
    }

    @Override
    public Failure validate(Node node) {
        if (!isGitRepository || processedReference) {
            return NO_PROBLEM;
        }
        if (!initialized) {
            initialize(node);
            return NO_PROBLEM;
        }
        if (node == firstReference) {
            processedReference = true;
            if (previousFrozenHeading == null) {
                return NO_PROBLEM;
            }
            return getFailure(previousFrozenHeading, node.getLineNumber());
        }
        if (!(node instanceof Heading)) {
            return NO_PROBLEM;
        }
        Heading heading = (Heading) node;
        if (!Version.isVersionLevel(heading)) {
            return NO_PROBLEM;
        }
        Version version;
        try {
            version = Version.parse(heading);
        } catch (IllegalArgumentException e) {
            Logger.debug("Ignoring invalid version at {}", heading);
            return NO_PROBLEM;
        }
        if (version.isUnreleased()) {
            return NO_PROBLEM;
        }
        if (!latestFrozenVersionSeen) {
            if (!version.equals(latestFrozenVersion)) {
                return NO_PROBLEM;
            }
            latestFrozenVersionSeen = true;
        }
        Failure result;
        if (previousFrozenHeading == null) {
            result = NO_PROBLEM;
        } else {
            result = getFailure(previousFrozenHeading, heading.getLineNumber());
        }
        previousFrozenHeading = heading;
        return result;
    }

    private void initialize(Node node) {
        initialized = true;
        try {
            initialize();
        } catch (RepositoryNotFoundException e) {
            Logger.debug("No git repository found");
            isGitRepository = false;
        } catch (Exception e) {
            Logger.error("Could not read from git repository", e);
            isGitRepository = false;
        }
        // Set {@link #firstReference} to the first reference in the reference list at the bottom
        // We just ignore link updates
        ReversiblePeekingIterator<Node> iterator = node.getDocument().getReversedChildIterator();
        Node previous = iterator.next();
        do {
            firstReference = previous;
            previous = iterator.next();
        } while (iterator.hasNext() && (previous instanceof Reference));
    }

    private Failure getFailure(Heading heading, int nextHeadingLineNumber) {
        int lineNumber = heading.getLineNumber();
        Optional<Integer> firstModifiedLineNumber = intersects(lineNumber, nextHeadingLineNumber - 1);
        if (firstModifiedLineNumber.isEmpty()) {
            return NO_PROBLEM;
        }
        Failure result = Failure.builder()
                                .rule(this)
                                .location(heading)
                                .line(firstModifiedLineNumber.get() + 1)
                                .message("Change in a released version")
                                .build();
        if (reported.contains(result)) {
            return NO_PROBLEM;
        }
        reported.add(result);
        return result;
    }

    private Optional<Integer> intersects(int start, int end) {
        return commitModificationRanges
                .stream()
                .filter(pair -> {
                    int first = pair.getFirst();
                    int second = pair.getSecond();
                    return start <= second && end >= first; // this checks for overlapping ranges
                }).findAny()
                .map(pair -> pair.getFirst());
    }

    @VisibleForTesting
    void initialize() throws Exception {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try (Repository repository = builder.setGitDir(path.resolve(".git").toFile())
                                            .readEnvironment()
                                            .findGitDir()
                                            .build();
             RevWalk revWalk = new RevWalk(repository)) {

            ObjectId newObjectId;
            if (useHeadAndParent) {
                newObjectId = repository.resolve(Constants.HEAD);
            } else {
                newObjectId = repository.resolve(newRevStr);
            }
            RevCommit commit = revWalk.parseCommit(newObjectId);
            RevCommit oldCommit;
            if (useHeadAndParent) {
                oldCommit = revWalk.parseCommit(commit.getParent(0));
            } else {
                oldCommit = revWalk.parseCommit(repository.resolve(oldRevStr));
            }

            this.commitModificationRanges = determineCommitModificationRanges(repository, oldCommit, commit);
            setLatestFrozenVersion(repository, revWalk, oldCommit);
        }
    }

    @VisibleForTesting
    static List<Pair<Integer, Integer>> determineCommitModificationRanges(Repository repository, RevCommit oldCommit, RevCommit newCommit) throws IOException, GitAPIException {
        final List<Pair<Integer, Integer>> commitVersionModificationRanges;
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser headTreeParser = new CanonicalTreeParser();
            headTreeParser.reset(reader, newCommit.getTree().getId());
            RevTree oldCommitTree = oldCommit.getTree();
            CanonicalTreeParser parentTreeParser = new CanonicalTreeParser();
            parentTreeParser.reset(reader, oldCommitTree.getId());
            Git git = new Git(repository);
            List<DiffEntry> diffs = git.diff()
                                       .setOldTree(parentTreeParser)
                                       .setNewTree(headTreeParser)
                                       .call();
            try (DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                formatter.setReader(reader, new Config());
                commitVersionModificationRanges =
                        diffs.stream()
                             .filter(entry -> entry.getNewPath().equals("CHANGELOG.md"))
                             .map(Unchecked.function(entry -> formatter.toFileHeader(entry)))
                             .flatMap(fileHeader -> fileHeader.toEditList().stream())
                             .flatMap(edit -> {
                                 switch (edit.getType()) {
                                     case INSERT:
                                     case REPLACE:
                                         return Stream.of(new Pair<>(edit.getBeginB(), edit.getEndB() - 1));
                                     case DELETE:
                                         return Stream.of(new Pair<>(edit.getBeginB(), edit.getBeginB()));
                                     default:
                                         return Stream.empty();
                                 }
                             }).collect(Collectors.toList());
            }
        }
        return commitVersionModificationRanges;
    }

    private void setLatestFrozenVersion(Repository repository, RevWalk revWalk, RevCommit commit) throws Exception {
        RevTree tree = revWalk.parseCommit(commit).getTree();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(tree);
            treeWalk.setFilter(PathFilter.create("CHANGELOG.md"));
            if (!treeWalk.next()) {
                throw new FileNotFoundException("CHANGELOG.md not found");
            }
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);
            this.latestFrozenVersion = getNewestRelease(loader.getBytes());
            if (this.latestFrozenVersion == null) {
                Logger.debug("No release found in CHANGELOG.md");
            }
        }
    }

    @VisibleForTesting
    static Version getNewestRelease(byte[] bytes) throws Exception {
        Parser parser = Parser.builder().build();
        Document document;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8)) {
            document = parser.parseReader(inputStreamReader);
        }
        ReversiblePeekingIterator<Node> iterator = document.getChildren().iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (!(node instanceof Heading)) {
                continue;
            }
            Heading heading = (Heading) node;
            if (!Version.isVersionLevel(heading)) {
                continue;
            }
            Version version = Version.parse(heading);
            if (version.isUnreleased()) {
                continue;
            }
            return version;
        }
        return null;
    }
}
