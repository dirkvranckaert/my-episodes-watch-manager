package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;

import java.util.List;

/**
 * Date: 04/11/15
 * Time: 08:09
 *
 * @author Dirk Vranckaert
 */
public class EpisodesListView extends AbstractViewHolder {
    public EpisodesListView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.new_episodes_list);
    }

    public void setLoading(boolean loading) {
        // TODO set loading correctly
    }

    public void setEpisodes(List<Episode> episodes) {
        // TODO add them to the adapter...
    }
}
