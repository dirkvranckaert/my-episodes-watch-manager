package eu.vranckaert.episodeWatcher.twopointo.context.shows;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.context.BaseFragment;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.twopointo.context.NavigationManager;

/**
 * Date: 13/11/15
 * Time: 12:09
 *
 * @author Dirk Vranckaert
 */
public class ManageShowsFragment extends BaseFragment {
    private static final int REQUEST_CODE_ADD_SHOW = 0;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.manageShows);
    }

    @Override
    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
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
}
