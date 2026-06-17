package nbbrd.heylogs.spi;

import lombok.NonNull;

public interface BranchLink extends ProjectLink {

    @NonNull
    String getBranchName();
}
