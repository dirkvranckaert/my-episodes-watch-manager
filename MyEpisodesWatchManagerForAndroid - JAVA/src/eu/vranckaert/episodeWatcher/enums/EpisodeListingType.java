package eu.vranckaert.episodeWatcher.enums;

/**
 * User: DIRK VRANCKAERT
 * Date: Dec 2, 2010
 * Time: 7:09:15 PM
 */
public enum EpisodeListingType {
    EPISODES_TO_WATCH(0),
    EPISODES_TO_ACQUIRE(1),
    EPISODES_COMING(2);

    EpisodeListingType(int type) {
        this.episodeListingType = type;
    }

    private int episodeListingType;

    public int getEpisodeListingType() {
        return episodeListingType;
    }

    public void setEpisodeListingType(int episodeListingType) {
        this.episodeListingType = episodeListingType;
    }
}
