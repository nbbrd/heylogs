package _test;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.sys.ProcessReader;
import nbbrd.io.sys.SystemProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class ShadedJarLauncher {

    @StaticFactoryMethod
    public static @NonNull ShadedJarLauncher of(@NonNull Class<?> anchor, @NonNull Predicate<String> fileName) throws URISyntaxException, IOException {
        Path targetFolder = getTargetFolder(anchor).orElseThrow(() -> new IllegalStateException("No target folder found"));
        Path shadedJar = findFileByName(targetFolder, fileName).orElseThrow(() -> new IllegalStateException("No shaded jar found in target folder"));
        return ShadedJarLauncher.builder().shadedJar(shadedJar).build();
    }

    @lombok.Singular
    Map<String, String> envVars;

    @lombok.NonNull
    @lombok.Builder.Default
    Path javaRuntime = initJava();

    @lombok.NonNull
    Path shadedJar;

    public String readString(String... params) throws IOException {
        return ProcessReader.readToString(UTF_8, toProcessBuilder(params).start());
    }

    public List<String> readAllLines(String... params) throws IOException {
        try (BufferedReader reader = ProcessReader.newReader(UTF_8, toProcessBuilder(params).start())) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    private ProcessBuilder toProcessBuilder(String[] params) {
        ProcessBuilder result = new ProcessBuilder(toCommand(params));
        result.environment().clear();
        result.environment().putAll(envVars);
        result.redirectErrorStream(true);
        return result;
    }

    private List<String> toCommand(String[] params) {
        List<String> result = new ArrayList<>();
        result.add(javaRuntime.toString());
        result.add("-jar");
        result.add(shadedJar.toString());
        result.addAll(asList(params));
        return result;
    }

    private static Path initJava() {
        return requireNonNull(SystemProperties.DEFAULT.getJavaHome(), "Java Home not found")
                .resolve("bin")
                .resolve("java");
    }

    @MightBePromoted
    private static Optional<Path> getTargetFolder(Class<?> anchor) throws URISyntaxException {
        Path result = Paths.get(requireNonNull(anchor.getResource(anchor.getSimpleName() + ".class")).toURI());
        while (result != null && !result.getFileName().toString().equals("target")) {
            result = result.getParent();
        }
        return Optional.ofNullable(result);
    }

    @MightBePromoted
    private static Optional<Path> findFileByName(Path folder, Predicate<String> fileNamePredicate) throws IOException {
        try (Stream<Path> stream = Files.find(folder, 1, (file, attr) -> fileNamePredicate.test(file.getFileName().toString()))) {
            return stream.findFirst();
        }
    }
}
