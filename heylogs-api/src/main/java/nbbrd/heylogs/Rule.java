package nbbrd.heylogs;

import com.vladsch.flexmark.util.ast.Node;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batch = true
)
public interface Rule {

    String getName();

    Failure validate(Node node);
}
