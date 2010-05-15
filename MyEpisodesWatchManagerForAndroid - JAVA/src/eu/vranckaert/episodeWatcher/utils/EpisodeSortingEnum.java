package eu.vranckaert.episodeWatcher.utils;

/**
 * @author Dirk Vranckaert
 *         Date: 11-mei-2010
 *         Time: 18:30:40
 */
public enum EpisodeSortingEnum {
    OLDEST("oldest_on_top"),
    NEWEST("newest_on_top");

    private String name;

    EpisodeSortingEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static EpisodeSortingEnum getEpisodeSorting(String name) {
        if (name.equals(NEWEST.getName())) {
            return NEWEST;
        } else {
            return OLDEST;
        }
    }
}
