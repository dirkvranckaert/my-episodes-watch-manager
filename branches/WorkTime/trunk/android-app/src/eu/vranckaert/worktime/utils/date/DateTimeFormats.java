package eu.vranckaert.worktime.utils.date;

import java.text.DateFormat;

/**
 * All possible DateFormat types.
 * @author Dirk Vranckaert
 */
public enum DateTimeFormats {
    /**
     * FULL is pretty completely specified, such as Tuesday, April 12, 1952 AD or 3:30:42pm PST.
     */
    FULL(DateFormat.FULL),
    /**
     * LONG is longer, such as January 12, 1952 or 3:30:32pm
     */
    LONG(DateFormat.LONG),
    /**
     * MEDIUM is longer, such as Jan 12, 1952
     */
    MEDIUM(DateFormat.MEDIUM),
    /**
     * SHORT is completely numeric, such as 12.13.52 or 3:30pm
     */
    SHORT(DateFormat.SHORT);

    int style;

    DateTimeFormats(int style) {
        this.style = style;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }
}
