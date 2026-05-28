package nbbrd.heylogs.ext.gitlab;

import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.RepresentableAs;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import static nbbrd.heylogs.spi.URLExtractor.urlOf;
import static nbbrd.heylogs.ext.gitlab.GitLabSupport.parseLink;

// https://docs.gitlab.com/user/project/releases/
@RepresentableAs(URL.class)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
class GitLabTagLink implements GitLabProjectLink {

    @StaticFactoryMethod
    public static @NonNull GitLabTagLink parse(@NonNull URL url) {
        return parseLink(GitLabTagLink::new, TAGS_KEYWORD, TAG_PATTERN, tag -> tag, url);
    }

    @NonNull
    URL base;

    @NonNull
    List<String> namespace;

    @NonNull
    String project;

    @NonNull
    String tag;

    @Override
    public String toString() {
        return toURL().toString();
    }

    @Override
    public @NonNull URL toURL() {
        return urlOf(GitLabSupport.linkToString(base, namespace, project, TAGS_KEYWORD, tag));
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    private static final String TAGS_KEYWORD = "tags";
    static final Pattern TAG_PATTERN = Pattern.compile("[a-z\\d][a-z\\d._+\\-]*", Pattern.CASE_INSENSITIVE);
}

