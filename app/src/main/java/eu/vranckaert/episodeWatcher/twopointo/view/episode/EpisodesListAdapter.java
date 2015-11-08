package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.android.recyclerview.SectionedAdapter;
import eu.vranckaert.android.viewholder.AbstractRecyclerViewHolder;
import eu.vranckaert.episodeWatcher.domain.Episode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 05/11/15
 * Time: 18:15
 *
 * @author Dirk Vranckaert
 */
public class EpisodesListAdapter extends SectionedAdapter<AbstractRecyclerViewHolder> {

    public final Context mContext;

    private final List<ListItem> mListItems = new ArrayList<>();

    public EpisodesListAdapter(Context context) {
        mContext = context;
    }

    public void setEpisodes(List<Episode> episodes) {
        mListItems.clear();

        Collections.sort(episodes, new Comparator<Episode>() {
            @Override
            public int compare(Episode lhs, Episode rhs) {
                return lhs.getShowName().compareTo(rhs.getShowName());
            }
        });

        int currentHeaderPosition = 0;
        int episodeCount = episodes.size();
        List<String> addedHeaders = new ArrayList<>();
        for (int i = 0; i < episodeCount; i++) {
            Episode episode = episodes.get(i);

            String showName = episode.getShowName();
            if (!addedHeaders.contains(showName)) {
                addedHeaders.add(showName);

                HeaderElement headerElement = new HeaderElement();
                headerElement.setShowName(showName);
                mListItems.add(headerElement);
                currentHeaderPosition = mListItems.size() - 1;
            }

            ListElement listElement = new ListElement(currentHeaderPosition, episode);
            mListItems.add(listElement);
        }

        notifyDataSetChanged();
    }

    @Override
    public boolean isHeader(int position) {
        return mListItems.get(position).isHeader();
    }

    @Override
    public boolean isLinear() {
        return true;
    }

    @Override
    public AbstractRecyclerViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new EpisodeHeaderItemView(LayoutInflater.from(mContext), parent);
    }

    @Override
    public AbstractRecyclerViewHolder onCreateElementViewHolder(ViewGroup parent) {
        return new EpisodeListItemView(LayoutInflater.from(mContext), parent);
    }

    @Override
    protected void onBindHeaderViewHolder(AbstractRecyclerViewHolder holder, int position) {
        ((EpisodeHeaderItemView) holder).setShowName(((HeaderElement) mListItems.get(position)).getShowName());
    }

    @Override
    protected void onBindElementViewHolder(AbstractRecyclerViewHolder holder, int position) {
        ListElement listElement = (ListElement) mListItems.get(position);
        ((EpisodeListItemView) holder).setEpisode(listElement.getEpisode());
    }

    @Override
    protected int getHeaderPosition(int position) {
        ListItem listItem = mListItems.get(position);
        if (listItem.isHeader()) {
            return position;
        } else {
            return ((ListElement) listItem).getHeaderPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mListItems.size();
    }

    private abstract class ListItem {
        public boolean isHeader() {
            return this instanceof HeaderElement;
        }
    }

    private class HeaderElement extends ListItem {
        private String showName;

        public String getShowName() {
            return showName;
        }

        public void setShowName(String showName) {
            this.showName = showName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            HeaderElement that = (HeaderElement) o;

            return !(showName != null ? !showName.equals(that.showName) : that.showName != null);

        }

        @Override
        public int hashCode() {
            return showName != null ? showName.hashCode() : 0;
        }
    }

    private class ListElement extends ListItem {
        private int headerPosition;
        private Episode episode;

        public ListElement(int headerPosition, Episode episode) {
            this.headerPosition = headerPosition;
            this.episode = episode;
        }

        public int getHeaderPosition() {
            return headerPosition;
        }

        public Episode getEpisode() {
            return episode;
        }
    }
}
