package internal.heylogs.github;

import internal.heylogs.GitHostLink;
import org.junit.jupiter.api.Test;

import static internal.heylogs.github.GitHubCommitSHALink.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class GitHubCommitSHALinkTest {

    @Test
    public void testRepresentableAsString() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/commit"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("https://github.com/nbbrd/heylogs/862157d164a8afa1fdd3295c89ceb394efbcb82d"));

        assertThat(parse("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .returns(GitHostLink.urlOf("https://github.com"), GitHubCommitSHALink::getBase)
                .returns("nbbrd", GitHubCommitSHALink::getOwner)
                .returns("heylogs", GitHubCommitSHALink::getRepo)
                .returns("862157d164a8afa1fdd3295c89ceb394efbcb82d", GitHubCommitSHALink::getHash)
                .hasToString("https://github.com/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d");

        assertThat(parse("https://localhost:8080/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d"))
                .returns(GitHostLink.urlOf("https://localhost:8080"), GitHubCommitSHALink::getBase)
                .returns("nbbrd", GitHubCommitSHALink::getOwner)
                .returns("heylogs", GitHubCommitSHALink::getRepo)
                .returns("862157d164a8afa1fdd3295c89ceb394efbcb82d", GitHubCommitSHALink::getHash)
                .hasToString("https://localhost:8080/nbbrd/heylogs/commit/862157d164a8afa1fdd3295c89ceb394efbcb82d");
    }

}