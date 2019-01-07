/**
 * Copyright 2015-2018 Proemion GmbH
 */
package hellocucumber;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.Optional;

/**
 * TODO: Add Javadoc.
 * @author Umberto Nicoletti (umberto.nicoletti@gmail.com)
 * @version $Id$
 * @since 10.10
 */
public interface InjectableRegistry {
    void injectRegistry(Optional<PrometheusMeterRegistry> registry);
}
