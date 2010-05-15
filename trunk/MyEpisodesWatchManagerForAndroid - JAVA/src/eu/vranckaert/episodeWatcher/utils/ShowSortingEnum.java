package eu.vranckaert.episodeWatcher.utils;

/**
 * @author Dirk Vranckaert
 *         Date: 15-mei-2010
 *         Time: 17:28:31
 */
public enum ShowSortingEnum {
    ASCENDING("show_ascending_sort"),
    DESCENDING("show_descending_sort"),
    DEFAULT_MYEPISODES_COM("show_myepisodes_default_sort");

    private String name;

    ShowSortingEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static ShowSortingEnum getShowSorting(String name) {
        if (name.equals(ASCENDING.getName())) {
            return ASCENDING;
        } else if (name.equals(DESCENDING.getName())) {
            return DESCENDING;
        } else {
            return DEFAULT_MYEPISODES_COM;
        }
    }
}
