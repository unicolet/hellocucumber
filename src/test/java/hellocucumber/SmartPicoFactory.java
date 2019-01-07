/**
 * Copyright 2015-2018 Proemion GmbH
 */
package hellocucumber;

import cucumber.runtime.java.picocontainer.PicoFactory;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.Optional;

/**
 * Our special Pico container factory.
 * @author Umberto Nicoletti (umberto.nicoletti@gmail.com)
 * @version $Id$
 * @since 10.10
 */
public class SmartPicoFactory extends PicoFactory {
    private final Optional<PrometheusMeterRegistry> registry;

    public SmartPicoFactory() {
        this.registry = Optional.ofNullable(
            ThreadLocalConfig.instance().registry
        );
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        final T result = super.getInstance(type);
        if (result instanceof InjectableRegistry) {
            ((InjectableRegistry)result).injectRegistry(this.registry);
        }
        return result;
    }
}
