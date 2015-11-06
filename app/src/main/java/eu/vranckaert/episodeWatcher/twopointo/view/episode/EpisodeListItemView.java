package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.android.viewholder.AbstractRecyclerViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;

/**
 * Date: 05/11/15
 * Time: 18:16
 *
 * @author Dirk Vranckaert
 */
public class EpisodeListItemView extends AbstractRecyclerViewHolder implements OnClickListener {
    private final TextView mEpisodeName;
    private final TextView mEpisodeNumber;
    private final TextView mAirDate;
    private final TextView mLink;

    public EpisodeListItemView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.new_episode_list_item);

        mEpisodeName = findViewById(R.id.episode_name);
        mEpisodeNumber = findViewById(R.id.episode_number);
        mAirDate = findViewById(R.id.air_date);
        mLink = findViewById(R.id.link);
        // TODO linkify!

        getView().setOnClickListener(this);
    }

    public void setEpisode(Episode episode) {
        mEpisodeName.setText(episode.getName());
        mEpisodeNumber.setText(episode.getSeason() + "x" + episode.getEpisode()); // TODO check the formatting in the current app for seasonXepisode
        mAirDate.setText(episode.getAirDate().toString()); // TODO format the air date

        if (TextUtils.isEmpty(episode.getTVRageWebSite())) {
            mLink.setVisibility(GONE);
        } else {
            mLink.setVisibility(VISIBLE);
            mLink.setText(episode.getTVRageWebSite());
        }
    }

    @Override
    public void onClick(View v) {

    }
}
