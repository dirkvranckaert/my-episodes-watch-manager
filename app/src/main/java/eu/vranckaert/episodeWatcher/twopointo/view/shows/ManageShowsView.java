package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.android.recyclerview.RecyclerViewUtil;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;

import java.util.List;

/**
 * Date: 16/11/15
 * Time: 08:32
 *
 * @author Dirk Vranckaert
 */
public class ManageShowsView extends AbstractViewHolder {
    private final RecyclerView mList;
    private final ManageShowsAdapter mAdapter;

    public ManageShowsView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.new_manage_shows);

        mList = findViewById(R.id.list);
        RecyclerViewUtil.init(mList, R.color.divider_color);
        mAdapter = new ManageShowsAdapter(getContext());
        mList.setAdapter(mAdapter);
    }

    public void setShows(List<Show> shows) {
        mAdapter.setShows(shows);
    }

    public void addShow(Show show) {
        mAdapter.addShow(show);
    }
}
