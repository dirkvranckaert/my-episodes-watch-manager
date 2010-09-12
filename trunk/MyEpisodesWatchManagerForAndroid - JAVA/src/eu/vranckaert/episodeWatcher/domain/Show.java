package eu.vranckaert.episodeWatcher.domain;

import java.util.ArrayList;
import java.util.List;

public class Show {
	private String showName;
	private List<Episode> episodes = new ArrayList<Episode>(0);
    private String myEpisodeID;

	public Show(String showName) {
		this.showName = showName;
	}

    public Show(String showName, String myEpisodeID) {
		this.showName = showName;
        this.myEpisodeID = myEpisodeID;
	}
	
    /**
     * Gets the show name.
     *
     * @return The show name.
     */
	public String getShowName() {
		return showName;
	}
	
    /**
     * Set the show name.
     *
     * @param showName The show name.
     */
	public void setShowName(String showName) {
		this.showName = showName;
	}

	public void addEpisode(Episode episode) {
		episodes.add(episode);
	}
	
	public List<Episode> getEpisodes() {
		return episodes;
	}
	
	public int getNumberEpisodes() {
		return episodes.size();
	}

    public String getMyEpisodeID() {
        return myEpisodeID;
    }

    public void setMyEpisodeID(String myEpisodeID) {
        this.myEpisodeID = myEpisodeID;
    }
}
