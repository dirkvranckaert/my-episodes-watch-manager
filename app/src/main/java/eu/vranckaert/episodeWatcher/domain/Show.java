package eu.vranckaert.episodeWatcher.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Show implements Serializable {
	private String showName;
	private int episodeCount;
	private boolean added;
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

	public int getEpisodeCount() {
		return episodeCount;
	}

	public void setEpisodeCount(int episodeCount) {
		this.episodeCount = episodeCount;
	}

	public boolean isAdded() {
		return added;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}

	public void addEpisode(Episode episode) {
		episodes.add(episode);
	}
	
	public List<Episode> getEpisodes() {
		return episodes;
	}
	
	public Episode getFirstEpisode() {
		return episodes.get(0);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Show show = (Show) o;

		return myEpisodeID.equals(show.myEpisodeID);

	}

	@Override
	public int hashCode() {
		return myEpisodeID.hashCode();
	}
}
