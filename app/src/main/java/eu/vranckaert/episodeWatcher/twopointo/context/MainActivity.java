package eu.vranckaert.episodeWatcher.twopointo.context;

import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.twopointo.context.base.BaseMenuActivity;

/**
 * Date: 04/11/15
 * Time: 07:41
 *
 * @author Dirk Vranckaert
 */
public class MainActivity extends BaseMenuActivity {
    @Override
    protected int getDefaultMenuItem() {
        return R.id.drawer_item_episodes;
    }


}
