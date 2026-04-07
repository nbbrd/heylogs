package internal.heylogs;

import lombok.NonNull;
import nbbrd.heylogs.Config;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PomParser {

    private PomParser() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final Pattern MODULES_PATTERN = Pattern.compile("<modules>\\s*(.+?)\\s*</modules>", Pattern.DOTALL);
    private static final Pattern MODULE_PATTERN = Pattern.compile("<module>\\s*(.+?)\\s*</module>");

    private static final String MAVEN_PLUGIN_ARTIFACT = "heylogs-maven-plugin";

    public static @NonNull List<String> extractModules(@NonNull Path pomFile) throws IOException {
        String content = new String(Files.readAllBytes(pomFile), StandardCharsets.UTF_8);
        List<String> modules = new ArrayList<>();

        Matcher modulesMatcher = MODULES_PATTERN.matcher(content);
        if (modulesMatcher.find()) {
            String modulesContent = modulesMatcher.group(1);
            Matcher moduleMatcher = MODULE_PATTERN.matcher(modulesContent);
            while (moduleMatcher.find()) {
                modules.add(moduleMatcher.group(1).trim());
            }
        }

        return modules;
    }

    public static @NonNull Config extractHeylogsConfig(@NonNull Path pomFile) throws IOException {
        String content = new String(Files.readAllBytes(pomFile), StandardCharsets.UTF_8);
        Config.Builder builder = Config.builder();

        // Extract from maven plugin
        String pluginConfig = extractPluginConfig(content);
        if (pluginConfig != null) {
            parseConfiguration(pluginConfig, builder);
        }

        // Extract from enforcer rules
        String enforcerConfig = extractEnforcerRulesConfig(content);
        if (enforcerConfig != null) {
            parseConfiguration(enforcerConfig, builder);
        }

        return builder.build();
    }

    public static void removeHeylogsConfig(@NonNull Path pomFile) throws IOException {
        String content = new String(Files.readAllBytes(pomFile), StandardCharsets.UTF_8);

        // Remove maven plugin configuration
        content = removePluginConfig(content);

        // Remove enforcer rules configuration
        content = removeEnforcerRulesConfig(content);

        Files.write(pomFile, content.getBytes(StandardCharsets.UTF_8));
    }

    private static @Nullable String extractPluginConfig(@NonNull String pomContent) {
        return extractPluginConfig(pomContent, MAVEN_PLUGIN_ARTIFACT);
    }

    private static @Nullable String extractPluginConfig(@NonNull String pomContent, @NonNull String artifactId) {
        Pattern pluginPattern = Pattern.compile(
                "<plugin>\\s*<groupId>com\\.github\\.nbbrd\\.heylogs</groupId>\\s*<artifactId>" +
                Pattern.quote(artifactId) + "</artifactId>.*?<configuration>\\s*(.+?)\\s*</configuration>.*?</plugin>",
                Pattern.DOTALL
        );
        Matcher matcher = pluginPattern.matcher(pomContent);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static @Nullable String extractEnforcerRulesConfig(@NonNull String pomContent) {
        // Look for heylogs-enforcer-rules in enforcer plugin
        Pattern pattern = Pattern.compile(
                "<rule>\\s*<heylogsRule>\\s*<configuration>\\s*(.+?)\\s*</configuration>\\s*</heylogsRule>\\s*</rule>",
                Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(pomContent);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static @NonNull String removePluginConfig(@NonNull String pomContent) {
        return removePluginConfig(pomContent, MAVEN_PLUGIN_ARTIFACT);
    }

    private static @NonNull String removePluginConfig(@NonNull String pomContent, @NonNull String artifactId) {
        Pattern configPattern = Pattern.compile(
                "(<plugin>\\s*<groupId>com\\.github\\.nbbrd\\.heylogs</groupId>\\s*<artifactId>" +
                Pattern.quote(artifactId) + "</artifactId>.*?)<configuration>.*?</configuration>(.*?</plugin>)",
                Pattern.DOTALL
        );
        return configPattern.matcher(pomContent).replaceAll("$1$2");
    }

    private static @NonNull String removeEnforcerRulesConfig(@NonNull String pomContent) {
        Pattern configPattern = Pattern.compile(
                "(<rule>\\s*<heylogsRule>\\s*)<configuration>.*?</configuration>(\\s*</heylogsRule>\\s*</rule>)",
                Pattern.DOTALL
        );
        return configPattern.matcher(pomContent).replaceAll("$1$2");
    }

    private static void parseConfiguration(@NonNull String configContent, @NonNull Config.Builder builder) {
        builder.versioningOf(extractTag(configContent, "versioning"));
        builder.taggingOf(extractTag(configContent, "tagging"));
        builder.forgeOf(extractTag(configContent, "forge"));

        List<String> rules = extractTags(configContent, "rule");
        if (!rules.isEmpty()) {
            builder.rulesOf(rules);
        }

        List<String> domains = extractTags(configContent, "domain");
        if (!domains.isEmpty()) {
            builder.domainsOf(domains);
        }
    }

    private static @Nullable String extractTag(@NonNull String content, @NonNull String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">\\s*(.+?)\\s*</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private static @NonNull List<String> extractTags(@NonNull String content, @NonNull String tagName) {
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile("<" + tagName + ">\\s*(.+?)\\s*</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            results.add(matcher.group(1).trim());
        }
        return results;
    }
}
