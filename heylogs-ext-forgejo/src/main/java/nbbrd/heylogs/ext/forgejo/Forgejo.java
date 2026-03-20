package nbbrd.heylogs.ext.forgejo;

import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.heylogs.spi.ForgeSupport;
import nbbrd.service.ServiceProvider;

import static nbbrd.heylogs.spi.ForgeLinkType.*;

@DirectImpl
@ServiceProvider
public final class Forgejo implements Forge {

    @lombok.experimental.Delegate
    private final Forge delegate = ForgeSupport
            .builder()
            .id("forgejo")
            .moduleId("forgejo")
            .name("Forgejo")
            .knownHostPredicate(ForgeSupport.onHostContaining("codeberg"))
            .parser(COMMIT, ForgejoCommitLink::parse, ForgejoCommitRef::parse)
            .parser(ISSUE, ForgejoIssueLink::parse, ForgejoIssueRef::parse)
            .parser(REQUEST, ForgejoRequestLink::parse, ForgejoRequestRef::parse)
            .parser(MENTION, ForgejoMentionLink::parse, ForgejoMentionRef::parse)
            .linkParser(COMPARE, ForgejoCompareLink::parse)
            .linkResolver(ISSUE, ForgejoIssueLink::resolve)
            .linkResolver(REQUEST, ForgejoRequestLink::resolve)
            .messageFetcher(ISSUE, ForgejoMessageFetcher.ISSUE)
            .messageFetcher(REQUEST, ForgejoMessageFetcher.REQUEST)
            .build();
}
