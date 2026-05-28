package nbbrd.heylogs.ext.rules;

import com.vladsch.flexmark.ast.BulletListItem;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Document;
import internal.heylogs.FlexmarkIO;
import nbbrd.design.MightBePromoted;

import java.util.stream.StreamSupport;

import static nbbrd.io.function.IOFunction.unchecked;

final class RulesTestHelper {

    private RulesTestHelper() {
    }

    static BulletListItem asBulletListItem(String text) {
        return unchecked(FlexmarkIO.newTextParser()::parseChars)
                .andThen(doc -> (BulletListItem) StreamSupport.stream(doc.getDescendants().spliterator(), false).filter(item -> item instanceof BulletListItem).findFirst().orElseThrow(IllegalArgumentException::new))
                .apply(text);
    }

    static Link asLink(String text) {
        return unchecked(FlexmarkIO.newTextParser()::parseChars)
                .andThen(doc -> (Link) StreamSupport.stream(doc.getDescendants().spliterator(), false).filter(item -> item instanceof Link).findFirst().orElseThrow(IllegalArgumentException::new))
                .apply(text);
    }

    @MightBePromoted
    static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}

