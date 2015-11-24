package eu.vranckaert.episodeWatcher.twopointo.context.shows;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.Callback;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.ShowAction;
import eu.vranckaert.episodeWatcher.enums.ShowType;
import eu.vranckaert.episodeWatcher.service.ShowService;
import eu.vranckaert.episodeWatcher.twopointo.context.NavigationManager;
import eu.vranckaert.episodeWatcher.twopointo.threading.MyEpisodesTask;
import eu.vranckaert.episodeWatcher.twopointo.utils.SnackbarUtil;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.ManageShowsView;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.ManageShowsView.ManageShowsListener;

import java.util.ArrayList;
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
    private final List<ChangeShowTask> mChangeShowTasks = new ArrayList<>();
    private Snackbar mSnackbar;
    private boolean mStopping = false;

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
        mStopping = true;

        if (mListShowsTask != null) {
            mListShowsTask.cancel();
            mListShowsTask = null;
        }

        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }

        int deleteShowTasks = mChangeShowTasks.size();
        for (int i = 0; i < deleteShowTasks; i++) {
            mChangeShowTasks.get(i).cancel();
        }
        mChangeShowTasks.clear();

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
            if (requestCode == REQUEST_CODE_ADD_SHOW && resultCode == RESULT_OK && data != null) {
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

    @Override
    public void onShowClick(final Show show) {
        new AlertDialog.Builder(getContext())
                .setTitle(show.getShowName())
                .setPositiveButton(R.string.favoIgnoredDeleteShow, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onRemoveShow(show);
                    }
                })
                .setNegativeButton(R.string.favoIgnoredIgnoreShow, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onIgnoreShow(show);
                    }
                })
                .setNeutralButton(R.string.close, null)
                .show();
    }

    @Override
    public void onRemoveShow(final Show show) {
        mView.removeShow(show);

        mSnackbar = Snackbar.make(getView(), R.string.favoIgnoredDeletedShow, Snackbar.LENGTH_LONG);
        mSnackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.addShow(show);
            }
        });
        mSnackbar.setCallback(new Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (Callback.DISMISS_EVENT_ACTION != event) {
                    removeShow(show);
                    snackbar.setCallback(null);
                }
            }
        });
        mSnackbar = SnackbarUtil.colorSnackBar(mSnackbar, getResources().getColor(R.color.primary_color));
        mSnackbar = SnackbarUtil.colorSnackBarText(mSnackbar, getResources().getColor(android.R.color.white));
        mSnackbar = SnackbarUtil.colorSnackBarAction(mSnackbar, getResources().getColor(android.R.color.white));
        mSnackbar.show();
    }

    public void removeShow(Show show) {
        if (mStopping) {
            return;
        }

        ChangeShowTask changeShowTask = new ChangeShowTask(this, show, ShowAction.DELETE);
        mChangeShowTasks.add(changeShowTask);
        changeShowTask.execute();
    }

    private void onIgnoreShow(final Show show) {
        mView.removeShow(show);

        Snackbar snackbar = Snackbar.make(getView(), R.string.favoIgnoredIgnoredShow, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView.addShow(show);
            }
        });
        snackbar.setCallback(new Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (Callback.DISMISS_EVENT_ACTION != event) {
                    ignoreShow(show);
                    snackbar.setCallback(null);
                }
            }
        });
        snackbar = SnackbarUtil.colorSnackBar(snackbar, getResources().getColor(R.color.primary_color));
        snackbar = SnackbarUtil.colorSnackBarText(snackbar, getResources().getColor(android.R.color.white));
        snackbar = SnackbarUtil.colorSnackBarAction(snackbar, getResources().getColor(android.R.color.white));
        snackbar.show();
    }

    public void ignoreShow(Show show) {
        if (mStopping) {
            return;
        }

        ChangeShowTask changeShowTask = new ChangeShowTask(this, show, ShowAction.IGNORE);
        mChangeShowTasks.add(changeShowTask);
        changeShowTask.execute();
    }

    private void showRemovalFailed(Show show) {
        mView.addShow(show);
        // TODO should show some kind of message (SnackBar) that removing a show failed
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

    public static class ChangeShowTask extends MyEpisodesTask<Void> {
        private final ManageShowsFragment mFragment;
        private final Show mShow;
        private final ShowAction mAction;

        public ChangeShowTask(ManageShowsFragment fragment, Show show, ShowAction action) {
            super(fragment.getContext());
            mFragment = fragment;
            mShow = show;
            mAction = action;
            setShowErrorDialog(false);
        }

        @Override
        public void onError(Exception exception) {
            mFragment.showRemovalFailed(mShow);
        }

        @Override
        public Void doInBackground() throws Exception {
            ShowService showService = new ShowService();
            showService.markShow(User.get(mFragment.getContext()), mShow, mAction, ShowType.FAVOURITE_SHOWS);
            return null;
        }
    }
}
