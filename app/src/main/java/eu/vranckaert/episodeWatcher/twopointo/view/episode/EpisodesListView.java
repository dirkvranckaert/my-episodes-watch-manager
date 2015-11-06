package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.tonicartos.superslim.LayoutManager;
import eu.vranckaert.android.recyclerview.ListItemDividerDecoration;
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
    private final RecyclerView mList;
    private final EpisodesListAdapter mAdapter;

    public EpisodesListView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.new_episodes_list);

        mList = findViewById(R.id.list);
        //RecyclerViewUtil.init(mList, R.color.divider_color);
        // TODO move to library project
        LayoutManager layoutManager = new LayoutManager(getContext());
        mList.setLayoutManager(layoutManager);
        mAdapter = new EpisodesListAdapter(getContext());
        mList.setAdapter(mAdapter);
        //mList.addItemDecoration(new ListItemDividerDecoration(getContext(), R.color.divider_color, 1));

    }

    public void setLoading(boolean loading) {
        // TODO set loading correctly
    }

    public void setEpisodes(List<Episode> episodes) {
        mAdapter.setEpisodes(episodes);
    }
}
