package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;

/**
 * Date: 27/11/15
 * Time: 16:50
 *
 * @author Dirk Vranckaert
 */
public class RandomEpisodePickerView extends AbstractViewHolder {
    private final SwipeRefreshLayout mLoading;
    private final TextView mLabel;
    private final TextView mShow;
    private final TextView mEpisodeName;
    private final TextView mEpisodeNumber;

    public RandomEpisodePickerView(LayoutInflater inflater, ViewGroup parent) {
        super(inflater, parent, R.layout.new_random_episode_picker);

        mLoading = findViewById(R.id.refresh);
        mLabel = findViewById(R.id.label);
        mShow = findViewById(R.id.show);
        mEpisodeName = findViewById(R.id.episode_name);
        mEpisodeNumber = findViewById(R.id.episode_number);

        mLabel.setVisibility(GONE);
        mShow.setVisibility(GONE);
        mEpisodeName.setVisibility(GONE);
        mEpisodeNumber.setVisibility(GONE);
        mLoading.setEnabled(false);

        startLoading();
    }

    public void startLoading() {
        mLoading.setRefreshing(true);
    }

    public void setEpisode(Episode episode) {
        mLoading.setRefreshing(false);
        mLabel.setVisibility(VISIBLE);
        mShow.setVisibility(VISIBLE);
        mEpisodeName.setVisibility(VISIBLE);
        mEpisodeNumber.setVisibility(VISIBLE);

        mShow.setText(episode.getShowName());
        mEpisodeName.setText(episode.getName());
        mEpisodeNumber.setText(episode.getEpisodeString());
    }
}
