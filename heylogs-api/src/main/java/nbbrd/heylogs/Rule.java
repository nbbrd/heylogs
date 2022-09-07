package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;

public interface Rule {

    String getName();

    Failure validate(Node node);
}
