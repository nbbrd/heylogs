package nbbrd.heylogs.ext.json;

import com.google.gson.*;
import internal.heylogs.spi.FormatSupport;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.design.MightBeGenerated;
import nbbrd.heylogs.*;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatType;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@DirectImpl
@ServiceProvider
public final class JsonFormat implements Format {

    public static final String ID = "json";

    @Override
    public @NonNull String getFormatId() {
        return ID;
    }

    @Override
    public @NonNull String getFormatName() {
        return "JSON-serialized output";
    }

    @Override
    public @NonNull String getFormatModuleId() {
        return "json";
    }

    @Override
    public @NonNull Set<FormatType> getSupportedFormatTypes() {
        return EnumSet.allOf(FormatType.class);
    }

    @Override
    public void formatProblems(@NonNull Appendable appendable, @NonNull List<Check> list) throws IOException {
        try {
            GSON.toJson(list, appendable);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void formatStatus(@NonNull Appendable appendable, @NonNull List<Scan> list) throws IOException {
        try {
            GSON.toJson(list, appendable);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> list) throws IOException {
        try {
            GSON.toJson(list, appendable);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public @NonNull DirectoryStream.Filter<? super Path> getFormatFileFilter() {
        return FormatSupport.getFormatFileFilterByExtension(".json");
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
}
