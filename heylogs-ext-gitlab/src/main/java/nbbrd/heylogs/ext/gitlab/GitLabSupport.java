package nbbrd.heylogs.ext.gitlab;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.spi.ForgeLink;
import nbbrd.heylogs.spi.ForgeRef;
import nbbrd.io.http.URLQueryBuilder;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static internal.heylogs.spi.URLExtractor.*;

// https://docs.gitlab.com/user/markdown/#gitlab-specific-references
// https://docs.gitlab.com/user/reserved_names/#limitations-on-usernames-project-and-group-names
class GitLabSupport {

    private GitLabSupport() {
        // Utility class, no instances allowed
    }

    // https://docs.gitlab.com/user/reserved_names/#rules-for-usernames-project-and-group-names-and-slugs
    // FIXME: This regex is not perfect, it allows some invalid names.
    static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}", Pattern.CASE_INSENSITIVE);
    static final Pattern PROJECT_PATTERN = Pattern.compile("[a-z\\d._-]{1,100}", Pattern.CASE_INSENSITIVE);
    static final Pattern HASH_PATTERN = Pattern.compile("[0-9a-f]{7,40}", Pattern.CASE_INSENSITIVE);
    static final Pattern OID_PATTERN = Pattern.compile(".+\\.{3}.+", Pattern.CASE_INSENSITIVE);
    static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);
    static final String DASH_KEYWORD = "-";
    static final String PATH_SEPARATOR = "/";

    @MightBePromoted
    private static List<String> unmodifiableList(String[] array, int from, int to) {
        return Collections.unmodifiableList(Arrays.asList(Arrays.copyOfRange(array, from, to)));
    }

    @FunctionalInterface
    interface GitLabLinkFactory<L extends ForgeLink> {

        @NonNull
        L create(@NonNull URL base, @NonNull List<String> namespace, @NonNull String project, @NonNull String value);
    }

    static <L extends ForgeLink> @NonNull L parseLink(@NonNull GitLabLinkFactory<L> factory, @NonNull String typeKeyword, @NonNull Pattern typePattern, @NonNull URL url) {
        String[] pathArray = getPathArray(url);

        int length = pathArray.length;

        if (length < 4) {
            throw new IllegalArgumentException("GitLab " + typeKeyword + " number link must have at least 4 path segments");
        }

        if (length == 4 && pathArray[length - 3].equals(DASH_KEYWORD)) {
            throw new IllegalArgumentException("GitLab " + typeKeyword + " number link must have a project name, not just '-'");
        }

        int valueIndex = length - 1;
        int typeIndex = length - 2;
        int projectIndex = pathArray[length - 3].equals(DASH_KEYWORD) ? length - 4 : length - 3;

        checkPathItem(pathArray, valueIndex, typePattern);
        checkPathItem(pathArray, typeIndex, typeKeyword);
        checkPathItem(pathArray, projectIndex, PROJECT_PATTERN);
        for (int i = projectIndex - 1; i >= 0; i--) {
            checkPathItem(pathArray, i, NAMESPACE_PATTERN);
        }

        return factory.create(baseOf(url), unmodifiableList(pathArray, 0, projectIndex), pathArray[projectIndex], pathArray[valueIndex]);
    }

    public static @NonNull String linkToString(@NonNull URL base, @NonNull List<String> namespace, @NonNull String project, @NonNull String type, @NonNull String value) {
        return URLQueryBuilder.of(base).path(namespace).path(project).path(DASH_KEYWORD).path(type).path(value).toString();
    }

    @FunctionalInterface
    interface GitLabRefFactory<R extends ForgeRef<?>> {

        @NonNull
        R create(@Nullable List<String> namespace, @Nullable String project, @NonNull String value);
    }

    public static <R extends ForgeRef<?>> @NonNull R parseRef(@NonNull GitLabRefFactory<R> factory, char typeSeparator, @NonNull Pattern typePattern, boolean enforceTypeSeparator, @NonNull CharSequence text) {
        String textString = text.toString();

        int separatorIndex = textString.lastIndexOf(typeSeparator);
        if (separatorIndex == -1) {
            if (enforceTypeSeparator)
                throw new IllegalArgumentException("Missing type separator '" + typeSeparator + "' in reference: " + textString);
            if (!typePattern.matcher(textString).matches())
                throw new IllegalArgumentException("Invalid commit reference: " + textString);
            return factory.create(null, null, textString);
        }

        String value = textString.substring(separatorIndex + 1);
        if (!typePattern.matcher(value).matches())
            throw new IllegalArgumentException("Invalid type in reference: " + textString);

        String[] parts = separatorIndex > 0 ? textString.substring(0, separatorIndex).split(Pattern.quote(PATH_SEPARATOR), -1) : new String[0];
        switch (parts.length) {
            case 0:
                return factory.create(null, null, value);
            case 1:
                checkPathItem(parts, 0, GitLabSupport.PROJECT_PATTERN);
                return factory.create(null, parts[0], value);
            default:
                int projectIndex = parts.length - 1;
                checkPathItem(parts, projectIndex, GitLabSupport.PROJECT_PATTERN);
                for (int i = projectIndex - 1; i >= 0; i--) {
                    checkPathItem(parts, i, GitLabSupport.NAMESPACE_PATTERN);
                }
                return factory.create(unmodifiableList(parts, 0, parts.length - 1), parts[parts.length - 1], value);
        }
    }

    public static @NonNull String refToString(@Nullable List<String> namespace, @Nullable String project, char separator, @NonNull String value) {
        switch (getRefType(namespace, project)) {
            case SAME_PROJECT:
                return value;
            case SAME_NAMESPACE:
                return project + separator + value;
            case CROSS_PROJECT:
                return String.join(PATH_SEPARATOR, namespace) + PATH_SEPARATOR + project + separator + value;
            default:
                throw new RuntimeException();
        }
    }

    public static @NonNull GitLabRefType getRefType(@Nullable List<String> namespace, @Nullable String project) {
        return project != null ? (namespace != null ? GitLabRefType.CROSS_PROJECT : GitLabRefType.SAME_NAMESPACE) : GitLabRefType.SAME_PROJECT;
    }
}
