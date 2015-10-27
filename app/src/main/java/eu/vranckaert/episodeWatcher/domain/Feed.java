package eu.vranckaert.episodeWatcher.domain;

import java.util.ArrayList;
import java.util.List;

public class Feed {
    List<FeedItem> items = new ArrayList<FeedItem>(0);

    public List<FeedItem> getItems() {
        return items;
    }

    public void addItem(FeedItem item) {
        items.add(item);
    }
}
