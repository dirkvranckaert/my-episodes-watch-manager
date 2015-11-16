package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.android.viewholder.AbstractRecyclerViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.AddShowView.AddShowsListener;

/**
 * Date: 13/11/15
 * Time: 12:20
 *
 * @author Dirk Vranckaert
 */
public class AddShowsListItemView extends AbstractRecyclerViewHolder implements OnClickListener {
    private final AddShowsListener mListener;
    private final TextView mShowName;
    private final TextView mEpisodeCount;
    private final TextView mAdded;

    private Show mShow;

    public AddShowsListItemView(LayoutInflater inflater, ViewGroup parent, AddShowsListener listener) {
        super(inflater, parent, R.layout.new_add_shows_list_item);
        mListener = listener;

        mShowName = findViewById(R.id.name);
        mEpisodeCount = findViewById(R.id.episode_count);
        mAdded = findViewById(R.id.added);
        getView().setOnClickListener(this);
    }

    public void setShow(Show show) {
        mShow = show;
        mShowName.setText(show.getShowName());
        mEpisodeCount.setText(getString(R.string.showSearchEpisodeCount, show.getEpisodeCount()));
        mAdded.setVisibility(show.isAdded() ? VISIBLE : GONE);
        getView().setClickable(!show.isAdded());
    }

    @Override
    public void onClick(View v) {
        mListener.addShow(mShow);
    }
}
