package nbbrd.heylogs.ext.github;

import internal.heylogs.git.Hash;
import org.junit.jupiter.api.Test;

import static internal.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.github.GitHubCommitLink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static tests.heylogs.spi.ForgeLinkAssert.assertForgeLinkCompliance;

class GitHubCommitLinkTest {

    @Test
    public void testCompliance() {
        assertForgeLinkCompliance(parse(urlOf("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d")));
    }

    @Test
    public void testRepresentable() {
        assertThatIllegalArgumentException()
                .describedAs("missing hash")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/commit")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid hash")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/commit/boom")))
                .withMessage("Invalid path item at index 3: expecting pattern '[0-9a-f]{7,40}', found 'boom'");

        assertThatIllegalArgumentException()
                .describedAs("missing commit")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/862157d164a8afa1fdd3295c89ceb394efbcb82d")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid commit")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/heylogs/comit/862157d164a8afa1fdd3295c89ceb394efbcb82d")))
                .withMessage("Invalid path item: expecting [commit], found 'comit'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbbrd/hey%20logs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d")))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse(urlOf("https://github.com/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d")))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse(urlOf("https://github.com/nbb%20rd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d")))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThat(parse(urlOf("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d")))
                .returns(urlOf("https://github.com"), GitHubCommitLink::getBase)
                .returns("nbbrd", GitHubCommitLink::getOwner)
                .returns("heylogs", GitHubCommitLink::getRepo)
                .returns(Hash.parse("862157d164a8afa1fdd3295c89ceb394efbcb82d"), GitHubCommitLink::getHash)
                .hasToString("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d");

        assertThat(parse(urlOf("https://github.com/nbbRD/heyLOGS/commit/862157d164a8afa1fdd3295c89ceb394efbcb82D")))
                .describedAs("case sensitivity")
                .returns(urlOf("https://github.com"), GitHubCommitLink::getBase)
                .returns("nbbRD", GitHubCommitLink::getOwner)
                .returns("heyLOGS", GitHubCommitLink::getRepo)
                .returns(Hash.parse("862157d164a8afa1fdd3295c89ceb394efbcb82D"), GitHubCommitLink::getHash)
                .hasToString("https://github.com/nbbRD/heyLOGS/commit/862157d164a8afa1fdd3295c89ceb394efbcb82D");

        assertThat(parse(urlOf("https://localhost:8080/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d")))
                .returns(urlOf("https://localhost:8080"), GitHubCommitLink::getBase)
                .returns("nbbrd", GitHubCommitLink::getOwner)
                .returns("heylogs", GitHubCommitLink::getRepo)
                .returns(Hash.parse("862157d164a8afa1fdd3295c89ceb394efbcb82d"), GitHubCommitLink::getHash)
                .hasToString("https://localhost:8080/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d");
    }

}