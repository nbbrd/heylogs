package internal.heylogs;

import com.google.gson.*;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import nbbrd.heylogs.Problem;
import nbbrd.heylogs.Resource;
import nbbrd.heylogs.Status;
import nbbrd.heylogs.TimeRange;
import nbbrd.heylogs.spi.Format;
import nbbrd.heylogs.spi.FormatType;
import nbbrd.heylogs.spi.RuleIssue;
import nbbrd.heylogs.spi.RuleSeverity;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@ServiceProvider
public final class JsonFormat implements Format {

    public static final String ID = "json";

    @Override
    public @NonNull String getFormatId() {
        return ID;
    }

    @Override
    public @NonNull String getFormatName() {
        return "Outputs JSON-serialized results.";
    }

    @Override
    public @NonNull Set<FormatType> getSupportedFormatTypes() {
        return EnumSet.allOf(FormatType.class);
    }

    @Override
    public void formatProblems(@NonNull Appendable appendable, @NonNull String source, @NonNull List<Problem> problems) throws IOException {
        try {
            GSON.toJson(new FileProblems(source, problems), appendable);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void formatStatus(@NonNull Appendable appendable, @NonNull String source, @NonNull Status status) throws IOException {
        try {
            GSON.toJson(new FileStatus(source, status), appendable);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void formatResources(@NonNull Appendable appendable, @NonNull List<Resource> resources) throws IOException {
        try {
            GSON.toJson(resources, appendable);
        } catch (JsonIOException ex) {
            throw new IOException(ex);
        }
    }

    @lombok.Value
    private static class FileProblems {
        String filePath;
        List<Problem> messages;
    }

    @lombok.Value
    private static class FileStatus {
        String filePath;
        Status status;
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Problem.class, (JsonSerializer<Problem>) JsonFormat::serializeProblem)
            .registerTypeAdapter(Problem.class, (JsonDeserializer<Problem>) JsonFormat::deserializeProblem)
            .registerTypeAdapter(TimeRange.class, (JsonSerializer<TimeRange>) JsonFormat::serializeTimeRange)
            .registerTypeAdapter(TimeRange.class, (JsonDeserializer<TimeRange>) JsonFormat::deserializeTimeRange)
            .setPrettyPrinting()
            .create();

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
