package nbbrd.heylogs.ext.forgejo;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.VisibleForTesting;
import nbbrd.heylogs.spi.*;
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
            .builder(ForgeRefFactory.of(ForgejoIssueLink::parse, ForgejoIssueRef::parse, ForgejoIssueRef::of))
            .refType(ForgeRefType.ISSUE)
            .id("fj-issue-ref")
            .name("Forgejo issue ref")
            .moduleId("forgejo")
            .forgeId(Forgejo.ID)
            .linkPredicate(Forgejo::isKnownHost)
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_PULL_REQUEST_REF = ForgeRefRuleSupport
            .builder(ForgeRefFactory.of(ForgejoRequestLink::parse, ForgejoRequestRef::parse, ForgejoRequestRef::of))
            .refType(ForgeRefType.REQUEST)
            .id("fj-pull-request-ref")
            .name("Forgejo pull request ref")
            .moduleId("forgejo")
            .forgeId(Forgejo.ID)
            .linkPredicate(Forgejo::isKnownHost)
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_MENTION_REF = ForgeRefRuleSupport
            .builder(ForgeRefFactory.of(ForgejoMentionLink::parse, ForgejoMentionRef::parse, ForgejoMentionRef::of))
            .refType(ForgeRefType.MENTION)
            .id("fj-mention-ref")
            .name("Forgejo mention ref")
            .moduleId("forgejo")
            .forgeId(Forgejo.ID)
            .linkPredicate(Forgejo::isKnownHost)
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_COMMIT_REF = ForgeRefRuleSupport
            .builder(ForgeRefFactory.of(ForgejoCommitLink::parse, ForgejoCommitRef::parse, ForgejoCommitRef::of))
            .refType(ForgeRefType.COMMIT)
            .id("fj-commit-ref")
            .name("Forgejo commit ref")
            .moduleId("forgejo")
            .forgeId(Forgejo.ID)
            .linkPredicate(Forgejo::isKnownHost)
            .build();
}
