package nbbrd.heylogs.spi;

import lombok.NonNull;

import java.util.List;

public interface BlobLink extends BranchLink {

    @NonNull
    List<String> getFilePath();
}