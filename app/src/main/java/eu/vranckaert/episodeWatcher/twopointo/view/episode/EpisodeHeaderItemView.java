package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.android.viewholder.AbstractRecyclerViewHolder;
import eu.vranckaert.episodeWatcher.R;

/**
 * Date: 06/11/15
 * Time: 06:40
 *
 * @author Dirk Vranckaert
 */
public class EpisodeHeaderItemView extends AbstractRecyclerViewHolder {
    private final TextView mShowName;

    public EpisodeHeaderItemView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.new_episode_header_item);

        mShowName = findViewById(R.id.show_name);
    }

    public void setShowName(String showName) {
        mShowName.setText(showName);
    }
}
