package eu.vranckaert.episodeWatcher.domain;

public class FeedItem {
    private String guid;
    private String title;
    private String link;
    private String description;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String toString() {
    	return "GUID: " + guid + " | Title: " + title + " | Link: " +link + " | Description: " + description;
    }
}
