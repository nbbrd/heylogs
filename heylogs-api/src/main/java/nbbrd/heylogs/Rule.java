package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;

public interface Rule {

    String name();

    Failure validate(Node node);
}
