package internal.heylogs;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.heylogs.Config;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Loads heylogs configuration from heylogs.properties files in the directory hierarchy.
 * Similar to lombok.config, this loader walks up the directory tree to discover configuration
 * files and merges them from parent to child (child overrides parent).
 */
public final class ConfigFileLoader {

    private ConfigFileLoader() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The name of the configuration file.
     */
    public static final String CONFIG_FILE_NAME = "heylogs.properties";

    /**
     * Property key to stop looking for parent configuration files.
     */
    public static final String STOP_BUBBLING_KEY = "config.stopBubbling";

    /**
     * Loads configuration from heylogs.properties files starting from the given directory.
     * Walks up the directory tree and merges configurations from parent to child.
     *
     * @param startDirectory the directory to start searching from
     * @return the merged configuration, or Config.DEFAULT if no config files found
     */
    public static @NonNull Config loadConfig(@Nullable Path startDirectory) {
        return loadConfigWithDefaults(startDirectory, Config.DEFAULT);
    }

    /**
     * Loads configuration from heylogs.properties files starting from the given directory,
     * using the provided defaults as the base configuration.
     *
     * @param startDirectory the directory to start searching from
     * @param defaults       the default configuration to use as base
     * @return the merged configuration
     */
    public static @NonNull Config loadConfigWithDefaults(@Nullable Path startDirectory, @NonNull Config defaults) {
        if (startDirectory == null) {
            return defaults;
        }

        List<Path> configFiles = discoverConfigFiles(startDirectory);
        if (configFiles.isEmpty()) {
            return defaults;
        }

        Config result = defaults;
        for (Path configFile : configFiles) {
            try {
                Config fileConfig = loadConfigFromFile(configFile);
                result = result.mergeWith(fileConfig);
            } catch (IOException ex) {
                // Log warning and continue - config files are optional
                System.err.println("Warning: Failed to load config file " + configFile + ": " + ex.getMessage());
            }
        }

        return result;
    }

    /**
     * Discovers all heylogs.properties files in the directory hierarchy,
     * walking up from the start directory to the filesystem root.
     * Returns files in order from root to start directory (parent to child).
     *
     * @param startDirectory the directory to start searching from
     * @return list of config files in parent-to-child order
     */
    @MightBePromoted
    private static @NonNull List<Path> discoverConfigFiles(@NonNull Path startDirectory) {
        List<Path> result = new ArrayList<>();
        Path current = startDirectory.toAbsolutePath().normalize();

        while (current != null) {
            Path configFile = current.resolve(CONFIG_FILE_NAME);
            if (Files.isRegularFile(configFile)) {
                result.add(configFile);

                // Check if this file has stopBubbling set
                if (shouldStopBubbling(configFile)) {
                    break;
                }
            }
            current = current.getParent();
        }

        // Reverse to get parent-to-child order
        Collections.reverse(result);
        return result;
    }

    /**
     * Checks if a config file contains config.stopBubbling=true.
     *
     * @param configFile the config file to check
     * @return true if bubbling should stop
     */
    private static boolean shouldStopBubbling(@NonNull Path configFile) {
        try {
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(configFile)) {
                properties.load(input);
            }
            return Boolean.parseBoolean(properties.getProperty(STOP_BUBBLING_KEY, "false"));
        } catch (IOException ex) {
            // If we can't read the file, don't stop bubbling
            return false;
        }
    }

    /**
     * Loads configuration from a single properties file.
     *
     * @param configFile the properties file to load
     * @return the configuration
     * @throws IOException if the file cannot be read
     */
    private static @NonNull Config loadConfigFromFile(@NonNull Path configFile) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(configFile)) {
            properties.load(input);
        }
        return ConfigProperties.fromProperties(properties);
    }
}

