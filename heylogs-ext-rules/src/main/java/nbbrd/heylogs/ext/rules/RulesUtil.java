package nbbrd.heylogs.ext.rules;

final class RulesUtil {

    private RulesUtil() {
    }

    static String truncate(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength) + "\u2026" : text;
    }
}

