package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.heylogs.spi.ProjectLink;
import nbbrd.io.http.URLQueryBuilder;

import java.net.URL;
import java.util.List;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;

public interface GitLabProjectLink extends ProjectLink {

    @NonNull
    URL getBase();

    @NonNull
    List<String> getNamespace();

    @NonNull
    String getProject();

    @Override
    default @NonNull URL getProjectURL() {
        return urlOf(URLQueryBuilder.of(getBase()).path(getNamespace()).path(getProject()).toString());
    }
}
