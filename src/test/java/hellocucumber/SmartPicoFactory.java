/**
 * Copyright 2015-2018 Proemion GmbH
 */
package hellocucumber;

import cucumber.runtime.java.picocontainer.PicoFactory;

/**
 * Our special Pico container factory.
 * @author Umberto Nicoletti (umberto.nicoletti@gmail.com)
 * @version $Id$
 * @since 10.10
 */
public class SmartPicoFactory extends PicoFactory {
    public SmartPicoFactory() {
        if(ThreadLocalConfig.instance().registry != null) {
            System.out.println("Registry added");
            addClass(ThreadLocalConfig.instance().registry.getClass());
        }
        System.out.println("SmartPicoFactory created...");
    }
}
