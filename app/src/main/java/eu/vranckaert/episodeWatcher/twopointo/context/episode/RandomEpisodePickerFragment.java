package eu.vranckaert.episodeWatcher.twopointo.context.episode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.service.EpisodesService;
import eu.vranckaert.episodeWatcher.twopointo.threading.MyEpisodesTask;
import eu.vranckaert.episodeWatcher.twopointo.view.episode.RandomEpisodePickerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Date: 27/11/15
 * Time: 08:03
 *
 * @author Dirk Vranckaert
 */
public class RandomEpisodePickerFragment extends BaseFragment {
    private RandomEpisodePickerView mView;
    private RandomEpisodePickerTask mEpisodePickerTask;

    private boolean mLoading;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.randompickershort);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new RandomEpisodePickerView(inflater, container);
        pickEpisode();
        return mView.getView();
    }

    private void pickEpisode() {
        mLoading = true;
        invalidateOptionsMenu();

        mView.startLoading();
        if (mEpisodePickerTask != null) {
            mEpisodePickerTask.cancel();
        }
        mEpisodePickerTask = new RandomEpisodePickerTask(this);
        mEpisodePickerTask.execute();
    }

    private void setPickedEpisode(Episode episode) {
        mLoading = false;
        invalidateOptionsMenu();

        mView.setEpisode(episode);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (!mLoading) {
            new MenuInflater(getContext()).inflate(R.menu.random_episode_picker, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            pickEpisode();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        mView.stopLoading();

        if (mEpisodePickerTask != null) {
            mEpisodePickerTask.cancel();
            mEpisodePickerTask = null;
        }

        super.onDestroyView();
    }

    public static class RandomEpisodePickerTask extends MyEpisodesTask<Episode> {
        private final RandomEpisodePickerFragment mFragment;

        public RandomEpisodePickerTask(RandomEpisodePickerFragment fragment) {
            super(fragment.getContext());
            mFragment = fragment;
        }

        @Override
        public Episode doInBackground() throws Exception {
            User user = User.get(mFragment.getContext());
            List<Episode> episodes = new EpisodesService().retrieveUnlimitedNumberOfEpisodes(EpisodeType.EPISODES_TO_WATCH, user);
            Map<String, Episode> filteredEpisodes = new HashMap<>();
            int episodeCount = episodes.size();
            for (int i = 0; i < episodeCount; i++) {
                Episode episode = episodes.get(i);
                String id = episode.getMyEpisodeID();
                if (filteredEpisodes.containsKey(id)) {
                    Episode filteredEpisode = filteredEpisodes.get(id);
                    if (episode.getSeason() < filteredEpisode.getSeason() || (episode.getSeason() == filteredEpisode.getSeason() && episode.getEpisode() < filteredEpisode.getEpisode())) {
                        filteredEpisodes.put(id, episode);
                    }
                } else {
                    filteredEpisodes.put(id, episode);
                }
            }

            Random r = new Random();
            int randint = r.nextInt(filteredEpisodes.size());
            return filteredEpisodes.values().toArray(new Episode[filteredEpisodes.size()])[randint];
        }

        @Override
        public void onTaskCompleted(Episode episode) {
            mFragment.setPickedEpisode(episode);
        }
    }
}
