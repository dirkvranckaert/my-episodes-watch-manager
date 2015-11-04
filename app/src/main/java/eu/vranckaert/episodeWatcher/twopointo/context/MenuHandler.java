package eu.vranckaert.episodeWatcher.twopointo.context;


import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.twopointo.context.base.BaseFragment;
import eu.vranckaert.episodeWatcher.twopointo.context.episode.EpisodesTabFragment;

/**
 * Date: 12/06/15
 * Time: 11:34
 *
 * @author Dirk Vranckaert
 */
public class MenuHandler {
    public static BaseFragment navigate(int itemId) {
        BaseFragment baseFragment = null;
        switch (itemId) {
            case R.id.drawer_item_episodes:
                baseFragment = new EpisodesTabFragment();
                break;
        }

        return baseFragment;
    }
}
