package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.episodeWatcher.domain.Episode;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 05/11/15
 * Time: 18:15
 *
 * @author Dirk Vranckaert
 */
public class EpisodesListAdapter extends Adapter<EpisodeListItemView> {
    public final Context mContext;

    private final List<Episode> mEpisodes = new ArrayList<>();

    public EpisodesListAdapter(Context context) {
        mContext = context;
    }

    public void setEpisodes(List<Episode> episodes) {
        mEpisodes.clear();
        mEpisodes.addAll(episodes);
        notifyDataSetChanged();
    }

    @Override
    public EpisodeListItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        EpisodeListItemView view = new EpisodeListItemView(LayoutInflater.from(mContext), parent);
        return view;
    }

    @Override
    public void onBindViewHolder(EpisodeListItemView holder, int position) {
        holder.setEpisode(mEpisodes.get(position));
    }

    @Override
    public int getItemCount() {
        return mEpisodes.size();
    }
}
