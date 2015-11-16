package eu.vranckaert.episodeWatcher.twopointo.context.shows;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.android.threading.CustomTask;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.service.ShowService;
import eu.vranckaert.episodeWatcher.twopointo.threading.MyEpisodesTask;
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
    protected static final String EXTRA_SHOW = "show";

    private AddShowView mView;
    private SearchShowsTask mSearchTask;
    private AddShowTask mAddShowTask;

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

    @Override
    public void addShow(final Show show) {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.showSearchAddShow)
                .setPositiveButton(R.string.addShow, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mAddShowTask != null) {
                            mAddShowTask.cancel();
                        }
                        mAddShowTask = new AddShowTask(AddShowFragment.this, show);
                        mAddShowTask.execute();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void onShowAdded(Show show) {
        Bundle data = new Bundle();
        data.putSerializable(EXTRA_SHOW, show);
        setResult(RESULT_OK, data);
    }

    public static class SearchShowsTask extends MyEpisodesTask<List<Show>> {
        private final AddShowFragment mFragment;
        private final CharSequence mNeedle;

        public SearchShowsTask(AddShowFragment fragment, CharSequence needle) {
            super(fragment.getContext());
            mFragment = fragment;
            mNeedle = needle;
        }

        @Override
        public List<Show> doInBackground() throws Exception {
            User user = User.get(mFragment.getContext());
            return new ShowService().newSearchShows(mNeedle.toString(), user);
        }

        @Override
        public void onTaskCompleted(List<Show> result) {
            mFragment.onShowsFound(result);
        }
    }

    public static class AddShowTask extends MyEpisodesTask<Void> {
        private final AddShowFragment mFragment;
        private final Show mShow;

        public AddShowTask(AddShowFragment fragment, Show show) {
            super(fragment.getActivity());
            mFragment = fragment;
            mShow = show;
        }

        @Override
        public Void doInBackground() throws Exception {
            User user = User.get(mFragment.getContext());
            ShowService showService = new ShowService();
            showService.addShow(mShow.getMyEpisodeID(), user);
            return null;
        }

        @Override
        public void onTaskCompleted(Void result) {
            mFragment.onShowAdded(mShow);
        }
    }
}
