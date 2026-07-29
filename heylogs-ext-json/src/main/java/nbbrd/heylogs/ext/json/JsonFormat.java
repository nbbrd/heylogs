package nbbrd.heylogs.ext.json;

import com.google.gson.*;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBeGenerated;
import nbbrd.heylogs.*;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatSupport;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.toList;

@DirectImpl
@ServiceProvider
public final class JsonFormat implements Format {

    public static final String ID = "json";

    @lombok.experimental.Delegate
    private final FormatSupport delegate = FormatSupport
            .builder()
            .id(ID)
            .name("JSON-serialized output")
            .moduleId("json")
            .problems(this::format)
            .status(this::format)
            .resources(this::format)
            .content(JsonFormat::formatChangelogContent)
            .contentParser(JsonFormat::parseChangelogContent)
            .filterByExtension(".json")
            .build();

    private void format(Appendable appendable, List<?> list) throws IOException {
        try {
            GSON.toJson(list, appendable);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        }
    }

    private static void formatChangelogContent(Appendable appendable, ChangelogContent content) throws IOException {
        try {
            JsonElement element = CONTENT_GSON.toJsonTree(content, ChangelogContent.class);
            CONTENT_GSON.toJson(element, appendable);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        }
        appendable.append('\n');
    }

    private static ChangelogContent parseChangelogContent(Reader reader) throws IOException {
        try {
            ChangelogContent result = CONTENT_GSON.fromJson(reader, ChangelogContent.class);
            if (result == null) {
                throw new IOException("Empty or null content");
            }
            return result;
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        } catch (JsonSyntaxException ex) {
            throw new IOException("Invalid JSON content", ex);
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Check.class, (JsonSerializer<Check>) JsonFormat::serializeCheck)
            .registerTypeAdapter(Check.class, (JsonDeserializer<Check>) JsonFormat::deserializeCheck)
            .registerTypeAdapter(Problem.class, (JsonSerializer<Problem>) JsonFormat::serializeProblem)
            .registerTypeAdapter(Problem.class, (JsonDeserializer<Problem>) JsonFormat::deserializeProblem)
            .registerTypeAdapter(Scan.class, (JsonSerializer<Scan>) JsonFormat::serializeScan)
            .registerTypeAdapter(Scan.class, (JsonDeserializer<Scan>) JsonFormat::deserializeScan)
            .registerTypeAdapter(TimeRange.class, (JsonSerializer<TimeRange>) JsonFormat::serializeTimeRange)
            .registerTypeAdapter(TimeRange.class, (JsonDeserializer<TimeRange>) JsonFormat::deserializeTimeRange)
            .setPrettyPrinting()
            .create();

    private static final Gson CONTENT_GSON = new GsonBuilder()
            .registerTypeAdapter(ChangelogContent.class, (JsonSerializer<ChangelogContent>) JsonFormat::serializeChangelogContent)
            .registerTypeAdapter(ChangelogContent.class, (JsonDeserializer<ChangelogContent>) JsonFormat::deserializeChangelogContent)
            .registerTypeAdapter(ChangelogContent.VersionContent.class, (JsonSerializer<ChangelogContent.VersionContent>) JsonFormat::serializeVersionContent)
            .registerTypeAdapter(ChangelogContent.VersionContent.class, (JsonDeserializer<ChangelogContent.VersionContent>) JsonFormat::deserializeVersionContent)
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    @MightBeGenerated
    private static JsonElement serializeCheck(Check src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("filePath", src.getSource());
        JsonArray messages = new JsonArray();
        src.getProblems().stream().map(message -> serializeProblem(message, Problem.class, context)).forEach(messages::add);
        result.add("messages", messages);
        return result;
    }

    @MightBeGenerated
    private static Check deserializeCheck(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject x = json.getAsJsonObject();
        return Check
                .builder()
                .source(x.get("filePath").getAsString())
                .problems(x.get("messages").getAsJsonArray().asList().stream().map(e -> deserializeProblem(e, Problem.class, context)).collect(toList()))
                .build();
    }

    @MightBeGenerated
    private static JsonElement serializeProblem(Problem src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("ruleId", src.getId());
        result.addProperty("severity", src.getSeverity().toCode());
        result.addProperty("message", src.getIssue().getMessage());
        result.addProperty("line", src.getIssue().getLine());
        result.addProperty("column", src.getIssue().getColumn());
        return result;
    }

    @MightBeGenerated
    private static Problem deserializeProblem(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject x = json.getAsJsonObject();
        return Problem
                .builder()
                .id(x.get("ruleId").getAsString())
                .severity(RuleSeverity.parseCode(x.get("severity").getAsInt()))
                .issue(RuleIssue
                        .builder()
                        .message(x.get("message").getAsString())
                        .line(x.get("line").getAsInt())
                        .column(x.get("column").getAsInt())
                        .build())
                .build();
    }

    @MightBeGenerated
    private static JsonElement serializeScan(Scan src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("filePath", src.getSource());
        result.add("summary", context.serialize(src.getSummary()));
        return result;
    }

    @MightBeGenerated
    private static Scan deserializeScan(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject x = json.getAsJsonObject();
        return Scan
                .builder()
                .source(x.get("filePath").getAsString())
                .summary(context.deserialize(x.get("messages"), Summary.class))
                .build();
    }

    @MightBeGenerated
    private static JsonElement serializeTimeRange(TimeRange src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("from", src.getFrom().toString());
        result.addProperty("to", src.getTo().toString());
        return result;
    }

    @MightBeGenerated
    private static TimeRange deserializeTimeRange(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject x = json.getAsJsonObject();
        return TimeRange.of(LocalDate.parse(x.get("from").getAsString()), LocalDate.parse(x.get("to").getAsString()));
    }

    @MightBeGenerated
    private static JsonElement serializeChangelogContent(ChangelogContent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty("title", src.getTitle());
        if (src.getDescription() != null) {
            result.addProperty("description", src.getDescription());
        } else {
            result.add("description", JsonNull.INSTANCE);
        }
        JsonArray releases = new JsonArray();
        for (ChangelogContent.VersionContent vc : src.getVersions()) {
            releases.add(context.serialize(vc, ChangelogContent.VersionContent.class));
        }
        result.add("releases", releases);
        return result;
    }

    @MightBeGenerated
    private static ChangelogContent deserializeChangelogContent(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject x = json.getAsJsonObject();
        String title = x.get("title").getAsString();
        JsonElement descEl = x.get("description");
        String description = (descEl == null || descEl.isJsonNull()) ? null : descEl.getAsString();
        List<ChangelogContent.VersionContent> versions = new ArrayList<>();
        if (x.has("releases")) {
            for (JsonElement ve : x.get("releases").getAsJsonArray()) {
                versions.add(context.deserialize(ve, ChangelogContent.VersionContent.class));
            }
        }
        return new ChangelogContent(title, description, versions);
    }

    @MightBeGenerated
    private static JsonElement serializeVersionContent(ChangelogContent.VersionContent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        Version v = src.getVersion();
        if (v.isUnreleased()) {
            result.add("version", JsonNull.INSTANCE);
        } else {
            result.addProperty("version", v.getRef());
        }
        if (v.getLink() != null) {
            result.addProperty("link", v.getLink().toString());
        } else {
            result.add("link", JsonNull.INSTANCE);
        }
        if (v.isUnreleased()) {
            result.add("date", JsonNull.INSTANCE);
        } else {
            result.addProperty("date", v.getDate().toString());
        }
        JsonArray changes = new JsonArray();
        for (ChangelogContent.TypeOfChangeContent tc : src.getGroups()) {
            String key = tc.getTypeOfChange().name().toLowerCase(Locale.ROOT);
            for (String item : tc.getItems()) {
                JsonObject changeObj = new JsonObject();
                changeObj.addProperty(key, item);
                changes.add(changeObj);
            }
        }
        result.add("changes", changes);
        result.addProperty("yanked", v.isYanked());
        return result;
    }

    @MightBeGenerated
    private static ChangelogContent.VersionContent deserializeVersionContent(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject x = json.getAsJsonObject();
        JsonElement vEl = x.get("version");
        String ref = (vEl == null || vEl.isJsonNull()) ? "Unreleased" : vEl.getAsString();
        JsonElement dEl = x.get("date");
        LocalDate date = (dEl == null || dEl.isJsonNull()) ? LocalDate.MAX : LocalDate.parse(dEl.getAsString());
        boolean yanked = x.has("yanked") && x.get("yanked").getAsBoolean();
        JsonElement lEl = x.get("link");
        URL link = null;
        if (lEl != null && !lEl.isJsonNull()) {
            try {
                link = new URI(lEl.getAsString()).toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                throw new JsonParseException("Invalid URL: " + lEl.getAsString(), ex);
            }
        }
        Version version = Version.of(ref, link, '-', date, yanked);
        List<ChangelogContent.TypeOfChangeContent> groups = new ArrayList<>();
        if (x.has("changes")) {
            Map<TypeOfChange, List<String>> groupMap = new LinkedHashMap<>();
            for (JsonElement changeEl : x.get("changes").getAsJsonArray()) {
                JsonObject changeObj = changeEl.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : changeObj.entrySet()) {
                    TypeOfChange type;
                    try {
                        type = TypeOfChange.valueOf(entry.getKey().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        throw new JsonParseException("Unknown change type: " + entry.getKey(), ex);
                    }
                    groupMap.computeIfAbsent(type, k -> new ArrayList<>()).add(entry.getValue().getAsString());
                }
            }
            for (Map.Entry<TypeOfChange, List<String>> entry : groupMap.entrySet()) {
                groups.add(new ChangelogContent.TypeOfChangeContent(entry.getKey(), entry.getValue()));
            }
        }
        return new ChangelogContent.VersionContent(version, groups);
    }
}
