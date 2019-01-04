/**
 * Copyright 2015-2018 Proemion GmbH
 */
package hellocucumber;

/**
 * TODO: Add Javadoc.
 * @author Umberto Nicoletti (umberto.nicoletti@gmail.com)
 * @version $Id$
 * @since 10.10
 */
public final class DayOfWeek {
    public String isItFriday(final String today) {
        if (today.equals("Friday")) {
            return "TGIF";
        }
        return "Nope";
    }
}
