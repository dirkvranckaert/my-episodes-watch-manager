package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.app.Activity;
import android.content.Context;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import eu.vranckaert.android.recyclerview.MultiSelector;
import eu.vranckaert.android.recyclerview.MultiSelector.MultiSelectorListener;
import eu.vranckaert.android.recyclerview.SectionedAdapter;
import eu.vranckaert.android.viewholder.AbstractRecyclerViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.service.CacheService;

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
public class EpisodesListAdapter extends SectionedAdapter<AbstractRecyclerViewHolder> implements MultiSelectorListener {

    private final Context mContext;
    private final EpisodeType mType;
    private final EpisodesListListener mListener;
    private final MultiSelector mMultiSelector;

    private final List<ListItem> mListItems = new ArrayList<>();

    public EpisodesListAdapter(Context context, EpisodeType type, EpisodesListListener listener) {
        mContext = context;
        mType = type;
        mListener = listener;
        if (EpisodeType.EPISODES_TO_WATCH.equals(type) || EpisodeType.EPISODES_TO_ACQUIRE.equals(type)) {
            mMultiSelector = new MultiSelector((Activity) context, this, this);
        } else {
            mMultiSelector = null;
        }
    }

    public void setEpisodes(List<Episode> episodes) {
        int countBefore = mListItems.size();
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

        int countAfter = mListItems.size();
        if (countBefore != countAfter) {
            mListener.episodeCountHasUpdated();
        }

        notifyDataSetChanged();
    }

    public void removeAllEpisodes(List<Episode> episodes) {
        List<Episode> existingEpisodes = getEpisodes();
        existingEpisodes.removeAll(episodes);
        CacheService.storeEpisodes(existingEpisodes, mType);
        setEpisodes(existingEpisodes);
    }

    public void addAllEpisodes(List<Episode> episodes) {
        List<Episode> existingEpisodes = getEpisodes();

        for (Episode episode : episodes) {
            if (!existingEpisodes.contains(episode)) {
                existingEpisodes.add(episode);
            }
        }

        CacheService.storeEpisodes(existingEpisodes, mType);
        setEpisodes(existingEpisodes);
    }

    private List<Episode> getEpisodes() {
        List<Episode> existingEpisodes = new ArrayList<>();
        for (ListItem listItem : mListItems) {
            if (!listItem.isHeader()) {
                ListElement listElement = (ListElement) listItem;
                existingEpisodes.add(listElement.getEpisode());
            }
        }
        return existingEpisodes;
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
        return new EpisodeListItemView(LayoutInflater.from(mContext), parent, mMultiSelector);
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

    public void cancelContextualActionbar() {
        if (mMultiSelector != null) {
            mMultiSelector.cancelActionMode();
        }
    }

    @Override
    public boolean onSelectionUpdate(MultiSelector multiSelector, ActionMode actionMode) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(MultiSelector multiSelector, MenuItem item) {
        List<Episode> selectedEpisodes = new ArrayList<>();
        List<Integer> positions = multiSelector.getSelectedPositions();
        int count = positions.size();
        for (int i = 0; i < count; i++) {
            ListItem listItem = mListItems.get(positions.get(i));
            if (!listItem.isHeader()) {
                ListElement listElement = (ListElement) listItem;
                selectedEpisodes.add(listElement.getEpisode());
            }
        }

        if (item.getItemId() == R.id.watched) {
            mListener.markWatched(selectedEpisodes);
            return true;
        } else if (item.getItemId() == R.id.acquired) {
            mListener.markAcquired(selectedEpisodes);
            return true;
        }
        return false;
    }

    @Override
    public int getMenuRes() {
        return mType.equals(EpisodeType.EPISODES_TO_WATCH) ? R.menu.action_mode_to_watch :
                R.menu.action_mode_to_acquire;
    }

    @Override
    public void invalidateActionModeMenu(MultiSelector multiSelector, Menu menu) {

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

    public interface EpisodesListListener {
        void markWatched(List<Episode> episodes);

        void markAcquired(List<Episode> episodes);

        void episodeCountHasUpdated();
    }
}
