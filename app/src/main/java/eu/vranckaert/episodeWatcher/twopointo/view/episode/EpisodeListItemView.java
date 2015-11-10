package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.android.recyclerview.MultiSelector;
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
    private final MultiSelector mMultiSelector;
    private final TextView mEpisodeName;
    private final TextView mEpisodeNumber;
    private final TextView mAirDate;

    public EpisodeListItemView(LayoutInflater inflater, ViewGroup parent, MultiSelector multiSelector) {
        super(inflater, parent, R.layout.new_episode_list_item);
        mMultiSelector = multiSelector;

        mEpisodeName = findViewById(R.id.episode_name);
        mEpisodeNumber = findViewById(R.id.episode_number);
        mAirDate = findViewById(R.id.air_date);

        getView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mMultiSelector.tapSelection(EpisodeListItemView.this)) {
                    // Normal click
                }
            }
        });

        getView().setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                if (!mMultiSelector.isSelectionMode() && !mMultiSelector.isItemChecked(position)) {
                    mMultiSelector.setItemChecked(EpisodeListItemView.this, true);
                    return true;
                }
                return false;
            }
        });
    }

    public void setEpisode(Episode episode) {
        getView().setSelected(mMultiSelector.isItemChecked(getAdapterPosition()));

        mEpisodeName.setText(episode.getName());
        mEpisodeNumber.setText(episode.getSeason() + "x" + episode.getEpisode()); // TODO check the formatting in the current app for seasonXepisode
        mAirDate.setText(episode.getAirDate().toString()); // TODO format the air date
    }

    @Override
    public void onClick(View v) {

    }
}
