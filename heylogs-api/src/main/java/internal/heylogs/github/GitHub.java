package internal.heylogs.github;

import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.service.ServiceProvider;

import java.net.URL;

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
    public boolean isCompareLink(@NonNull URL url) {
        try {
            GitHubCompareLink.parse(url);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public @NonNull URL getProjectURL(@NonNull URL url) {
        return GitHubCompareLink.parse(url).getProjectURL();
    }

    @Override
    public @NonNull URL deriveCompareLink(@NonNull URL latest, @NonNull String nextTag) {
        return GitHubCompareLink.parse(latest).derive(nextTag).toURL();
    }
}
