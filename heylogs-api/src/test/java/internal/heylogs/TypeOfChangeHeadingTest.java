package internal.heylogs;

import com.vladsch.flexmark.ast.BulletListItem;
import nbbrd.heylogs.TypeOfChange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.heylogs.api.Sample.using;

class TypeOfChangeHeadingTest {

    @Test
    public void testGetBulletListItems() {

        Map<TypeOfChange, List<BulletListItem>> x = ChangelogHeading.root(using("/UnreleasedChanges.md"))
                .orElseThrow(RuntimeException::new)
                .getVersions()
                .filter(version -> version.getSection().isUnreleased())
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getTypeOfChanges()
                .collect(toMap(TypeOfChangeHeading::getSection, o -> o.getBulletListItems().collect(toList())));

        assertThat(x.keySet())
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        TypeOfChange.ADDED,
                        TypeOfChange.CHANGED,
                        TypeOfChange.FIXED
                );

        assertThat(x.get(TypeOfChange.ADDED))
                .hasSize(3)
                .map(o -> o.getChildChars().toString().replaceAll("\\n", "").replaceAll("\\r", ""))
                .containsExactlyInAnyOrder(
                        "Added Dutch translation",
                        "Added French translation",
                        "Added German translation"
                );
    }
}