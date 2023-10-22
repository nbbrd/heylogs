package internal.heylogs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.misc.Pair;
import nbbrd.heylogs.Failure;
import nbbrd.heylogs.Version;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static _test.Sample.using;
import static nbbrd.heylogs.Nodes.of;
import static org.assertj.core.api.Assertions.assertThat;

class GitDiffRuleTest {

    private GitDiffRule gitDiffRule;
    private Repository repository;
    private Git git;
    private Path changelog;

    private static final LocalDate d20230911 = LocalDate.parse("2023-09-11");
    private static final LocalDate d20230912 = LocalDate.parse("2023-09-12");

    private static final String CHANGELOG_INITIAL = """
            # Changelog

            All notable changes to this project will be documented in this file.

            The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
            and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

            ## [0.1.0] - 2023-09-11

            Initial release.

            [0.1.0]: https://example.com/github/repository/releases/tag/0.1.0
            """;

    private static final String CHANGELOG_UNRELEASED = """
            # Changelog

            All notable changes to this project will be documented in this file.

            The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
            and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

            ## [unreleased]

            ### Added

            - Some additions.

            ## [0.1.0] - 2023-09-11

            Initial release.

            [unreleased]: https://example.com/github/repository/compare/0.1.0...HEAD
            [0.1.0]: https://example.com/github/repository/releases/tag/0.1.0
            """;

    private static final String CHANGELOG_020 = """
            # Changelog

            All notable changes to this project will be documented in this file.

            The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
            and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

            ## [0.2.0] - 2023-09-12

            ### Added

            - Some additions.

            ## [0.1.0] - 2023-09-11

            Initial release.

            [0.2.0]: https://example.com/github/repository/compare/0.1.0...0.2.0
            [0.1.0]: https://example.com/github/repository/releases/tag/0.1.0
            """;

    private static final String CHANGELOG_NO_RELEASE = """
            # Changelog

            All notable changes to this project will be documented in this file.

            The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
            and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

            ## [unreleased]

            Preparing initial release
            """;

    @BeforeEach
    public void setup(@TempDir Path dir) throws Exception {
        gitDiffRule = new GitDiffRule(dir);
        changelog = dir.resolve("CHANGELOG.md");
        repository = FileRepositoryBuilder.create(dir.resolve(".git").toFile());
        repository.create();
        git = new Git(repository);
    }

    private void addAndCommitChangelogMd(String content) throws Exception {
        Files.writeString(changelog, content);
        git.add().addFilepattern("CHANGELOG.md").call();
        git.commit()
           .setMessage("Creates/updates CHANGELOG.md")
           .setAuthor(new PersonIdent("Test User", "test@example.com"))
           .call();
    }

    @Test
    public void newUnreleasedSectionIsOk() throws Exception {
        addAndCommitChangelogMd(CHANGELOG_INITIAL);
        addAndCommitChangelogMd(CHANGELOG_UNRELEASED);
        assertThat(of(Node.class).descendants(using(changelog)))
                .map(gitDiffRule::validate)
                .filteredOn(Objects::nonNull)
                .isEmpty();
    }

    @Test
    public void newReleaseAddedIsOk() throws Exception {
        addAndCommitChangelogMd(CHANGELOG_INITIAL);
        addAndCommitChangelogMd(CHANGELOG_020);
        assertThat(of(Node.class).descendants(using(changelog)))
                .map(gitDiffRule::validate)
                .filteredOn(Objects::nonNull)
                .isEmpty();
    }

    @Test
    public void newReleaseAddedEvenWithNonOKChangesInBetweenIsOk() throws Exception {
        addAndCommitChangelogMd(CHANGELOG_INITIAL);
        addAndCommitChangelogMd(CHANGELOG_UNRELEASED);
        git.checkout().setCreateBranch(true).setName("test").call();
        createCommit("file.txt", "a");
        addAndCommitChangelogMd(CHANGELOG_020.replace("additions.", "additions"));
        createCommit("file.txt", "b");
        addAndCommitChangelogMd(CHANGELOG_020);
        createCommit("file.txt", "c");

        gitDiffRule = new GitDiffRule("master", "test");
        gitDiffRule.setPath(changelog.getParent());

        assertThat(of(Node.class).descendants(using(changelog)))
                .map(gitDiffRule::validate)
                .filteredOn(Objects::nonNull)
                .isEmpty();
    }

    @Test
    public void fixInitialReleaseRaisesFailure() throws Exception {
        addAndCommitChangelogMd(CHANGELOG_INITIAL);
        addAndCommitChangelogMd(CHANGELOG_020.replace("Initial release.", "_Initial release._"));
        assertThat(of(Node.class).descendants(using(changelog)))
                .map(gitDiffRule::validate)
                .filteredOn(Objects::nonNull)
                .contains(Failure.builder().rule(gitDiffRule).message("Change in a released version").line(16).column(1).build())
                .hasSize(1);
    }

    @Test
    public void fix020ReleaseRaisesFailure() throws Exception {
        addAndCommitChangelogMd(CHANGELOG_020);
        addAndCommitChangelogMd(CHANGELOG_020.replace("additions.", "additions"));
        assertThat(of(Node.class).descendants(using(changelog)))
                .map(gitDiffRule::validate)
                .filteredOn(Objects::nonNull)
                .contains(Failure.builder().rule(gitDiffRule).message("Change in a released version").line(12).column(1).build())
                .hasSize(1);
    }

    @Test
    public void fix020ReleaseRaisesFailureOneLargerDiff() throws Exception {
        addAndCommitChangelogMd(CHANGELOG_020);
        git.checkout().setCreateBranch(true).setName("test").call();
        createCommit("file.txt", "a");
        createCommit("file.txt", "b");
        createCommit("file.txt", "c");
        addAndCommitChangelogMd(CHANGELOG_020.replace("additions.", "additions"));

        gitDiffRule = new GitDiffRule("master", "test");
        gitDiffRule.setPath(changelog.getParent());

        assertThat(of(Node.class).descendants(using(changelog)))
                .map(gitDiffRule::validate)
                .filteredOn(Objects::nonNull)
                .contains(Failure.builder().rule(gitDiffRule).message("Change in a released version").line(12).column(1).build())
                .hasSize(1);
    }

    private void createCommit(String file, String content) throws Exception {
        Files.writeString(changelog.getParent().resolve(file), content);
        git.add().addFilepattern("CHANGELOG.md").call();
        git.commit()
           .setMessage("Creates/updates " + content)
           .setAuthor(new PersonIdent("Test User", "test@example.com"))
           .call();
    }

    public static Stream<Arguments> correctChangePairs() {
        // vs.code and other editors count the line numbers starting by 1, but in this code, everyhting starts with 0
        // Thus, when inspecting this test manually in an editor, please respect the offset.
        return Stream.of(
                Arguments.of(CHANGELOG_INITIAL, CHANGELOG_UNRELEASED, List.of(new Pair(7, 12), new Pair(17, 17))),
                Arguments.of(CHANGELOG_020, CHANGELOG_020.replace("additions.", "additions"), List.of(new Pair(11, 11))),
                Arguments.of(CHANGELOG_020, CHANGELOG_020.replace("additions.", "additions.\n\nSome new line"), List.of(new Pair(13, 14)))
        );
    }

    @ParameterizedTest
    @MethodSource
    public void correctChangePairs(String firstChangelog, String secondChangelog, List<Pair<Integer, Integer>> expected) throws Exception {
        addAndCommitChangelogMd(firstChangelog);
        addAndCommitChangelogMd(secondChangelog);

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit newCommit = revWalk.parseCommit(repository.resolve(Constants.HEAD));
            RevCommit oldCommit = revWalk.parseCommit(newCommit.getParent(0));
            List<Pair<Integer, Integer>> pairs = GitDiffRule.determineCommitModificationRanges(repository, oldCommit, newCommit);
            assertThat(pairs).isEqualTo(expected);
        }
    }

    @Test
    public void fromUnreleasedToNewRelease() throws Exception {
        addAndCommitChangelogMd(CHANGELOG_INITIAL);
        addAndCommitChangelogMd(CHANGELOG_UNRELEASED);
        addAndCommitChangelogMd(CHANGELOG_020);
        assertThat(of(Node.class).descendants(using(changelog)))
                .map(gitDiffRule::validate)
                .filteredOn(Objects::nonNull)
                .isEmpty();
    }

    @Test
    public void everyThingOkWhenNoReleaseFound() throws Exception {
        addAndCommitChangelogMd(CHANGELOG_NO_RELEASE);
        addAndCommitChangelogMd("""
                # Changelog

                All notable changes to this project will be documented in this file.

                The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
                and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

                ## [unreleased]

                ### Added
                                
                - Something added
                """);
        assertThat(of(Node.class).descendants(using(changelog)))
                .map(gitDiffRule::validate)
                .filteredOn(Objects::nonNull)
                .isEmpty();
    }

    public static Stream<Arguments> versionDeterminedCorrectly() {
        return Stream.of(
                Arguments.of(null, CHANGELOG_NO_RELEASE),
                // Can't use import static nbbrd.heylogs.Version.HYPHEN, thus no constant here
                Arguments.of(Version.of("0.1.0", '-', d20230911), CHANGELOG_INITIAL),
                Arguments.of(Version.of("0.1.0", '-', d20230911), CHANGELOG_UNRELEASED),
                Arguments.of(Version.of("0.2.0", '-', d20230912), CHANGELOG_020)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void versionDeterminedCorrectly(Version version, String changelog) throws Exception {
        assertThat(GitDiffRule.getNewestRelease(changelog.getBytes(StandardCharsets.UTF_8)))
                .isEqualTo(version);
    }
}
