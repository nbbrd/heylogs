package internal.heylogs.github;

import internal.heylogs.URLExtractor;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Forge;
import nbbrd.io.http.URLQueryBuilder;
import nbbrd.io.text.Parser;
import nbbrd.service.ServiceProvider;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
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
    public boolean isCompareLink(@NonNull CharSequence text) {
        return Parser.of(GitHubCompareLink::parse).parseValue(text).isPresent();
    }

    @Override
    public @NonNull URL getBaseURL(@NonNull CharSequence text) {
        GitHubCompareLink compareLink = GitHubCompareLink.parse(text);
        try {
            return URLQueryBuilder
                    .of(compareLink.getBase())
                    .path(compareLink.getOwner())
                    .path(compareLink.getRepo())
                    .build();
        } catch (MalformedURLException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public @NonNull URL getCompareLink(@NonNull URL latest, @NonNull String nextTag) {
        return URLExtractor.urlOf(GitHubCompareLink.parse(latest.toString()).derive(nextTag).toString());
    }
}
