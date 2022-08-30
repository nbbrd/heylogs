package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;

public interface Rule<T extends Node> {

    String name();

    String validate(T t);

    static String invalidNode(Node node, String reason) {
        return "Invalid " + node.getNodeName() + " node at line " + node.getLineNumber() + ": " + reason;
    }
}
