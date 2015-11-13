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
import eu.vranckaert.episodeWatcher.twopointo.view.episode.EpisodesListAdapter.EpisodesListListener;
import eu.vranckaert.episodeWatcher.twopointo.view.episode.EpisodesTabsView;
import eu.vranckaert.episodeWatcher.twopointo.view.episode.EpisodesTabsView.EpisodesTabListener;

import java.util.List;

/**
 * Date: 04/11/15
 * Time: 07:51
 *
 * @author Dirk Vranckaert
 */
public class EpisodesTabFragment extends BaseFragment implements EpisodesListListener, EpisodesTabListener {
    private EpisodesTabsView mView;
    private LoadEpisodesTask mLoadingEpisodesToWatchTask;
    private LoadEpisodesTask mLoadingEpisodesToAcquireTask;
    private MarkAllEpisodesTask mMarkEpisodesTask;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);

        setTitle(R.string.watchListTitle);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new EpisodesTabsView(inflater, container, this, this);
        loadAllEpisodes();
        return mView.getView();
    }

    private void loadAllEpisodes() {
        loadAllEpisodesToWatch();
        loadAllEpisodesToAcquire();
    }

    private void loadAllEpisodesToWatch() {
        if (mLoadingEpisodesToWatchTask != null) {
            mLoadingEpisodesToWatchTask.cancel();
        }

        mLoadingEpisodesToWatchTask = new LoadEpisodesTask(this, EpisodeType.EPISODES_TO_WATCH);
        mLoadingEpisodesToWatchTask.execute();
    }

    private void loadAllEpisodesToAcquire() {
        if (mLoadingEpisodesToAcquireTask != null) {
            mLoadingEpisodesToAcquireTask.cancel();
        }

        mLoadingEpisodesToAcquireTask = new LoadEpisodesTask(this, EpisodeType.EPISODES_TO_ACQUIRE);
        mLoadingEpisodesToAcquireTask.execute();
    }

    @Override
    public void startRefreshingEpisodesToWatch() {
        loadAllEpisodesToWatch();
    }

    @Override
    public void startRefreshingEpisodesToAcquire() {
        loadAllEpisodesToAcquire();
    }

    @Override
    public void onDestroyView() {
        if (mLoadingEpisodesToWatchTask != null) {
            mLoadingEpisodesToWatchTask.cancel();
            mLoadingEpisodesToWatchTask = null;
        }
        if (mLoadingEpisodesToAcquireTask != null) {
            mLoadingEpisodesToAcquireTask.cancel();
            mLoadingEpisodesToAcquireTask = null;
        }

        if (mMarkEpisodesTask != null) {
            mMarkEpisodesTask.cancel();
            mMarkEpisodesTask = null;
        }

        super.onDestroyView();
    }

    @Override
    public void markWatched(List<Episode> episodes) {
        if (mMarkEpisodesTask != null) {
            mMarkEpisodesTask.cancel();
        }
        mMarkEpisodesTask = new MarkAllEpisodesTask(this, episodes, MarkAllEpisodesTask.ACTION_MARK_WATHCED);
        mMarkEpisodesTask.execute();
    }

    @Override
    public void markAcquired(List<Episode> episodes) {
        if (mMarkEpisodesTask != null) {
            mMarkEpisodesTask.cancel();
        }
        mMarkEpisodesTask = new MarkAllEpisodesTask(this, episodes, MarkAllEpisodesTask.ACTION_MARK_ACQUIRED);
        mMarkEpisodesTask.execute();
    }

    @Override
    public void episodeCountHasUpdated() {
        mView.episodeCountHasUpdated();
    }

    private void episodesMarkedAcquired(List<Episode> episodes) {
        mView.onEpisodesMarkedAcquired(episodes);
    }

    private void episodesMarkedWatched(List<Episode> episodes) {
        mView.onEpisodesMarkedWatched(episodes);
    }

    private void episodesNotMarkedAcquired(List<Episode> episodes) {
        mView.onEpisodesNotMarkedAcquired(episodes);
    }

    private void episodesNotMarkedWatched(List<Episode> episodes) {
        mView.onEpisodesNotMarkedWatched(episodes);
    }

    public final class LoadEpisodesTask extends CustomTask<List<Episode>> {
        private final EpisodesTabFragment mFragment;
        private final EpisodeType mType;

        public LoadEpisodesTask(EpisodesTabFragment fragment, EpisodeType type) {
            super(fragment.getContext());
            mFragment = fragment;
            mType = type;
        }

        @Override
        public void preExecute() {
            if (EpisodeType.EPISODES_TO_WATCH.equals(mType)) {
                mFragment.mView.setLoadingEpisodesToWatch(true);
            } else if (EpisodeType.EPISODES_TO_ACQUIRE.equals(mType)) {
                mFragment.mView.setLoadingEpisodesToAcquire(true);
            }
        }

        @Override
        public void onError(Exception exception) {
            if (EpisodeType.EPISODES_TO_WATCH.equals(mType)) {
                mFragment.mView.setLoadingEpisodesToWatch(false);
            } else if (EpisodeType.EPISODES_TO_ACQUIRE.equals(mType)) {
                mFragment.mView.setLoadingEpisodesToAcquire(false);
            }
        }

        @Override
        public List<Episode> doInBackground() throws Exception {
            User user = new User(
                    Preferences.getPreference(mFragment.getActivity(), User.USERNAME),
                    Preferences.getPreference(mFragment.getActivity(), User.PASSWORD)
            );
            EpisodesService episodesService = new EpisodesService();
            List<Episode> episodes = episodesService.retrieveUnlimitedNumberOfEpisodes(mType, user);
            return episodes;
        }

        @Override
        public void onTaskCompleted(List<Episode> result) {
            if (EpisodeType.EPISODES_TO_WATCH.equals(mType)) {
                mFragment.mView.setEpisodesToWatch(result);
            } else if (EpisodeType.EPISODES_TO_ACQUIRE.equals(mType)) {
                mFragment.mView.setEpisodesToAcquire(result);
            }
        }
    }

    public final class MarkAllEpisodesTask extends CustomTask<List<Episode>> {
        public static final int ACTION_MARK_WATHCED = 0;
        public static final int ACTION_MARK_ACQUIRED = 1;

        private final EpisodesTabFragment mFragment;
        private final List<Episode> mEpisodes;
        private final int mAction;

        public MarkAllEpisodesTask(EpisodesTabFragment fragment, List<Episode> episodes, int action) {
            super(fragment.getActivity());
            mFragment = fragment;

            mEpisodes = episodes;
            mAction = action;
        }

        @Override
        protected boolean isProgressTask() {
            return true;
        }

        @Override
        public void onError(Exception exception) {
            if (mAction == ACTION_MARK_WATHCED) {
                mFragment.episodesNotMarkedWatched(mEpisodes);
            } else if (mAction == ACTION_MARK_ACQUIRED) {
                mFragment.episodesNotMarkedAcquired(mEpisodes);
            }
        }

        @Override
        public List<Episode> doInBackground() throws Exception {
            User user = new User(
                    Preferences.getPreference(mFragment.getActivity(), User.USERNAME),
                    Preferences.getPreference(mFragment.getActivity(), User.PASSWORD)
            );

            EpisodesService episodesService = new EpisodesService();
            if (mAction == ACTION_MARK_WATHCED) {
                episodesService.watchedEpisodes(mEpisodes, user);
            } else if (mAction == ACTION_MARK_ACQUIRED) {
                episodesService.acquireEpisodes(mEpisodes, user);
            }
            return mEpisodes;
        }

        @Override
        public void onTaskCompleted(List<Episode> result) {
            if (mAction == ACTION_MARK_WATHCED) {
                mFragment.episodesMarkedWatched(result);
            } else if (mAction == ACTION_MARK_ACQUIRED) {
                mFragment.episodesMarkedAcquired(result);
            }
        }
    }
}
