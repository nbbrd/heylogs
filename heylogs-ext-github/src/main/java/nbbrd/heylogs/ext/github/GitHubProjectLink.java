package nbbrd.heylogs.ext.github;

import lombok.NonNull;
import nbbrd.heylogs.spi.ProjectLink;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.regex.Pattern;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;

interface GitHubProjectLink extends ProjectLink {

    @NonNull
    URL getBase();

    @NonNull
    String getOwner();

    @NonNull
    String getRepo();

    @Override
    default @NonNull URL getProjectURL() {
        return urlOf(URLQueryBuilder.of(getBase()).path(getOwner()).path(getRepo()).toString());
    }

    Pattern OWNER_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    Pattern REPO_PATTERN = Pattern.compile("[a-z\\d._-]{1,100}", Pattern.CASE_INSENSITIVE);
}
