package eu.vranckaert.episodeWatcher.twopointo.context.shows;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.AddShowView;

/**
 * Date: 13/11/15
 * Time: 12:15
 *
 * @author Dirk Vranckaert
 */
public class AddShowFragment extends BaseFragment {
    private AddShowView mView;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.addShow);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = new AddShowView(inflater, container);
        return mView.getView();
    }
}
