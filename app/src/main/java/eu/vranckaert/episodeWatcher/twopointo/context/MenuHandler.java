package eu.vranckaert.episodeWatcher.twopointo.context;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;
import eu.vranckaert.android.context.AbstractMenuHandler;
import eu.vranckaert.android.context.BaseAppcompatFragment;
import eu.vranckaert.episodeWatcher.MyEpisodes;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.service.CacheService;
import eu.vranckaert.episodeWatcher.twopointo.context.episode.EpisodesTabFragment;
import eu.vranckaert.episodeWatcher.twopointo.context.episode.RandomEpisodePickerFragment;
import eu.vranckaert.episodeWatcher.twopointo.context.settings.SettingsFragment;
import eu.vranckaert.episodeWatcher.twopointo.context.shows.ManageShowsFragment;

/**
 * Date: 12/06/15
 * Time: 11:34
 *
 * @author Dirk Vranckaert
 */
public class MenuHandler extends AbstractMenuHandler {
    @Override
    public BaseAppcompatFragment navigate(final Activity activity, int itemId) {
        BaseAppcompatFragment baseFragment = null;
        switch (itemId) {
            case R.id.drawer_item_episodes:
                baseFragment = new EpisodesTabFragment();
                break;
            case R.id.drawer_item_manage_shows:
                baseFragment = new ManageShowsFragment();
                break;
            case R.id.drawer_item_random_episode_picker:
                baseFragment = new RandomEpisodePickerFragment();
                break;
            case R.id.drawer_item_logout:
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.logoutDialogTitle)
                        .setMessage(R.string.logoutDialogMessage)
                        .setPositiveButton(R.string.logout, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean caching = Preferences
                                        .getPreferenceBoolean(MyEpisodes.getContext(), PreferencesKeys.CACHE_EPISODES,
                                                true);
                                if (!caching) {
                                    CacheService.clearEpisodeCache();
                                }
                                Preferences.setPreference(MyEpisodes.getContext(), User.USERNAME, null);
                                Preferences.setPreference(MyEpisodes.getContext(), User.PASSWORD, null);
                                NavigationManager.restartApplication(activity, false);
                            }
                        })
                        .setNegativeButton(R.string.close, null)
                        .show();
                break;
            case R.id.drawer_item_settings:
                baseFragment = new SettingsFragment();
        }

        return baseFragment;
    }
}
