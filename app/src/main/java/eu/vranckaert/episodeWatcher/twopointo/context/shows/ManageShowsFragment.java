package eu.vranckaert.episodeWatcher.twopointo.context.shows;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.android.threading.CustomTask;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.ShowType;
import eu.vranckaert.episodeWatcher.service.ShowService;
import eu.vranckaert.episodeWatcher.twopointo.context.NavigationManager;
import eu.vranckaert.episodeWatcher.twopointo.threading.MyEpisodesTask;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.ManageShowsView;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.ManageShowsView.ManageShowsListener;

import java.util.List;

/**
 * Date: 13/11/15
 * Time: 12:09
 *
 * @author Dirk Vranckaert
 */
public class ManageShowsFragment extends BaseFragment implements ManageShowsListener {
    private static final int REQUEST_CODE_ADD_SHOW = 0;

    private boolean mShowsInitialized = false;
    private ManageShowsView mView;
    private ListShowsTask mListShowsTask;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.manageShows);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new ManageShowsView(inflater, container, this);
        loadShows();
        return mView.getView();
    }

    private void loadShows() {
        if (mListShowsTask != null) {
            mListShowsTask.cancel();
        }
        mListShowsTask = new ListShowsTask(this);
        mListShowsTask.attachSwipeRefreshLayout(mView.getRefreshView());
        mListShowsTask.execute();
    }

    private void onShowsLoaded(List<Show> shows) {
        mShowsInitialized = true;
        mView.setShows(shows);
    }

    @Override
    public void onDestroyView() {
        if (mListShowsTask != null) {
            mListShowsTask.cancel();
            mListShowsTask = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manage_shows, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            NavigationManager.startAddShow(this, REQUEST_CODE_ADD_SHOW);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        if (mShowsInitialized) {
            if (resultCode == REQUEST_CODE_ADD_SHOW && resultCode == RESULT_OK && data != null) {
                Show show = (Show) data.getSerializable(AddShowFragment.EXTRA_SHOW);
                mView.addShow(show);
            }
        } else {
            loadShows();
        }
    }

    @Override
    public void refresh() {
        loadShows();
    }

    public static class ListShowsTask extends MyEpisodesTask<List<Show>> {
        private final ManageShowsFragment mFragment;

        public ListShowsTask(ManageShowsFragment fragment) {
            super(fragment.getContext());
            mFragment = fragment;
        }

        @Override
        public List<Show> doInBackground() throws Exception {
            ShowService showService = new ShowService();
            return showService.getFavoriteOrIgnoredShows(User.get(mFragment.getContext()), ShowType.FAVOURITE_SHOWS);
        }

        @Override
        public void onTaskCompleted(List<Show> result) {
            mFragment.onShowsLoaded(result);
        }
    }
}
