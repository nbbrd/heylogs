package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeRefRuleSupport;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.service.ServiceProvider;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

import static java.util.Locale.ROOT;

@DirectImpl
@ServiceProvider
public final class GitLabRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(GITLAB_COMMIT_REF);
    }

    @VisibleForTesting
    static final Rule GITLAB_COMMIT_REF = ForgeRefRuleSupport
            .builder(GitLabCommitLink::parse, GitLabCommitRef::parse)
            .id("gitlab-commit-ref")
            .name("GitLab commit ref")
            .category("forge")
            .linkPredicate(GitLabRules::isGitLabHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting commit ref %s, found %s", GitLabCommitRef.of(link, GitLabCommitRef.Type.HASH), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting commit ref %s, found %s", GitLabCommitRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static boolean isGitLabHost(@NonNull ForgeLink expected, @Nullable String forgeId) {
        return GitLab.ID.equals(forgeId) || GitLab.isKnownHost(expected);
    }
}
