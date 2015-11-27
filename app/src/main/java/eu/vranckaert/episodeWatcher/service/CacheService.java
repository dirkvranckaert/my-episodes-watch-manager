package eu.vranckaert.episodeWatcher.service;

import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;

import java.util.Date;
import java.util.List;

/**
 * Date: 27/11/15
 * Time: 07:41
 *
 * @author Dirk Vranckaert
 */
public class CacheService {
    private static final long EPISODES_VALID_TIME = 1000 * 60 * 15; // Valid for 15 minutes

    private static Long mCacheTimeEpisodesToWatch;
    private static Long mCacheTimeEpisodesToAcquire;
    private static List<Episode> mEpisodesToWatch;
    private static List<Episode> mEpisodesToAcquire;

    public static void storeEpisodes(List<Episode> episodes, EpisodeType type) {
        if (type.equals(EpisodeType.EPISODES_TO_ACQUIRE)) {
            mEpisodesToAcquire = episodes;
            mCacheTimeEpisodesToAcquire = new Date().getTime();
        } else if (type.equals(EpisodeType.EPISODES_TO_WATCH)) {
            mEpisodesToWatch = episodes;
            mCacheTimeEpisodesToWatch = new Date().getTime();
        }
    }

    public static List<Episode> getEpisodes(EpisodeType type) {
        if (type.equals(EpisodeType.EPISODES_TO_ACQUIRE)) {
            if (mEpisodesToAcquire != null && isValid(mCacheTimeEpisodesToAcquire, EPISODES_VALID_TIME)) {
                return mEpisodesToAcquire;
            } else {
                mEpisodesToAcquire = null;
            }
        } else if (type.equals(EpisodeType.EPISODES_TO_WATCH)) {
            if (mEpisodesToWatch != null && isValid(mCacheTimeEpisodesToWatch, EPISODES_VALID_TIME)) {
                return mEpisodesToWatch;
            } else {
                mEpisodesToWatch = null;
            }
        }

        return null;
    }

    private static boolean isValid(long time, long maxValidLength) {
        long now = new Date().getTime();
        return !(now > time + maxValidLength);
    }
}
