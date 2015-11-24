package eu.vranckaert.episodeWatcher.twopointo.context.shows;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.Callback;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.ShowAction;
import eu.vranckaert.episodeWatcher.enums.ShowType;
import eu.vranckaert.episodeWatcher.service.ShowService;
import eu.vranckaert.episodeWatcher.twopointo.threading.MyEpisodesTask;
import eu.vranckaert.episodeWatcher.twopointo.utils.SnackbarUtil;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.ManageShowsView;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.ManageShowsView.ManageShowsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 24/11/15
 * Time: 14:03
 *
 * @author Dirk Vranckaert
 */
public class IgnoredShowsFragment extends BaseFragment implements ManageShowsListener {
    private ManageShowsView mView;
    private ListShowsTask mListShowsTask;
    private final List<ChangeShowTask> mChangeShowTasks = new ArrayList<>();
    private Snackbar mSnackbar;
    private boolean mStopping = false;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.ignoredShows);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new ManageShowsView(inflater, container, this);
        loadShows();
        return mView.getView();
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
                .setNegativeButton(R.string.favoIgnoredUnignoreShow, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onUnignoreShow(show);
                    }
                })
                .setNeutralButton(R.string.close, null)
                .show();
    }

    @Override
    public void onRemoveShow(final Show show) {
        mView.removeShow(show);

        if (mSnackbar != null) {
            mSnackbar.dismiss();
        }

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

    private void onUnignoreShow(final Show show) {
        mView.removeShow(show);

        if (mSnackbar != null) {
            mSnackbar.dismiss();
        }

        mSnackbar = Snackbar.make(getView(), R.string.favoIgnoredIgnoredShow, Snackbar.LENGTH_LONG);
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
                    unignoreShow(show);
                    snackbar.setCallback(null);
                }
            }
        });
        mSnackbar = SnackbarUtil.colorSnackBar(mSnackbar, getResources().getColor(R.color.primary_color));
        mSnackbar = SnackbarUtil.colorSnackBarText(mSnackbar, getResources().getColor(android.R.color.white));
        mSnackbar = SnackbarUtil.colorSnackBarAction(mSnackbar, getResources().getColor(android.R.color.white));
        mSnackbar.show();
    }

    public void unignoreShow(Show show) {
        if (mStopping) {
            return;
        }

        setResult(RESULT_OK);

        ChangeShowTask changeShowTask = new ChangeShowTask(this, show, ShowAction.IGNORE);
        mChangeShowTasks.add(changeShowTask);
        changeShowTask.execute();
    }

    private void showRemovalFailed(Show show) {
        mView.addShow(show);
        // TODO should show some kind of message (SnackBar) that removing a show failed
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

    public static class ListShowsTask extends MyEpisodesTask<List<Show>> {
        private final IgnoredShowsFragment mFragment;

        public ListShowsTask(IgnoredShowsFragment fragment) {
            super(fragment.getContext());
            mFragment = fragment;
        }

        @Override
        public List<Show> doInBackground() throws Exception {
            ShowService showService = new ShowService();
            return showService.getFavoriteOrIgnoredShows(User.get(mFragment.getContext()), ShowType.IGNORED_SHOWS);
        }

        @Override
        public void onTaskCompleted(List<Show> result) {
            mFragment.onShowsLoaded(result);
        }
    }

    public static class ChangeShowTask extends MyEpisodesTask<Void> {
        private final IgnoredShowsFragment mFragment;
        private final Show mShow;
        private final ShowAction mAction;

        public ChangeShowTask(IgnoredShowsFragment fragment, Show show, ShowAction action) {
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
            showService.markShow(User.get(mFragment.getContext()), mShow, mAction, ShowType.IGNORED_SHOWS);
            return null;
        }
    }
}
