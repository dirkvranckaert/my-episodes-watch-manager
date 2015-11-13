package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.android.viewholder.AbstractRecyclerViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;

/**
 * Date: 13/11/15
 * Time: 12:20
 *
 * @author Dirk Vranckaert
 */
public class AddShowsListItemView extends AbstractRecyclerViewHolder implements OnClickListener {
    private final TextView mShowName;
    private final TextView mEpisodeCount;

    public AddShowsListItemView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.new_add_shows_list_item);

        mShowName = findViewById(R.id.name);
        mEpisodeCount = findViewById(R.id.episode_count);
        getView().setOnClickListener(this);
    }

    public void setShow(Show show) {
        mShowName.setText(show.getShowName());
        mEpisodeCount.setText(getString(R.string.showSearchEpisodeCount, show.getEpisodeCount()));
    }

    @Override
    public void onClick(View v) {

    }
}
