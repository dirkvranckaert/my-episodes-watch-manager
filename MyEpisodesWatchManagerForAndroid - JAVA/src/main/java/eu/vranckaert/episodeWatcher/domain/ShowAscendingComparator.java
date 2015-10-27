package eu.vranckaert.episodeWatcher.domain;

import java.util.Comparator;

/**
 * @author Dirk Vranckaert
 *         Date: 15-mei-2010
 *         Time: 17:37:13
 */
public class ShowAscendingComparator implements Comparator<Show> {
    @Override
    public int compare(Show o1, Show o2) {
        return o1.getShowName().compareTo(o2.getShowName());
    }
}
