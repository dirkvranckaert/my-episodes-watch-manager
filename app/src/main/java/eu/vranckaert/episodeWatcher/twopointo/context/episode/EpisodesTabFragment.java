package eu.vranckaert.episodeWatcher.twopointo.context.episode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.android.threading.CustomTask;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.service.EpisodesService;
import eu.vranckaert.episodeWatcher.twopointo.view.episode.EpisodesTabsView;

import java.util.List;

/**
 * Date: 04/11/15
 * Time: 07:51
 *
 * @author Dirk Vranckaert
 */
public class EpisodesTabFragment extends BaseFragment {
    private EpisodesTabsView mView;
    private LoadAllEpisodesTask mLoadingTask;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);

        setTitle(R.string.watchListTitle);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new EpisodesTabsView(inflater, container);
        loadAllEpisodes();
        return mView.getView();
    }

    private void loadAllEpisodes() {
        if (mLoadingTask != null) {
            mLoadingTask.cancel();
        }

        mLoadingTask = new LoadAllEpisodesTask(this);
        mLoadingTask.execute();
    }

    @Override
    public void onDestroyView() {
        if (mLoadingTask != null) {
            mLoadingTask.cancel();
            mLoadingTask = null;
        }

        super.onDestroyView();
    }

    public final class LoadAllEpisodesTask extends CustomTask<Void> {
        private final EpisodesTabFragment mFragment;

        private List<Episode> mEpisodesToWatch;
        private List<Episode> mEpisodesToAcquire;

        public LoadAllEpisodesTask(EpisodesTabFragment fragment) {
            super(fragment.getContext());
            mFragment = fragment;
        }

        @Override
        public void preExecute() {
            mFragment.mView.startLoadingAll();
        }

        @Override
        public Void doInBackground() throws Exception {
            User user = new User(
                    Preferences.getPreference(mFragment.getActivity(), User.USERNAME),
                    Preferences.getPreference(mFragment.getActivity(), User.PASSWORD)
            );
            EpisodesService episodesService = new EpisodesService();
            mEpisodesToWatch = episodesService.retrieveUnlimitedNumberOfEpisodes(EpisodeType.EPISODES_TO_WATCH, user);
            mEpisodesToAcquire = episodesService.retrieveUnlimitedNumberOfEpisodes(EpisodeType.EPISODES_TO_ACQUIRE, user);
            return null;
        }

        @Override
        public void onTaskCompleted(Void result) {
            mFragment.mView.setEpisodesToWatch(mEpisodesToWatch);
            mFragment.mView.setEpisodesToAcquire(mEpisodesToAcquire);
        }
    }
}
