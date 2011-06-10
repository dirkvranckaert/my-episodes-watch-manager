package eu.vranckaert.episodeWatcher.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: Dec 2, 2010
 * Time: 7:09:15 PM
 */
public enum EpisodeType {
    EPISODES_TO_WATCH(0),
    EPISODES_TO_ACQUIRE(1),
    EPISODES_COMING(2),
    EPISODES_TO_YESTERDAY(3);

    EpisodeType(int type) {
        this.episodeListingType = type;
    }

    private int episodeListingType;

    public int getEpisodeListingType() {
        return episodeListingType;
    }

    public static List<Integer> getEpisodeListingTypeList() {
        List<Integer> episodeTypes = new ArrayList<Integer>();
        episodeTypes.add(EPISODES_TO_WATCH.getEpisodeListingType());
        episodeTypes.add(EPISODES_TO_ACQUIRE.getEpisodeListingType());
        episodeTypes.add(EPISODES_COMING.getEpisodeListingType());
        return episodeTypes;
    }

    public static CharSequence[] getEpisodeListingTypeArray() {
        CharSequence[] types = {
                       String.valueOf(EPISODES_TO_WATCH.getEpisodeListingType()),
                       String.valueOf(EPISODES_TO_ACQUIRE.getEpisodeListingType()),
                       String.valueOf(EPISODES_COMING.getEpisodeListingType())
                      };
        return types;
    }
}
