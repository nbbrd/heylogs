package internal.heylogs;

import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Heading;
import nbbrd.heylogs.TypeOfChange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static tests.heylogs.api.Sample.asHeading;
import static tests.heylogs.api.Sample.using;
import static internal.heylogs.ChangelogNodes.*;
import static org.assertj.core.api.Assertions.assertThat;

class ChangelogNodesTest {

    @Test
    public void testIsUnreleasedHeading() {
        assertThat(isUnreleasedHeading(asHeading("## [Unreleased]"))).isTrue();
        assertThat(isUnreleasedHeading(asHeading("## [unreleased]"))).isTrue();
        assertThat(isUnreleasedHeading(asHeading("# [unreleased]"))).isFalse();
        assertThat(isUnreleasedHeading(asHeading("## unreleased"))).isFalse();
        assertThat(isUnreleasedHeading(asHeading("## [stuff]"))).isFalse();
    }

    @Test
    public void testGetUnreleasedHeading() {
        assertThat(getUnreleasedHeading(using("/Empty.md")))
                .isEmpty();

        assertThat(getUnreleasedHeading(using("/Main.md")))
                .isNotEmpty()
                .hasValueSatisfying(ChangelogNodes::isUnreleasedHeading);
    }

    @Test
    public void testGetBulletListsByTypeOfChange() {
        Heading unreleased = getUnreleasedHeading(using("/UnreleasedChanges.md")).orElseThrow(RuntimeException::new);

        Map<TypeOfChange, List<BulletListItem>> x = getBulletListsByTypeOfChange(unreleased);

        assertThat(x.keySet())
                .hasSize(3)
                .containsExactly(
                        TypeOfChange.ADDED,
                        TypeOfChange.CHANGED,
                        TypeOfChange.FIXED
                );

        assertThat(x.get(TypeOfChange.ADDED))
                .hasSize(3)
                .map(o -> o.getChildChars().toString().replaceAll("\\n", "").replaceAll("\\r", ""))
                .containsExactly(
                        "Added Dutch translation",
                        "Added French translation",
                        "Added German translation"
                );
    }
}