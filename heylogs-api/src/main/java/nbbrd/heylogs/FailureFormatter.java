package nbbrd.heylogs;

import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

import java.io.IOException;
import java.util.List;

@ServiceDefinition(
        quantifier = Quantifier.MULTIPLE,
        batch = true
)
public interface FailureFormatter {

    String getName();

    void format(Appendable appendable, String source, List<Failure> failures) throws IOException;
}
