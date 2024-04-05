package internal.heylogs.github;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;

@DirectImpl
@ServiceProvider
public final class GitHub implements Forge {

    @Override
    public @NonNull String getForgeId() {
        return "github";
    }

    @Override
    public @NonNull String getForgeName() {
        return "GitHub";
    }

    @Override
    public boolean isCompareLink(@NonNull CharSequence text) {
        return Parser.of(GitHubCompareLink::parse).parseValue(text).isPresent();
    }
}
