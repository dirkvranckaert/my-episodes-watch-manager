package eu.vranckaert.episodeWatcher.twopointo.context.episode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.twopointo.view.episode.EpisodesTabsView;

/**
 * Date: 04/11/15
 * Time: 07:51
 *
 * @author Dirk Vranckaert
 */
public class EpisodesTabFragment extends BaseFragment {
    private EpisodesTabsView mView;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);

        setTitle(R.string.watchListTitle);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new EpisodesTabsView(inflater, container);
        return mView.getView();
    }
}
