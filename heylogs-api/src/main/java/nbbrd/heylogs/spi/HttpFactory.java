package nbbrd.heylogs.spi;

import internal.heylogs.spi.NoOpHttpFactory;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.http.HttpClient;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

@ServiceDefinition(
        quantifier = Quantifier.SINGLE,
        fallback = NoOpHttpFactory.class
)
public interface HttpFactory {

    @NonNull
    HttpClient getClient();

    @StaticFactoryMethod
    static @NonNull HttpFactory noOp() {
        return NoOpHttpFactory.NO_OP;
    }
}
