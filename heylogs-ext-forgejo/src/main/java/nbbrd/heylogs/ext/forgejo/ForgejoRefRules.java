package nbbrd.heylogs.ext.forgejo;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.ForgeRefRuleSupport;
import nbbrd.heylogs.spi.ForgeRefType;
import nbbrd.heylogs.spi.Rule;
import nbbrd.heylogs.spi.RuleBatch;
import nbbrd.service.ServiceProvider;

import java.util.stream.Stream;

@DirectImpl
@ServiceProvider
public final class ForgejoRefRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(FORGEJO_ISSUE_REF, FORGEJO_PULL_REQUEST_REF, FORGEJO_MENTION_REF, FORGEJO_COMMIT_REF);
    }

    @VisibleForTesting
    static final Rule FORGEJO_ISSUE_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(ForgejoIssueLink::parse)
            .refParser(ForgejoIssueRef::parse)
            .refType(ForgeRefType.ISSUE)
            .id("fj-issue-ref")
            .name("Forgejo issue ref")
            .moduleId("forgejo")
            .forgeId(Forgejo.ID)
            .linkPredicate(Forgejo::isForgejoHost)
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_PULL_REQUEST_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(ForgejoRequestLink::parse)
            .refParser(ForgejoRequestRef::parse)
            .refType(ForgeRefType.REQUEST)
            .id("fj-pull-request-ref")
            .name("Forgejo pull request ref")
            .moduleId("forgejo")
            .forgeId(Forgejo.ID)
            .linkPredicate(Forgejo::isForgejoHost)
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_MENTION_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(ForgejoMentionLink::parse)
            .refParser(ForgejoMentionRef::parse)
            .refType(ForgeRefType.MENTION)
            .id("fj-mention-ref")
            .name("Forgejo mention ref")
            .moduleId("forgejo")
            .forgeId(Forgejo.ID)
            .linkPredicate(Forgejo::isForgejoHost)
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_COMMIT_REF = ForgeRefRuleSupport
            .builder()
            .linkParser(ForgejoCommitLink::parse)
            .refParser(ForgejoCommitRef::parse)
            .refType(ForgeRefType.COMMIT)
            .id("fj-commit-ref")
            .name("Forgejo commit ref")
            .moduleId("forgejo")
            .forgeId(Forgejo.ID)
            .linkPredicate(Forgejo::isForgejoHost)
            .build();
}
