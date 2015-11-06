package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LinearSLM;
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
public class EpisodesListAdapter extends Adapter<AbstractRecyclerViewHolder> {
    private static final int VIEW_TYPE_HEADER_ELEMENT = 0x01;
    private static final int VIEW_TYPE_LIST_ELEMENT = 0x02;

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
    public int getItemViewType(int position) {
        return mListItems.get(position).isHeader() ? VIEW_TYPE_HEADER_ELEMENT : VIEW_TYPE_LIST_ELEMENT;
    }

    @Override
    public AbstractRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LIST_ELEMENT) {
            EpisodeListItemView view = new EpisodeListItemView(LayoutInflater.from(mContext), parent);
            return view;
        } else {
            EpisodeHeaderItemView view = new EpisodeHeaderItemView(LayoutInflater.from(mContext), parent);
            return view;
        }
    }

    @Override
    public void onBindViewHolder(AbstractRecyclerViewHolder holder, int position) {
        ListItem listItem = mListItems.get(position);

        int viewType = getItemViewType(position);
        int headerPosition = position;
        if (viewType == VIEW_TYPE_LIST_ELEMENT) {
            ListElement listElement = (ListElement) listItem;
            ((EpisodeListItemView) holder).setEpisode(listElement.getEpisode());
            headerPosition = listElement.getHeaderPosition();
        } else if (viewType == VIEW_TYPE_HEADER_ELEMENT) {
            ((EpisodeHeaderItemView) holder).setShowName(((HeaderElement) listItem).getShowName());
        }

        View itemView = holder.getView();
        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
        lp.setSlm(LinearSLM.ID);
        lp.setFirstPosition(headerPosition);
        itemView.setLayoutParams(lp);
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
