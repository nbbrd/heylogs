package nbbrd.heylogs.ext.forgejo;

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
public final class ForgejoRules implements RuleBatch {

    @Override
    public @NonNull Stream<Rule> getProviders() {
        return Stream.of(FORGEJO_ISSUE_REF, FORGEJO_PULL_REQUEST_REF, FORGEJO_MENTION_REF, FORGEJO_COMMIT_REF);
    }

    @VisibleForTesting
    static final Rule FORGEJO_ISSUE_REF = ForgeRefRuleSupport
            .builder(ForgejoIssueLink::parse, ForgejoIssueRef::parse)
            .id("forgejo-issue-ref")
            .name("Forgejo issue ref")
            .moduleId("forgejo")
            .linkPredicate((link, forgeId) -> link.getType().equals(ForgejoIssueLink.ISSUES_TYPE) && isForgejoHost(link, forgeId))
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting issue ref %s, found %s", ForgejoIssueRef.of(link, ForgejoIssueRef.Type.NUMBER), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting issue ref %s, found %s", ForgejoIssueRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_PULL_REQUEST_REF = ForgeRefRuleSupport
            .builder(ForgejoIssueLink::parse, ForgejoIssueRef::parse)
            .id("forgejo-pull-request-ref")
            .name("Forgejo pull request ref")
            .moduleId("forgejo")
            .linkPredicate((link, forgeId) -> link.getType().equals(ForgejoIssueLink.PULL_REQUEST_TYPE) && isForgejoHost(link, forgeId))
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting pull request ref %s, found %s", ForgejoIssueRef.of(link, ForgejoIssueRef.Type.NUMBER), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting pull request ref %s, found %s", ForgejoIssueRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_MENTION_REF = ForgeRefRuleSupport
            .builder(ForgejoMentionLink::parse, ForgejoMentionRef::parse)
            .id("forgejo-mention-ref")
            .name("Forgejo mention ref")
            .moduleId("forgejo")
            .linkPredicate(ForgejoRules::isForgejoHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting mention ref %s, found %s", ForgejoMentionRef.of(link), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting mention ref %s, found %s", ForgejoMentionRef.of(link), ref))
            .build();

    @VisibleForTesting
    static final Rule FORGEJO_COMMIT_REF = ForgeRefRuleSupport
            .builder(ForgejoCommitLink::parse, ForgejoCommitRef::parse)
            .id("forgejo-commit-ref")
            .name("Forgejo commit ref")
            .moduleId("forgejo")
            .linkPredicate(ForgejoRules::isForgejoHost)
            .parsableMessage((link, ref) -> String.format(ROOT, "Expecting commit ref %s, found %s", ForgejoCommitRef.of(link, ForgejoCommitRef.Type.HASH), ref))
            .compatibleMessage((link, ref) -> String.format(ROOT, "Expecting commit ref %s, found %s", ForgejoCommitRef.of(link, ref.getType()), ref))
            .build();

    @VisibleForTesting
    static boolean isForgejoHost(@NonNull ForgeLink expected, @Nullable String forgeId) {
        return Forgejo.ID.equals(forgeId) || Forgejo.isKnownHost(expected);
    }
}
