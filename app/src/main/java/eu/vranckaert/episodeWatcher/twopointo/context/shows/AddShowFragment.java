package eu.vranckaert.episodeWatcher.twopointo.context.shows;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.android.threading.CustomTask;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.service.ShowService;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.AddShowView;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.AddShowView.AddShowsListener;

import java.util.List;

/**
 * Date: 13/11/15
 * Time: 12:15
 *
 * @author Dirk Vranckaert
 */
public class AddShowFragment extends BaseFragment implements AddShowsListener {
    private AddShowView mView;
    private SearchShowsTask mSearchTask;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.addShow);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new AddShowView(inflater, container, this);
        return mView.getView();
    }

    @Override
    public void onDestroyView() {
        if (mSearchTask != null) {
            mSearchTask.cancel();
            mSearchTask = null;
        }

        super.onDestroyView();
    }

    @Override
    public void searchShows(CharSequence needle) {
        if (mSearchTask != null) {
            mSearchTask.cancel();
        }
        mSearchTask = new SearchShowsTask(this, needle);
        mSearchTask.execute();
    }

    @Override
    public void cancelPreviousSearch() {
        if (mSearchTask != null) {
            mSearchTask.cancel();
        }
    }

    private void onShowsFound(List<Show> result) {
        mView.setShows(result);
    }

    public static class SearchShowsTask extends CustomTask<List<Show>> {
        private final AddShowFragment mFragment;
        private final CharSequence mNeedle;

        public SearchShowsTask(AddShowFragment fragment, CharSequence needle) {
            super(fragment.getContext());
            mFragment = fragment;
            mNeedle = needle;
        }

        @Override
        public List<Show> doInBackground() throws Exception {
            User user = new User(
                    Preferences.getPreference(getContext(), User.USERNAME),
                    Preferences.getPreference(getContext(), User.PASSWORD)
            );
            return new ShowService().searchShows(mNeedle.toString(), user);
        }

        @Override
        public void onTaskCompleted(List<Show> result) {
            mFragment.onShowsFound(result);
        }
    }
}
