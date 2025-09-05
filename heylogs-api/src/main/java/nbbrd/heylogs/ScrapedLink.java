package nbbrd.heylogs;

import lombok.NonNull;

import java.util.List;

@lombok.Value
public class ScrapedLink {

    @NonNull
    String link;

    int line;

    @NonNull
    List<String> types;
}
