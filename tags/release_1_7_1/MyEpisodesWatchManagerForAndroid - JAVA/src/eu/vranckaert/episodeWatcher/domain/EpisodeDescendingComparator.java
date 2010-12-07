package eu.vranckaert.episodeWatcher.domain;

import java.util.Comparator;

/**
 * @author Dirk Vranckaert
 *         Date: 11-mei-2010
 *         Time: 18:51:31
 */
public class EpisodeDescendingComparator implements Comparator<Episode> {
    @Override
    public int compare(Episode o1, Episode o2) {
        int s1 = o1.getSeason();
        int e1 = o1.getEpisode();
        int s2 = o2.getSeason();
        int e2 = o2.getEpisode();

        if (s1 > s2) {
            return -1;
        } else if (s1 < s2) {
            return 1;
        } else {
            if (e1 > e2) {
                return -1;
            } else if (e1 < e2) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
