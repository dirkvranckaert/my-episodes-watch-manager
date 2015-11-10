package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.android.recyclerview.RecyclerViewUtil;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;

import java.util.List;

/**
 * Date: 04/11/15
 * Time: 08:09
 *
 * @author Dirk Vranckaert
 */
public class EpisodesListView extends AbstractViewHolder {
    private final RecyclerView mList;
    private final EpisodesListAdapter mAdapter;

    public EpisodesListView(LayoutInflater inflater, ViewGroup parent, EpisodeType type) {
        super(inflater, parent, R.layout.new_episodes_list);

        mList = findViewById(R.id.list);
        RecyclerViewUtil.initSuperSlim(mList, R.color.divider_color);
        mAdapter = new EpisodesListAdapter(getContext(), type);
        mList.setAdapter(mAdapter);

    }

    public void setLoading(boolean loading) {
        // TODO set loading correctly
    }

    public void setEpisodes(List<Episode> episodes) {
        mAdapter.setEpisodes(episodes);
    }

    public void cancelContextualActionbar() {
        mAdapter.cancelContextualActionbar();
    }
}
