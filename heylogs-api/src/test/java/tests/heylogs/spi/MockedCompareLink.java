package tests.heylogs.spi;

import internal.heylogs.git.ThreeDotDiff;
import lombok.NonNull;
import nbbrd.heylogs.spi.CompareLink;
import nbbrd.heylogs.spi.ForgeRef;
import org.jspecify.annotations.Nullable;

import java.net.URL;

import static internal.heylogs.spi.URLExtractor.urlOf;

@lombok.Value
public class MockedCompareLink implements CompareLink {

    public static MockedCompareLink parse(@NonNull URL url) {
        if (!url.getPath().contains("/compare"))
            throw new IllegalArgumentException("Not a compare link: " + url);
        return new MockedCompareLink(url);
    }

    URL url;

    @Override
    public @NonNull URL toURL() {
        return url;
    }

    @Override
    public @Nullable ForgeRef toRef(@Nullable ForgeRef baseRef) {
        return null;
    }

    @Override
    public @NonNull CompareLink derive(@NonNull String tag) {
        String urlAsString = url.toString();
        int oidIndex = urlAsString.lastIndexOf("/") + 1;
        ThreeDotDiff threeDotDiff = ThreeDotDiff.parse(urlAsString.substring(oidIndex));
        return new MockedCompareLink(urlOf(urlAsString.substring(0, oidIndex) + threeDotDiff.derive(tag)));
    }

    @Override
    public @NonNull URL getProjectURL() {
        String urlAsString = url.toString();
        int index = urlAsString.indexOf("/compare");
        return index == -1 ? url : urlOf(urlAsString.substring(0, index));
    }

    @Override
    public @NonNull String getCompareBaseRef() {
        return ThreeDotDiff.parse(url.toString().substring(url.toString().lastIndexOf("/") + 1)).getFrom();
    }

    @Override
    public @NonNull String getCompareHeadRef() {
        return ThreeDotDiff.parse(url.toString().substring(url.toString().lastIndexOf("/") + 1)).getTo();
    }
}
