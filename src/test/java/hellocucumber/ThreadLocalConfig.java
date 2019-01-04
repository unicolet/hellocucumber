/**
 * Copyright 2015-2018 Proemion GmbH
 */
package hellocucumber;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.Closeable;

/**
 * TODO: Add Javadoc.
 * @author Umberto Nicoletti (umberto.nicoletti@gmail.com)
 * @version $Id$
 * @since 10.10
 */
public class ThreadLocalConfig implements Closeable {
    private static ThreadLocal<ThreadLocalConfig> instance = new ThreadLocal<>();

    private ThreadLocalConfig() {
    }

    public static ThreadLocalConfig instance() {
        if (instance.get()==null) {
            instance.set(new ThreadLocalConfig());
        }
        return instance.get();
    }

    public PrometheusMeterRegistry registry;

    @Override
    public void close() {
        instance.set(null);
    }
}
