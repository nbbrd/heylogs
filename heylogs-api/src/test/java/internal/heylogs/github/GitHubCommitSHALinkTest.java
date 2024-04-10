package internal.heylogs.github;

import org.junit.jupiter.api.Test;

import static internal.heylogs.URLExtractor.urlOf;
import static internal.heylogs.github.GitHubCommitSHALink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class GitHubCommitSHALinkTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .describedAs("missing hash")
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/commit"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid hash")
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd"))
                .withMessage("Invalid path item at index 3: expecting pattern '[0-9a-f]{40}', found '862157d164a8afa1fdd'");

        assertThatIllegalArgumentException()
                .describedAs("missing commit")
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid commit")
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/comit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .withMessage("Invalid path item: expecting [commit], found 'comit'");

        assertThatIllegalArgumentException()
                .describedAs("missing repo")
                .isThrownBy(() -> parse("https://github.com/nbbrd/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid repo")
                .isThrownBy(() -> parse("https://github.com/nbbrd/hey%20logs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .withMessage("Invalid path item at index 1: expecting pattern '[a-z\\d._-]{1,100}', found 'hey logs'");

        assertThatIllegalArgumentException()
                .describedAs("missing owner")
                .isThrownBy(() -> parse("https://github.com/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .withMessage("Invalid path length: expecting [4], found 3");

        assertThatIllegalArgumentException()
                .describedAs("invalid owner")
                .isThrownBy(() -> parse("https://github.com/nbb%20rd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .withMessage("Invalid path item at index 0: expecting pattern '[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}', found 'nbb rd'");

        assertThat(parse("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .returns(urlOf("https://github.com"), GitHubCommitSHALink::getBase)
                .returns("nbbrd", GitHubCommitSHALink::getOwner)
                .returns("heylogs", GitHubCommitSHALink::getRepo)
                .returns("862157d164a8afa1fdd3295c89ceb394efbcb82d", GitHubCommitSHALink::getHash)
                .hasToString("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d");

        assertThat(parse("https://localhost:8080/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .returns(urlOf("https://localhost:8080"), GitHubCommitSHALink::getBase)
                .returns("nbbrd", GitHubCommitSHALink::getOwner)
                .returns("heylogs", GitHubCommitSHALink::getRepo)
                .returns("862157d164a8afa1fdd3295c89ceb394efbcb82d", GitHubCommitSHALink::getHash)
                .hasToString("https://localhost:8080/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d");
    }

}