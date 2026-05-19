package nbbrd.heylogs.ext.calver;

import internal.heylogs.ext.calver.CalVerFormat;
import internal.heylogs.ext.calver.CalVerTag;
import nbbrd.design.DirectImpl;
import nbbrd.heylogs.spi.Validator;
import nbbrd.heylogs.spi.Versioning;
import nbbrd.heylogs.spi.VersioningSupport;
import nbbrd.service.ServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static nbbrd.heylogs.spi.VersioningSupport.compilingArg;

@DirectImpl
@ServiceProvider
public final class CalVer implements Versioning {

    @lombok.experimental.Delegate
    private final Versioning delegate = VersioningSupport
            .builder()
            .id("calver")
            .name("Calendar Versioning")
            .urlOf("https://calver.org/")
            .moduleId("calver")
            .validator(Validator.of(CalVerFormat::parse))
            .predicate(compilingArg(CalVerFormat::parse, CalVerFormat::isValidVersion))
            .comparator(arg -> {
                CalVerFormat format = CalVerFormat.parse(Objects.requireNonNull(arg));
                return (a, b) -> compare(format, a, b);
            })
            .familyMapper(arg -> {
                CalVerFormat format = CalVerFormat.parse(Objects.requireNonNull(arg));
                return version -> toFamily(format, version);
            })
            .build();

    private static int compare(CalVerFormat format, CharSequence a, CharSequence b) {
        List<Integer> aParts = parseVersion(format, a);
        List<Integer> bParts = parseVersion(format, b);

        if (aParts == null || bParts == null) {
            return 0; // incomparable
        }

        for (int i = 0; i < Math.min(aParts.size(), bParts.size()); i++) {
            int cmp = Integer.compare(aParts.get(i), bParts.get(i));
            if (cmp != 0) return cmp;
        }

        return Integer.compare(aParts.size(), bParts.size());
    }

    private static String toFamily(CalVerFormat format, CharSequence version) {
        List<Integer> parts = parseVersion(format, version);
        if (parts == null) return null;

        // Family consists of date components (exclude MAJOR, MINOR, MICRO)
        List<CalVerTag> tags = format.getTokens().stream()
                .filter(CalVerTag.class::isInstance)
                .map(CalVerTag.class::cast)
                .collect(java.util.stream.Collectors.toList());

        StringBuilder family = new StringBuilder();
        for (int i = 0; i < tags.size() && i < parts.size(); i++) {
            CalVerTag tag = tags.get(i);
            if (tag != CalVerTag.TAG_MAJOR && tag != CalVerTag.TAG_MINOR && tag != CalVerTag.TAG_MICRO) {
                if (family.length() > 0) family.append('.');
                family.append(parts.get(i));
            } else {
                // Stop at first non-date component
                break;
            }
        }

        return family.length() > 0 ? family.toString() : null;
    }

    private static final Pattern NON_NUMERIC = Pattern.compile("\\D+");

    private static List<Integer> parseVersion(CalVerFormat format, CharSequence version) {
        if (!format.isValidVersion(version)) {
            return null;
        }

        String[] tagValues = NON_NUMERIC.split(version, -1);
        List<Integer> result = new ArrayList<>();

        for (String value : tagValues) {
            if (!value.isEmpty()) {
                result.add(Integer.parseInt(value));
            }
        }

        return result;
    }
}
