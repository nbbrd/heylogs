package internal.heylogs.base;

import nbbrd.design.MightBePromoted;

// https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
@MightBePromoted
final class AnsiCodes {

    private AnsiCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";

    /**
     * Returns {@code true} when ANSI escape codes should be emitted.
     * Respects the {@code org.fusesource.jansi.Ansi.disable} system property
     * set by {@code SpecialProperties#disableAnsi}.
     */
    static boolean isEnabled() {
        return !Boolean.parseBoolean(System.getProperty("org.fusesource.jansi.Ansi.disable", "false"));
    }

    static String bold(String text) {
        return isEnabled() ? BOLD + text + RESET : text;
    }

    static String red(String text) {
        return isEnabled() ? RED + text + RESET : text;
    }

    static String green(String text) {
        return isEnabled() ? GREEN + text + RESET : text;
    }

    static String yellow(String text) {
        return isEnabled() ? YELLOW + text + RESET : text;
    }

    static String stripAll(String text) {
        return text.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    static int visibleLength(CharSequence text) {
        return stripAll(text.toString()).length();
    }
}


