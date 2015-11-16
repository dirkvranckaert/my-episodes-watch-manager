package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.android.viewholder.AbstractRecyclerViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;

/**
 * Date: 16/11/15
 * Time: 08:37
 *
 * @author Dirk Vranckaert
 */
public class ManageShowsListItemView extends AbstractRecyclerViewHolder {
    public ManageShowsListItemView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.new_manage_shows_list_item);
    }

    public void setShow(Show show) {

    }
}
